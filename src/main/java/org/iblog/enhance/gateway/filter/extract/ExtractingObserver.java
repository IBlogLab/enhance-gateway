package org.iblog.enhance.gateway.filter.extract;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.CollectionUtils;
import org.iblog.enhance.gateway.core.KeyWord;
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
public class ExtractingObserver implements IEventObserver {
    private static final Logger logger = LoggerFactory.getLogger(ExtractingObserver.class);

    private final int MAX_RETRY = 10;

    private final LoggingRecordService loggingRecordService;
    private final Clock clock = Clock.defaultClock();

    public ExtractingObserver(LoggingRecordService loggingRecordService) {
        this.loggingRecordService = loggingRecordService;
    }

    @Override
    public void observe(Event event) {
        if (event.type != Event.EventType.EXTRACTING_BODY) {
            logger.trace("debug log: observed a event {} beyond ExtractingObserver can do.",
                    ObjectMappers.mustWriteValue(event));
            return;
        }
        logger.info("ExtractingObserver do observe for event {} at {}",
                ObjectMappers.mustWriteValue(event), clock.getTime());
        List<KeyWord> keyWords = (List<KeyWord>) event.decorator.getData();
        if (CollectionUtils.isEmpty(keyWords)) {
            logger.warn("not excepted: event data is empty.");
            return;
        }
        GlobalExecutor.get().submit(() -> {
            long pause = Math.max(10, new Random().nextInt(100)), maxPause = 1000;
            for (int tries = 0; ; ++tries) {
                LoggingRecord exist = loggingRecordService.find(event.decorator.getRecordId()).orNull();
                if (exist == null) {
                    logger.info("try {}: not found record {}", tries, event.decorator.getRecordId());
                    Uninterruptibles.sleepUninterruptibly(pause, TimeUnit.MILLISECONDS);
                    pause = Math.min(pause << 1, maxPause);
                    if (tries > MAX_RETRY) {
                        logger.warn("ExtractingObserver losing tags for {}",
                                event.decorator.getRecordId());
                        break;
                    }
                    continue;
                }
                boolean updated = exist.updateTags(keyWords);
                if (!updated) {
                    break;
                }
                loggingRecordService.updateIf(exist);
                logger.info("ExtractingObserver retry {} to finish extracting event that message: {}", tries, event.message);
                break;
            }
        });
    }
}
