package org.iblog.enhance.gateway.util;

import java.util.concurrent.TimeUnit;
import org.iblog.enhance.gateway.core.SchedulableWork;
import org.iblog.enhance.gateway.db.filter.SchedulableWorkFilter;
import org.iblog.enhance.gateway.service.SchedulableWorkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Strings;

/**
 * @author shaoxiao.xu
 * @date 2019/1/16 17:38
 */
public class WorkQueue {
    private static final Logger logger = LoggerFactory.getLogger(WorkQueue.class);

    private final String queueName;
    private final SchedulableWorkService workService;
    private final Clock clock = Clock.defaultClock();

    public WorkQueue(String queueName, SchedulableWorkService workService) {
        this.queueName = queueName;
        this.workService = workService;
    }

    public boolean scheduleAtIfAbsent(SchedulableWork work) {
        if (Strings.isNullOrEmpty(work.getType())) {
            logger.trace("reject work {}: work type is null", work.getId());
            return false;
        }
        if (work.getScheduledAt() < clock.getTime()) {
            // overtime, how to handle expired work
            logger.trace("received a expired work {}: go into overtime", work.getId());
            work.setScheduledAt(clock.getTime() + TimeUnit.SECONDS.toMillis(10));
        }
        boolean exist = workService.exist(work.getId());
        if (exist) {
            logger.trace("reject an existing work {}", work.getId());
            return false;
        }
        workService.create(work);
        logger.info("{} schedule task {}", queueName, work.getId());
        return true;
    }

    public boolean scheduleAt(long delay, String workId) {
        if (Strings.isNullOrEmpty(workId) || delay <= 0) {
            return false;
        }
        SchedulableWork exist = workService.find(workId).orNull();
        if (exist == null) {
            return false;
        }
        if (!check(exist)) {
            logger.info("{} reject a delay schedule {}", queueName, workId);
            return false;
        }
        exist.setScheduledAt(exist.getScheduledAt() + delay);
        workService.update(exist);
        logger.info("{} schedule task {} at {}", queueName, workId, exist.getScheduledAt());
        return true;
    }

    public SchedulableWork pollIf(String possessor) {
        SchedulableWork meta = workService.findAndModify(
                new SchedulableWorkFilter.Builder()
                        .setExcludeFinished(true)
                        .setUncredited(true)
                        .setExcludeDiscarded(true)
                        .build(),
                possessor)
                .orNull();
        if (meta == null) {
            return null;
        }
        if (!check(meta)) {
            return null;
        }
        logger.trace("{} poll work {}", queueName, meta.getId());
        return meta;
    }

    public boolean finish(String possessor, String workId) {
        SchedulableWork exist = workService.find(workId).orNull();
        if (exist == null) {
            return false;
        }
        if (Strings.isNullOrEmpty(exist.getPossessor())) {
            logger.error("should never happened: work {} was scheduled but no possessor", workId);
            return false;
        }
        if (!exist.getPossessor().equalsIgnoreCase(possessor)) {
            logger.error("should never happened: work {} was scheduled but error possessor", workId);
            return false;
        }
        exist.setFinished(true);
        workService.update(exist);
        return true;
    }

    public boolean releaseWork(String workId) {
        SchedulableWork work = workService.find(workId).orNull();
        if (work == null) {
            return false;
        }
        if (Strings.isNullOrEmpty(work.getPossessor())) {
            return false;
        }
        work.setRetry(work.getRetry() + 1);
        work.setPossessor(null);
        if (work.getRetry() >= work.getMaxRetries()) {
            // discarded
            work.setDiscarded(true);
        }
        workService.update(work);
        return true;
    }

    public boolean check(SchedulableWork work) {
        if (work == null) {
            return false;
        }
        if (work.getScheduledAt() <= 0) {
            return false;
        }
        if (Strings.isNullOrEmpty(work.getId()) || Strings.isNullOrEmpty(work.getType())) {
            return false;
        }
        if (work.isFinished()) {
            return false;
        }
        if (work.isDiscarded()) {
            return false;
        }
        if (work.getRetry() >= work.getMaxRetries()) {
            return false;
        }
        return true;
    }
}
