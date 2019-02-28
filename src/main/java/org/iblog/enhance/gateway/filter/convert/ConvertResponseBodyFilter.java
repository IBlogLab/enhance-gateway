package org.iblog.enhance.gateway.filter.convert;

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
public class ConvertResponseBodyFilter extends ModifyResponseBodyFilter {
    private static final Logger logger = LoggerFactory.getLogger(ConvertResponseBodyFilter.class);

    public ConvertResponseBodyFilter(Config config) {
        super(config);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Converter converter = converterWrapper.match(exchange, false).orNull();
        if (converter == null) {
            return chain.filter(exchange);
        }
        String uniqueKey = getUniqueKey(exchange);
        logger.info("ConvertResponseBodyFilter start to filter request {}", uniqueKey);
        ServerHttpResponseDecorator decorate = decorate(exchange);
        return chain.filter(exchange.mutate().response(decorate).build());
    }

    @Override
    public int getOrder() {
        return config.getOrder() != 0 ? config.getOrder()
                : PriorityAccess.get(this.getClass().getName());
    }
}
