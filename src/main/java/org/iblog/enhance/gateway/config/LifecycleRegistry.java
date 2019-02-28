package org.iblog.enhance.gateway.config;

import javax.annotation.Resource;
import org.iblog.enhance.gateway.filter.convert.LoggingConvertedBodyObserver;
import org.iblog.enhance.gateway.filter.extract.ExtractingObserver;
import org.iblog.enhance.gateway.filter.logging.LoggingBodyObserver;
import org.iblog.enhance.gateway.filter.logging.LoggingRequestObserver;
import org.iblog.enhance.gateway.filter.logging.LoggingResponseObserver;
import org.iblog.enhance.gateway.lifecycle.LoggingLifecycleManager;
import org.iblog.enhance.gateway.service.LoggingRecordService;
import org.iblog.enhance.gateway.service.OpenApiService;
import org.iblog.enhance.gateway.util.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lance
 */
@Configuration
public class LifecycleRegistry implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(LifecycleRegistry.class);

    private final Clock clock = Clock.defaultClock();

    @Resource
    private LoggingLifecycleManager loggingLifecycleManager;
    @Resource
    private OpenApiService openApiService;
    @Resource
    private LoggingRecordService loggingRecordService;

    @Override
    public void afterPropertiesSet() throws Exception {
        registerLoggingRequestObserver();
        registerLoggingBodyObserver();
        registerLoggingResponseObserver();
        registerExtractingObserver();
        registerLoggingConvertedBodyObserver();
    }

    private void registerLoggingBodyObserver() {
        LoggingBodyObserver loggingBodyObserver = new LoggingBodyObserver(loggingRecordService);
        loggingLifecycleManager.registerObserver(loggingBodyObserver);
        logger.info("finish registering LoggingBodyObserver at {}", clock.getTime());
    }

    private void registerLoggingRequestObserver() {
        LoggingRequestObserver loggingRequestObserver = new LoggingRequestObserver(
                openApiService, loggingRecordService);
        loggingLifecycleManager.registerObserver(loggingRequestObserver);
        logger.info("finish registering LoggingRequestObserver at {}", clock.getTime());
    }

    private void registerLoggingResponseObserver() {
        LoggingResponseObserver loggingResponseObserver = new LoggingResponseObserver(
                loggingRecordService);
        loggingLifecycleManager.registerObserver(loggingResponseObserver);
        logger.info("finish registering LoggingResponseObserver at {}", clock.getTime());
    }

    private void registerExtractingObserver() {
        ExtractingObserver extractingObserver = new ExtractingObserver(loggingRecordService);
        loggingLifecycleManager.registerObserver(extractingObserver);
        logger.info("finish registering ExtractingObserver at {}", clock.getTime());
    }

    private void registerLoggingConvertedBodyObserver() {
        LoggingConvertedBodyObserver loggingConvertedBodyObserver =
                new LoggingConvertedBodyObserver(loggingRecordService);
        loggingLifecycleManager.registerObserver(loggingConvertedBodyObserver);
        logger.info("finish registering LoggingConvertedBodyObserver at {}", clock.getTime());
    }
}
