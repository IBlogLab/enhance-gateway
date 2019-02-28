package org.iblog.enhance.gateway.filter.async;

import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.iblog.enhance.gateway.core.Result;
import org.iblog.enhance.gateway.filter.InterfaceGlobalFilter;
import org.iblog.enhance.gateway.filter.MarkRequestFilter;
import org.iblog.enhance.gateway.filter.PriorityAccess;
import org.iblog.enhance.gateway.util.ObjectMappers;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import com.google.common.collect.Maps;

/**
 * @author lance
 */
public class AsyncProcessingFilter extends InterfaceGlobalFilter {
    private final Config config;

    public AsyncProcessingFilter(Config config) {
        this.config = config;
    }

    @SuppressWarnings({"unchecked", "Duplicates"})
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        boolean async = exchange.getAttribute(MarkRequestFilter.ASYNC_REQUEST_MARK);
        if (!async) {
            // noting done
            return chain.filter(exchange);
        }

        taskScheduler.scheduleWithFixedDelay(
                TimeUnit.SECONDS.toMillis(10), TimeUnit.SECONDS.toMillis(10),
                "internal_async_schedulable",
                exchange.getAttribute(MarkRequestFilter.UNIQUE_KEY_MARK),
                0, 0);
        return buildAsyncResult(exchange, buildMessage());
    }

    private String buildMessage() {
        return ObjectMappers.mustWriteValue(
                Result.build("200", "异步请求任务已被成功接收", "success", true));
    }

    private Mono<Void> buildAsyncResult(ServerWebExchange exchange, String message) {
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(message.getBytes(StandardCharsets.UTF_8));
        Map<String, String> header = Maps.newHashMap();
        header.put("Content-Type", "application/json; charset=utf-8");
        exchange.getResponse().getHeaders().setAll(header);
        exchange.getResponse().setStatusCode(HttpStatus.OK);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return config.getOrder() != 0 ? config.getOrder()
                : PriorityAccess.get(this.getClass().getName());
    }

    public static class Config {
        private String name = AsyncProcessingFilter.class.getSimpleName();
        private int order;

        public String getName() {
            return name;
        }

        public Config setName(String name) {
            this.name = name;
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
