package org.iblog.enhance.gateway.filter;

import reactor.core.publisher.Mono;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.web.server.ServerWebExchange;
import com.google.common.collect.Lists;

/**
 * @author lance
 */
public class FilterChainDispatcher {
	// entrance of spring filter chain
	// this class starts spring filter chain
    private static final Logger logger = LoggerFactory.getLogger(FilterChainDispatcher.class);

    private final List<GlobalFilter> globalFilters = Lists.newArrayList();

    public void register(GlobalFilter filter) {
        this.globalFilters.add(filter);
    }

    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        for (int i = 0; i < globalFilters.size() - 1; i++) {
            globalFilters.get(i).filter(exchange, chain);
        }
        return globalFilters.get(globalFilters.size() - 1).filter(exchange, chain);
    }
}
