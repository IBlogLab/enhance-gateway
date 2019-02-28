package org.iblog.enhance.gateway.filter.logging;

import reactor.core.publisher.Mono;

import org.iblog.enhance.gateway.core.LoggingRecord;
import org.iblog.enhance.gateway.filter.InterfaceGlobalFilter;
import org.iblog.enhance.gateway.filter.MarkRequestFilter;
import org.iblog.enhance.gateway.filter.PriorityAccess;
import org.iblog.enhance.gateway.lifecycle.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import org.iblog.enhance.gateway.core.Error;

/**
 * Logging response attributions that filled after
 * {@link org.springframework.cloud.gateway.filter.NettyWriteResponseFilter}
 *
 * @author lance
 */
public class LoggingResponseStatusFilter extends InterfaceGlobalFilter {
    private static final Logger logger = LoggerFactory.getLogger(LoggingResponseStatusFilter.class);

    private final Config config;

    public LoggingResponseStatusFilter(Config config) {
        this.config = config;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String recordId = exchange.getAttribute(MarkRequestFilter.UNIQUE_KEY_MARK);
        logger.info("start logging status code for {}", recordId);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            HttpStatus statusCode = exchange.getResponse().getStatusCode();
            LoggingRecord record = new LoggingRecord();
            record.setId(recordId);
            record.setStatusCode(statusCode.value());
            record.setEndTime(clock.getTime());
            record.setError(errorFromStatus(statusCode));
            loggingLifecycleManager.submit(new Event(
                    Event.EventType.LOGGING_RESPONSE,
                    new LoggingDecorator.Builder<LoggingRecord>()
                            .setData(record)
                            .setRecordId(recordId)
                            .build(),
                    "logging response status"));
        }));
    }

    private Error errorFromStatus(HttpStatus status) {
        if (status.is2xxSuccessful()) {
            return Error.OK;
        }
        switch (status.value()) {
            case 401:
                return Error.UNAUTHORIZED;
            case 403:
                return Error.FORBIDDEN;
            case 404:
                return Error.DOWNSTREAM_SERVICE_NOT_FOUND;
            case 502:
            case 503:
            case 504:
                return Error.DOWNSTREAM_SERVER_AVAILABLE;
        }
        if (status.is4xxClientError()) {
            return Error.BAD_REQUEST;
        }
        if (status.is5xxServerError()) {
            return Error.DOWNSTREAM_SERVER_INTERNAL_ERROR;
        }
        return Error.OK;
    }

    @Override
    public int getOrder() {
        return config.getOrder() != 0 ? config.getOrder() :
                PriorityAccess.get(getClass().getName());
    }

    public static class Config {
        private String filterName = "loggingResponseStatusFilter";
        private boolean start = true;
        private int order;

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

        public String getFilterName() {
            return filterName;
        }

        public Config setFilterName(String filterName) {
            this.filterName = filterName;
            return this;
        }
    }
}
