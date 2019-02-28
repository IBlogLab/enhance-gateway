package org.iblog.enhance.gateway.filter.extract;

import reactor.core.publisher.Mono;

import org.iblog.enhance.gateway.filter.ModifyResponseBodyFilter;
import org.iblog.enhance.gateway.filter.PriorityAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author lance
 */
public class ExtractResponseBodyFilter extends ModifyResponseBodyFilter {
    private static final Logger logger = LoggerFactory.getLogger(ExtractResponseBodyFilter.class);

    public ExtractResponseBodyFilter(Config config) {
        super(config);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Extractor extractor = extractorWrapper.match(exchange, false).orNull();
        if (extractor == null) {
            return chain.filter(exchange);
        }
        String uniqueKey = getUniqueKey(exchange);
        logger.info("ExtractResponseBodyFilter start to filter request {}", uniqueKey);
        ServerHttpResponseDecorator decorate = decorate(exchange);
        return chain.filter(exchange.mutate().response(decorate).build());
    }

    @Override
    public int getOrder() {
        return config.getOrder() != 0 ? config.getOrder()
                : PriorityAccess.get(this.getClass().getName());
    }
}
