package org.iblog.enhance.gateway.filter;

import java.util.Map;
import org.iblog.enhance.gateway.core.LoggingRecord;
import org.iblog.enhance.gateway.exception.DataFormatException;
import org.iblog.enhance.gateway.filter.convert.ConverterWrapper;
import org.iblog.enhance.gateway.filter.extract.ExtractorWrapper;
import org.iblog.enhance.gateway.lifecycle.LoggingLifecycleManager;
import org.iblog.enhance.gateway.lifecycle.sched.XTaskScheduler;
import org.iblog.enhance.gateway.util.Clock;
import org.iblog.enhance.gateway.util.ObjectMappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.iblog.enhance.gateway.core.Error;

/**
 * @author chen.kuan
 * @date 2018-12-24 17:04
 */
public abstract class InterfaceGlobalFilter implements GlobalFilter, Ordered {
    protected final Clock clock = Clock.defaultClock();

    @Autowired
    protected LoggingLifecycleManager loggingLifecycleManager;
    @Autowired
    protected ConverterWrapper converterWrapper;
    @Autowired
    protected ExtractorWrapper extractorWrapper;
    @Autowired
    protected XTaskScheduler taskScheduler;

    protected LoggingRecord initLoggingRecord(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        LoggingRecord record = new LoggingRecord();
        record.setId(exchange.getAttribute(MarkRequestFilter.UNIQUE_KEY_MARK));
        record.setInterrelatedId(request.getHeaders().getFirst(MarkRequestFilter.INTERRELATED_ID));
        Long start = exchange.getAttribute(MarkRequestFilter.REQUEST_START_TIME);
        record.setStartTime(start == null ? 0 : start);
        record.setClientId(request.getHeaders().getFirst(MarkRequestFilter.X_Session_User));
        record.setSecretKey(request.getHeaders().getFirst(MarkRequestFilter.X_Session_Secret));
        record.setWarehouse(request.getHeaders().getFirst(MarkRequestFilter.X_Session_Warehouse));
        record.setAsync(Boolean.parseBoolean(
                exchange.getRequest().getHeaders().getFirst(MarkRequestFilter.X_Task_Async)));
        record.setUri(request.getURI().getPath());
        record.setMethod(request.getMethod());
        record.setRequestType(exchange.getAttribute(MarkRequestFilter.REQUEST_TYPE_MARK));
        if (request.getRemoteAddress() != null && request.getRemoteAddress().getAddress() != null) {
            record.setIp(request.getRemoteAddress().getAddress().getHostAddress());
        }
        record.setRequestHeaders(transit(request.getHeaders()));
        record.setQueries(transit(request.getQueryParams()));
        record.setUrl(request.getURI().toString());
        record.setError(Error.OK);
        record.setCreatedBy(MarkRequestFilter.DEFAULT_CREATED_BY);
        return record;
    }

    protected LoggingRecord loggingResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        LoggingRecord record = new LoggingRecord();
        record.setId(exchange.getAttribute(MarkRequestFilter.UNIQUE_KEY_MARK));
        record.setResponseHeaders(transit(response.getHeaders()));
        return record;
    }

    protected String getUniqueKey(ServerWebExchange exchange) {
        return exchange.getAttribute(MarkRequestFilter.UNIQUE_KEY_MARK);
    }

    private String transit(Object obj) {
        if (obj == null) {
            return null;
        }
        if (!(obj instanceof Map)) {
            throw new DataFormatException("error input");
        }
        return ObjectMappers.mustWriteValue(obj);
    }

}
