package org.iblog.enhance.gateway.filter.logging;

import reactor.core.publisher.Mono;

import org.iblog.enhance.gateway.core.LoggingRecord;
import org.iblog.enhance.gateway.filter.ModifyResponseBodyFilter;
import org.iblog.enhance.gateway.filter.PriorityAccess;
import org.iblog.enhance.gateway.lifecycle.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author lance
 */
public class LoggingResponseBodyFilter extends ModifyResponseBodyFilter {
    private static final Logger logger = LoggerFactory.getLogger(LoggingResponseBodyFilter.class);

    public LoggingResponseBodyFilter(Config config) {
        super(config);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String uniqueKey = getUniqueKey(exchange);
        logger.info("LoggingResponseBodyFilter start to filter request {}", uniqueKey);

        ServerHttpResponseDecorator responseDecorator = decorate(exchange);

        LoggingRecord record = loggingResponse(exchange);
        loggingLifecycleManager.submit(new Event(
                Event.EventType.LOGGING_RESPONSE,
                new LoggingDecorator.Builder<LoggingRecord>()
                        .setServerWebExchange(exchange)
                        .setFrom(LoggingDecorator.BodyFrom.RESPONSE)
                        .setData(record)
                        .build(),
                "update logging record"));

        return chain.filter(exchange.mutate().response(responseDecorator).build());
    }

    @Override
    public int getOrder() {
        return config.getOrder() != 0 ? config.getOrder()
                : PriorityAccess.get(this.getClass().getName());
    }
}
