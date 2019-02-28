package org.iblog.enhance.gateway.filter.logging;

import org.iblog.enhance.gateway.core.RequestType;
import org.iblog.enhance.gateway.filter.MarkRequestFilter;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author lance
 */
public class LoggingDecorator<T> {
    public enum BodyFrom {
        REQUEST,
        RESPONSE
    }

    private final String recordId;
    private final T data;
    private final RequestType type;
    private final BodyFrom from;

    public LoggingDecorator(String recordId, T data, RequestType type, BodyFrom from) {
        this.recordId = recordId;
        this.data = data;
        this.type = type;
        this.from = from;
    }

    public static class Builder<T> {
        private String recordId;
        private T data;
        private RequestType type;
        private BodyFrom from;

        @SuppressWarnings("unchecked")
        public LoggingDecorator build() {
            return new LoggingDecorator(this.recordId, this.data, this.type, this.from);
        }

        /**
         * this is a dangerous approach, use with care and dont't modify.
         * @param exchange
         * @return
         */
        public Builder setServerWebExchange(ServerWebExchange exchange) {
            this.recordId = exchange.getAttribute(MarkRequestFilter.UNIQUE_KEY_MARK);
            this.type = exchange.getAttribute(MarkRequestFilter.REQUEST_TYPE_MARK);
            return this;
        }

        public Builder setRecordId(String recordId) {
            this.recordId = recordId;
            return this;
        }

        public Builder setType(RequestType type) {
            this.type = type;
            return this;
        }

        public Builder setData(T data) {
            this.data = data;
            return this;
        }

        public Builder setFrom(BodyFrom from) {
            this.from = from;
            return this;
        }
    }

    public String getRecordId() {
        return recordId;
    }

    public T getData() {
        return data;
    }

    public RequestType getType() {
        return type;
    }

    public BodyFrom getFrom() {
        return from;
    }
}
