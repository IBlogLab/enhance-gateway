package org.iblog.enhance.gateway.filter.extract;

import reactor.core.publisher.Mono;

import org.iblog.enhance.gateway.filter.ModifyRequestBodyFilter;
import org.iblog.enhance.gateway.filter.PriorityAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author shaoxiao.xu
 * @date 2019/1/14 17:40
 */
public class ExtractRequestBodyFilter extends ModifyRequestBodyFilter {
    private static final Logger logger = LoggerFactory.getLogger(ExtractRequestBodyFilter.class);

    public ExtractRequestBodyFilter(Config config) {
        super(config);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String uniqueKey = getUniqueKey(exchange);
        if (!checkContentLength(exchange)) {
            logger.info("ExtractRequestBodyFilter skip extracting request {} body: body is empty", uniqueKey);
            return chain.filter(exchange);
        }
        Extractor extractor = extractorWrapper.match(exchange, true).orNull();
        if (extractor == null) {
            return chain.filter(exchange);
        }
        logger.info("ExtractRequestBodyFilter start to filter request {}", uniqueKey);
        return decorate(exchange, chain);
    }

    @Override
    public int getOrder() {
        return config.getOrder() != 0 ? config.getOrder()
                : PriorityAccess.get(this.getClass().getName());
    }
}
