package org.iblog.enhance.gateway.lifecycle.sched;

import lombok.Setter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.constraints.NotNull;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.iblog.enhance.gateway.controller.RehandleHandler;
import org.iblog.enhance.gateway.controller.ReprocessHandler;
import org.iblog.enhance.gateway.core.SchedulableWork;
import org.iblog.enhance.gateway.service.SchedulableWorkService;
import org.iblog.enhance.gateway.util.Clock;
import org.iblog.enhance.gateway.util.ExecutorUtil;
import org.iblog.enhance.gateway.util.ObjectMappers;
import org.iblog.enhance.gateway.util.WorkQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;

/**
 * XTaskScheduler id typically used for tasks whose scheduling that need
 * to be persistent (i.e. can survive service restarts),
 * and/or globally visible (e.g. can be executed by another instance).
 * A task scheduled can be.
 *
 * There are notable limitations for this component due to the underlying
 * queueing mechanism:
 *   - It suits only for tasks that don't require high precision in terms of fire time.
 *   The actual fire time of a task can be a few seconds later than the scheduled timestamp.
 *   - For periodical tasks, the interval between consecutive invocations need to
 *   be large enough (> 1 minute).
 *
 * @author lance
 */
@Component
@ConfigurationProperties(prefix = "enhance-gateway.x-task-scheduler")
public class XTaskScheduler {
    private static final Logger logger = LoggerFactory.getLogger(XTaskScheduler.class);

    private final Clock clock = Clock.defaultClock();
    private final AtomicBoolean stopping = new AtomicBoolean();

    private ConcurrentHashMap<String, SchedulableHandler> handlers;
    private Qualifier canHandle;
    private ThreadPoolExecutor executor;
    private WorkQueue workQueue;

    private final AtomicLong WAITING = new AtomicLong();

    @Setter
    private long pollPause = 2000;
    @Setter
    private int poolSize = 5;
    @Setter
    private String nameSuffix = "";
    @Setter
    private String queueName = "schedulable-works-queue";
    @Setter
    private String possessor = "evo-interface-node-1";
    @Setter
    private int maxReties = 10;
    @Autowired
    private SchedulableWorkService schedulableWorkService;
    @Autowired
    private RehandleHandler rehandleHandler;
    @Autowired
    private ReprocessHandler reprocessHandler;

    @PostConstruct
    public void start() {
        handlers = new ConcurrentHashMap<>();
        canHandle = this::canHandle;
        executor = new ThreadPoolExecutor(
                poolSize, poolSize, 0, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new NamedThreadFactory("XTaskScheduler" + nameSuffix)) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                ExecutorUtil.afterExecute2(r, t, (r2, t2) -> {
                    logger.error("Unexpected error during execution: ", t2);
                });
            }
        };
        workQueue = new WorkQueue(queueName, schedulableWorkService);
        logger.info("XTaskScheduler start at {}", clock.getTime());

        injectHandlers();
        // commit tasks to executor
        for (int i = 0; i < executor.getCorePoolSize(); i++) {
            executor.submit(this::pollAndRun);
        }
        logger.info("started {} workers", executor.getCorePoolSize());
    }

    @PreDestroy
    public void destroy() {
        stopping.set(true);
        logger.info("XTaskScheduler destroy at {}", clock.getTime());
    }

    private void injectHandlers() {
        handlers.put(rehandleHandler.type(), rehandleHandler);
        handlers.put(reprocessHandler.type(), reprocessHandler);
        logger.info("finish injecting schedulable handlers: size {}", handlers.size());
    }

    public boolean scheduleWithFixedDelay(
            long initialDelay, long delay, String type, String id, int retry, int maxRetries) {
        SchedulableWork sched = new SchedulableWork();
        sched.setId(id);
        sched.setType(type);
        sched.setScheduledAt(clock.getTime() + (initialDelay > 0 ? initialDelay : 0));
        sched.setIntervalTime(delay > 0 ? delay : 0);
        sched.setFinished(false);
        sched.setDiscarded(false);
        sched.setRetry(retry);
        sched.setMaxRetries(maxRetries > 0 ? maxRetries : this.maxReties);
        if (delay > 0) {
            WAITING.set(Math.min(WAITING.get(), delay));
        }
        return workQueue.scheduleAtIfAbsent(sched);
    }

    private void pollAndRun() {
        while (!stopping.get()) {
            SchedulableWork work = null;
            Schedulable task;
            boolean testCanHandle = true;
            boolean outstanding = false;
            String currentPossessor = possessor + "_" + Thread.currentThread().getName();
            try {
                work = workQueue.pollIf(currentPossessor);
                if (work == null) {
                    Uninterruptibles.sleepUninterruptibly(pollPause, TimeUnit.MILLISECONDS);
                    continue;
                }
                long tolerate = tolerate(work);
                if (tolerate > 0) {
                    // TODO an unreasonable practice
                    WAITING.set(Math.min(WAITING.get(), tolerate));
                    Uninterruptibles.sleepUninterruptibly(WAITING.get(), TimeUnit.MILLISECONDS);
                }
                task = new Schedulable(work.getType(), ObjectMappers.mustWriteValue(work));
                if (task == null || Strings.isNullOrEmpty(task.getType())) {
                    logger.error("Encountered task without content or type: {}",
                            ObjectMappers.mustWriteValue(work));
                    continue;
                }
                if (canHandle != null) {
                    testCanHandle = false;
                    try {
                        testCanHandle = canHandle.canHandle(ObjectMappers.mustWriteValue(task));
                    } catch (Throwable t) {
                        logger.error("Unexpected error while calling qualifier {}",
                                canHandle.getClass(), t);
                    }
                    if (!testCanHandle) {
                        continue;
                    }
                }
                outstanding = run(task);
            } catch (Throwable t) {
                logger.error("Unexpected error while handing {}", work == null ? "N/A" : work.getId(), t);
            } finally {
                if (work == null) {
                    continue;
                }
                if (!testCanHandle) {
                    logger.info("task {} is unhandleable, need fixed delay", work.getId());
                    workQueue.scheduleAt(work.getIntervalTime(), work.getId());
                }
                if (!outstanding) {
                    logger.info("task {} is failed, need to fixed delay", work.getId());
                    workQueue.scheduleAt(work.getIntervalTime(), work.getId());
                } else {
                    boolean finished = workQueue.finish(currentPossessor, work.getId());
                    if (finished) {
                        logger.info("task {} is finished, need to mark finished", work.getId());
                    } else {
                        logger.error("task {} is finished, but mark occur exception", work.getId());
                    }
                }
                workQueue.releaseWork(work.getId());
            }
        }
    }

    private boolean run(@NotNull Schedulable task) {
        SchedulableHandler handler = handlers.get(task.getType());
        if (handler == null) {
            return false; // should never happen
        }
        return handler.handle(task);
    }

    private boolean canHandle(String content) {
        try {
            Schedulable sched = ObjectMappers.mustReadValue(content, Schedulable.class);
            return sched != null
                    && !Strings.isNullOrEmpty(sched.getType())
                    && handlers.containsKey(sched.getType());
        } catch (Throwable t) {
            return false;
        }
    }

    private long tolerate(@NotNull SchedulableWork work) {
        if (work.getIntervalTime() == 0) {
            return TimeUnit.SECONDS.toMillis(10);
        }
        long lead = Math.abs(clock.getTime() - work.getScheduledAt());
        if (lead <= work.getIntervalTime()
                && lead <= Math.min(work.getIntervalTime() / 10, TimeUnit.SECONDS.toMillis(6))) {
            // can handle
            return 0;
        }
        return lead;
    }
}
