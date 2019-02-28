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
import com.google.common.util.concurrent.Uninterruptibles;

/**
 * @author lance
 */
public class LoggingBodyObserver implements IEventObserver {
    private static final Logger logger = LoggerFactory.getLogger(LoggingBodyObserver.class);
    private final Event.EventType CAPACITY_MARK = Event.EventType.LOGGING_BODY;
    private final int MAX_RETRY = 30;

    private final LoggingRecordService loggingRecordService;
    private final Clock clock = Clock.defaultClock();

    public LoggingBodyObserver(LoggingRecordService loggingRecordService) {
        this.loggingRecordService = loggingRecordService;
    }

    @Override
    public void observe(Event event) {
        if (CAPACITY_MARK != event.type) {
            logger.trace("debug log: observed a event {} beyond LoggingBodyObserver can do.",
                    ObjectMappers.mustWriteValue(event));
            return;
        }
        GlobalExecutor.get().submit(() -> {
            logger.info("LoggingBodyObserver do observe for event {} at {}",
                    ObjectMappers.mustWriteValue(event), clock.getTime());
            long pause = Math.max(10, new Random().nextInt(100)), maxPause = 1000;
            for (int tries = 0; ; ++tries) {
                LoggingRecord exit = loggingRecordService.find(event.decorator.getRecordId()).orNull();
                if (exit == null) {
                    logger.info("try {}: not found record {}", tries, event.decorator.getRecordId());
                    Uninterruptibles.sleepUninterruptibly(pause, TimeUnit.MILLISECONDS);
                    pause = Math.min(pause << 1, maxPause);
                    if (tries > MAX_RETRY) {
                        logger.warn("LoggingBodyObserver losing request body for {}",
                                event.decorator.getRecordId());
                        break;
                    }
                    continue;
                }
                if (event.decorator.getFrom() == LoggingDecorator.BodyFrom.REQUEST) {
                    loggingRequest(exit, (String) event.decorator.getData());
                } else {
                    loggingResponse(exit, (String) event.decorator.getData());
                }
                loggingRecordService.updateIf(exit);
                logger.info("LoggingBodyObserver retry {} to finish logging event that message: {}", tries, event.message);
                break;
            }
        });
    }

    private void loggingRequest(LoggingRecord record, String body) {
        logger.info("logging request {} body {}", record.getId(), body);
        record.setRequestBody(body);
    }

    private void loggingResponse(LoggingRecord record, String body) {
        logger.info("logging response {} body {}", record.getId(), body);
        record.setResponseBody(body);
    }
}
