package org.iblog.enhance.gateway.in;

import reactor.core.publisher.Mono;

import java.util.List;
import org.iblog.enhance.gateway.filter.FilterChainDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import com.google.common.collect.ImmutableList;

/**
 * @author chen.kuan
 * @date 2018-12-24 17:00
 */
public class HttpAcceptor implements Acceptor, GlobalFilter, Ordered {
	// spring boot auto configure on condition
	// by default evo-interface will support http in-binding
	// invoke filter chain dispatcher
    private static final Logger logger = LoggerFactory.getLogger(HttpAcceptor.class);

    public static final List<String> HTTP_SCHEME = ImmutableList.of("http", "https");

    private final Config config;
    private final FilterChainDispatcher dispatcher;

    public HttpAcceptor(Config config, FilterChainDispatcher dispatcher) {
        this.config = config;
        this.dispatcher = dispatcher;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!config.isStart()) {
            return chain.filter(exchange);
        }
        String scheme = exchange.getRequest().getURI().getScheme();
        if (!HTTP_SCHEME.contains(scheme)) {
            // Direct passthrough request if not an http scheme.
            return chain.filter(exchange);
        }
        return dispatcher.filter(exchange, chain);
    }

    @Override
    public int getOrder() {
        return config.getOrder();
    }

    public static class Config {
        /**
         * status flag of {@link Acceptor}
         * {@link HttpAcceptor} is open by default
         */
        private boolean start = true;
        /**
         * priority ranking
         * this filed config need refer Filters of spring cloud predefined.
         * e.g. {@link org.springframework.cloud.gateway.filter.AdaptCachedBodyGlobalFilter}
         */
        private int order = -3000;

        public boolean isStart() {
            return start;
        }

        public Config setStart(boolean start) {
            this.start = start;
            return this;
        }

        public int getOrder() {
            return order;
        }

        public Config setOrder(int order) {
            this.order = order;
            return this;
        }
    }
}
