package org.iblog.enhance.gateway.filter.logging;

import reactor.core.publisher.Mono;

import org.iblog.enhance.gateway.core.LoggingRecord;
import org.iblog.enhance.gateway.core.RequestType;
import org.iblog.enhance.gateway.filter.ModifyRequestBodyFilter;
import org.iblog.enhance.gateway.filter.PriorityAccess;
import org.iblog.enhance.gateway.lifecycle.event.Event;
import org.iblog.enhance.gateway.util.ObjectMappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author shaoxiao.xu
 * @date 2018/12/25 10:48
 */
public class LoggingRequestBodyFilter extends ModifyRequestBodyFilter {
    private static final Logger logger = LoggerFactory.getLogger(LoggingRequestBodyFilter.class);

    public LoggingRequestBodyFilter(Config config) {
        super(config);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!config.isStart()) {
            return chain.filter(exchange);
        }
        String uniqueKey = getUniqueKey(exchange);
        logger.info("LoggingRequestBodyFilter start to filter request {}", uniqueKey);

        // publish event
        LoggingRecord record = initLoggingRecord(exchange);
        logger.info("init record {}", ObjectMappers.mustWriteValue(record));
        loggingLifecycleManager.submit(new Event(
                Event.EventType.LOGGING_REQUEST,
                new LoggingDecorator.Builder<LoggingRecord>()
                        .setRecordId(record.getId())
                        .setData(record)
                        .setFrom(LoggingDecorator.BodyFrom.REQUEST)
                        .setType(RequestType.HTTP)
                        .build(),
                "logging request"));

        if (!checkContentLength(exchange)) {
            logger.info("LoggingRequestBodyFilter skip logging request {} body: body is empty", uniqueKey);
            return chain.filter(exchange);
        }
        return super.decorate(exchange, chain);
    }

    @Override
    public int getOrder() {
        return config.getOrder() != 0 ? config.getOrder()
                : PriorityAccess.get(this.getClass().getName());
    }

}
