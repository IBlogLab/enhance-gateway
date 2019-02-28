package org.iblog.enhance.gateway.filter.logging;

import org.iblog.enhance.gateway.core.LoggingRecord;
import org.iblog.enhance.gateway.core.OpenApi;
import org.iblog.enhance.gateway.lifecycle.event.Event;
import org.iblog.enhance.gateway.lifecycle.event.IEventObserver;
import org.iblog.enhance.gateway.lifecycle.sched.GlobalExecutor;
import org.iblog.enhance.gateway.service.LoggingRecordService;
import org.iblog.enhance.gateway.service.OpenApiService;
import org.iblog.enhance.gateway.util.Clock;
import org.iblog.enhance.gateway.util.ObjectMappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Strings;

/**
 * @author lance
 */
public class LoggingRequestObserver implements IEventObserver {
    private static final Logger logger = LoggerFactory.getLogger(LoggingRequestObserver.class);

    private final Event.EventType CAPACITY_MARK = Event.EventType.LOGGING_REQUEST;

    private final OpenApiService openApiService;
    private final LoggingRecordService loggingRecordService;
    private final Clock clock = Clock.defaultClock();

    public LoggingRequestObserver(
            OpenApiService openApiService,
            LoggingRecordService loggingRecordService) {
        this.openApiService = openApiService;
        this.loggingRecordService = loggingRecordService;
    }

    @Override
    public void observe(Event event) {
        if (CAPACITY_MARK != event.type) {
            logger.trace("debug log: observed a event {} beyond LoggingRequestObserver can do.",
                    ObjectMappers.mustWriteValue(event));
            return;
        }
        GlobalExecutor.get().submit(() -> {
            LoggingRecord record = (LoggingRecord) event.decorator.getData();
            if (Strings.isNullOrEmpty(record.getInterrelatedId())) {
                OpenApi openApi = openApiService.find(record.getUri(), record.getMethod().toString()).orNull();
                if (openApi != null) {
                    record.setApiCode(openApi.getApiCode());
                    record.setBusinessType(openApi.getBusinessType());
                    record.setFrom(openApi.getFrom());
                    record.setTo(openApi.getTo());
                    record.setExternalized(true);
                } else {
                    logger.error("No object was matched of {} / {}", record.getUri(), record.getMethod());
                }
            } else {
                LoggingRecord interrelated = loggingRecordService.find(record.getInterrelatedId()).orNull();
                if (interrelated == null) {
                    logger.error("Not found interrelated record {} of {}",
                            record.getInterrelatedId(), record.getId());
                } else {
                    record.setApiCode(interrelated.getApiCode());
                    record.setBusinessType(interrelated.getBusinessType());
                    record.setFrom(interrelated.getFrom());
                    record.setTo(interrelated.getTo());
                    record.setExternalized(interrelated.isExternalized());
                }
            }
            loggingRecordService.create(record);
            logger.info("LoggingRequestObserver logging record {} / {} at {}",
                    record.getId(), event.message, clock.getTime());
        });
    }
}
