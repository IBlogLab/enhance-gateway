package org.iblog.enhance.gateway.filter.logging;

import reactor.core.publisher.Mono;

import java.net.URI;
import org.iblog.enhance.gateway.core.LoggingRecord;
import org.iblog.enhance.gateway.filter.InterfaceGlobalFilter;
import org.iblog.enhance.gateway.filter.MarkRequestFilter;
import org.iblog.enhance.gateway.lifecycle.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.LoadBalancerClientFilter;
import org.springframework.web.server.ServerWebExchange;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

/**
 * @author lance
 */
public class LoggingRealPathFilter extends InterfaceGlobalFilter {
    private static final Logger logger = LoggerFactory.getLogger(LoggingRealPathFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String recordId = (String) exchange.getAttributes().get(MarkRequestFilter.UNIQUE_KEY_MARK);

        URI requestUrl = (URI) exchange.getAttributes().get(GATEWAY_REQUEST_URL_ATTR);
        if (requestUrl != null) {
            logger.info("logging real url {} for {} after LoadBalancerClientFilter",
                    recordId, requestUrl.toString());
            LoggingRecord record = new LoggingRecord();
            record.setId(recordId);
            record.setRealUrl(requestUrl.toString());
            loggingLifecycleManager.submit(new Event(
                    Event.EventType.LOGGING_RESPONSE,
                    new LoggingDecorator.Builder<LoggingRecord>()
                            .setData(record)
                            .setRecordId(recordId)
                            .build(),
                    "logging request real url"));
        }
        return chain.filter(exchange);
    }

    /**
     * after {@link LoadBalancerClientFilter}
     * @return
     */
    @Override
    public int getOrder() {
        return LoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER + 1;
    }
}
