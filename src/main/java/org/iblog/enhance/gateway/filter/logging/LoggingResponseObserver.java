package org.iblog.enhance.gateway.filter.logging;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.iblog.enhance.gateway.core.LoggingRecord;
import org.iblog.enhance.gateway.lifecycle.event.Event;
import org.iblog.enhance.gateway.lifecycle.event.IEventObserver;
import org.iblog.enhance.gateway.lifecycle.sched.GlobalExecutor;
import org.iblog.enhance.gateway.service.LoggingRecordService;
import org.iblog.enhance.gateway.util.Clock;
import org.iblog.enhance.gateway.util.ObjectMappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;

public class LoggingResponseObserver implements IEventObserver {
    private static final Logger logger = LoggerFactory.getLogger(LoggingResponseObserver.class);

    private final Event.EventType CAPACITY_MARK = Event.EventType.LOGGING_RESPONSE;
    private final int MAX_RETRY = 30;

    private final LoggingRecordService loggingRecordService;
    private final Clock clock = Clock.defaultClock();

    public LoggingResponseObserver(LoggingRecordService loggingRecordService) {
        this.loggingRecordService = loggingRecordService;
    }
    @Override
    public void observe(Event event) {
        if (CAPACITY_MARK != event.type) {
            logger.trace("debug log: observed a event {} beyond LoggingResponseObserver can do.",
                    ObjectMappers.mustWriteValue(event));
            return;
        }
        GlobalExecutor.get().submit(() -> {
            String recordId = event.decorator.getRecordId();
            if (Strings.isNullOrEmpty(recordId)) {
                logger.error("not excepted: event decorator record id is null");
                return;
            }
            long pause = Math.max(10, new Random().nextInt(100)), maxPause = 1000;
            for (int tries = 0; ; ++tries) {
                LoggingRecord exist = loggingRecordService.find(recordId).orNull();
                if (exist == null) {
                    logger.info("try {}: not found record {}", tries, recordId);
                    Uninterruptibles.sleepUninterruptibly(pause, TimeUnit.MILLISECONDS);
                    pause = Math.min(pause << 1, maxPause);
                    if (tries > MAX_RETRY) {
                        logger.warn("LoggingResponseObserver losing response body for {}",
                                event.decorator.getRecordId());
                        break;
                    }
                    continue;
                }
                LoggingRecord update = (LoggingRecord) event.decorator.getData();
                if (update == null) {
                    logger.error("not excepted: event data is null");
                    return;
                }
                if (!exist.updateFrom(update)) {
                    logger.info("no fields were updated of {}", recordId);
                    return;
                }
                loggingRecordService.updateIf(exist);
                logger.info("LoggingResponseObserver retry {} to finish logging response of {} / {} at {}",
                        tries, recordId, event.message, clock.getTime());
                break;
            }
        });
    }
}
