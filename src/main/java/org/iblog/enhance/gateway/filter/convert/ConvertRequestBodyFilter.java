package org.iblog.enhance.gateway.filter.convert;

import reactor.core.publisher.Mono;

import org.iblog.enhance.gateway.filter.ModifyRequestBodyFilter;
import org.iblog.enhance.gateway.filter.PriorityAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author lance
 */
public class ConvertRequestBodyFilter extends ModifyRequestBodyFilter {
    private static final Logger logger = LoggerFactory.getLogger(ConvertRequestBodyFilter.class);

    public ConvertRequestBodyFilter(Config config) {
        super(config);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String uniqueKey = getUniqueKey(exchange);
        if (!checkContentLength(exchange)) {
            logger.info("ConvertRequestBodyFilter skip converting request {} body: body is empty", uniqueKey);
            return chain.filter(exchange);
        }
        Converter converter = converterWrapper.match(exchange, true).orNull();
        if (converter == null) {
            return chain.filter(exchange);
        }
        logger.info("ConvertRequestBodyFilter start to filter request {}", uniqueKey);
        return decorate(exchange, chain);
    }

    @Override
    public int getOrder() {
        return config.getOrder() != 0 ? config.getOrder()
                : PriorityAccess.get(this.getClass().getName());
    }
}
