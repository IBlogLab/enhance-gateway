package org.iblog.enhance.gateway.lifecycle.sched;

import javax.annotation.PreDestroy;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * TODO describe
 *
 * @author lance
 */
@Component
public class GlobalExecutor {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExecutor.class);

    private static final GlobalExecutor INSTANCE = new GlobalExecutor();

    private ScheduledExecutorService executor;

    public static GlobalExecutor get() {
        return INSTANCE;
    }

    protected GlobalExecutor() {
        this.executor = new ScheduledThreadPoolExecutor(
                Math.max(32, 4 * Runtime.getRuntime().availableProcessors()),
                new NamedThreadFactory("GlobalExecutor"),
                (r, e) -> { /* Ignore */ }) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                afterTaskExecute(r, t);
            }
        };
    }

    @PreDestroy
    public void stop() throws InterruptedException {
        this.executor.shutdown();
        this.executor.awaitTermination(200, TimeUnit.MILLISECONDS);
        this.executor.shutdownNow();
    }

    protected void afterTaskExecute(Runnable r, Throwable t) {
        if (t == null && r instanceof Future<?> && ((Future) r).isDone()) {
            try {
                ((Future<?>) r).get();
            } catch (CancellationException ce) {
                t = ce;
            } catch (ExecutionException ee) {
                t = ee.getCause();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt(); // ignore/reset
            }
        }
        if (t != null) {
            logger.error("Task in GlobalExecutor terminated abruptly:", t);
        }
    }

    public <V> Future<V> submit(final Callable<V> task) {
        return this.executor.submit(task);
    }

    public void submit(final Runnable task) {
        this.executor.submit(task);
    }

    public ScheduledFuture<?> schedule(final Runnable task, long initialDelay, long fixedDelay) {
        return this.executor.scheduleWithFixedDelay(() -> {
            try {
                task.run();
            } catch (Throwable t) {
                logger.error("Task in GlobalExecutor terminated abruptly: ", t);
            }
        }, initialDelay, fixedDelay, TimeUnit.MILLISECONDS);
    }

}
