package org.iblog.enhance.gateway.filter;

import reactor.core.publisher.Mono;

import org.iblog.enhance.gateway.core.RequestType;
import org.iblog.enhance.gateway.util.Clock;
import org.iblog.enhance.gateway.util.LoggingUUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import static org.iblog.enhance.gateway.in.HttpAcceptor.HTTP_SCHEME;

/**
 * @author shaoxiao.xu
 * @date 2018/12/25 14:45
 */
public class MarkRequestFilter implements GlobalFilter, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(MarkRequestFilter.class);

    public static final String REQUEST_START_TIME = "REQUEST_START_TIME";
    public static final String UNIQUE_KEY_MARK = "REQUEST_UNIQUE_MARK";
    public static final String REQUEST_TYPE_MARK = "REQUEST_TYPE_MARK";
    public static final String ASYNC_REQUEST_MARK = "async_request_mark";
    public static final String X_Session_User = "X-Session-User";
    public static final String X_Session_Secret = "X-Session-Secret";
    public static final String X_Session_Warehouse = "X-Session-Warehouse";
    public static final String X_Task_Async = "X_Task_Async";
    public static final String DEFAULT_CREATED_BY = "evo-interface";
    public static final String INTERRELATED_ID = "interrelated-id";

    private final Clock clock = Clock.defaultClock();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String uniqueKey = LoggingUUIDUtil.generateUUID(exchange);
        String scheme = exchange.getRequest().getURI().getScheme();
        long current = clock.getTime();
        logger.info("MarkRequestFilter generate unique key for request {}/{} at {}",
                uniqueKey, scheme, current);
        exchange.getAttributes().put(REQUEST_START_TIME, current);
        exchange.getAttributes().put(UNIQUE_KEY_MARK, uniqueKey);
        if (HTTP_SCHEME.contains(scheme)) {
            exchange.getAttributes().put(REQUEST_TYPE_MARK, RequestType.HTTP);
        } else {
            exchange.getAttributes().put(REQUEST_TYPE_MARK, RequestType.UNKNOWN);
        }
        observeRequest(exchange);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return PriorityAccess.get(this.getClass().getName());
    }

    private void observeRequest(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        boolean async = Boolean.valueOf(request.getHeaders().getFirst(X_Task_Async));
        if (async) {
            logger.info("received a async request {}",
                    (String) exchange.getAttribute(UNIQUE_KEY_MARK));
            exchange.getAttributes().put(ASYNC_REQUEST_MARK, true);
        } else {
            exchange.getAttributes().put(ASYNC_REQUEST_MARK, false);
        }
    }
}
