package org.iblog.enhance.gateway.filter.handler;

import reactor.core.publisher.Mono;

import java.util.Map;
import org.iblog.enhance.gateway.core.LoggingRecord;
import org.iblog.enhance.gateway.filter.MarkRequestFilter;
import org.iblog.enhance.gateway.filter.logging.LoggingDecorator;
import org.iblog.enhance.gateway.lifecycle.LoggingLifecycleManager;
import org.iblog.enhance.gateway.lifecycle.event.Event;
import org.iblog.enhance.gateway.service.OpenApiService;
import org.iblog.enhance.gateway.util.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.iblog.enhance.gateway.core.Error;

/**
 * @author lance
 */
public class InterfaceExceptionHandler extends DefaultErrorWebExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(InterfaceExceptionHandler.class);

    @Autowired
    private LoggingLifecycleManager loggingLifecycleManager;
    private final OpenApiService openApiService;
    private final Clock clock = Clock.defaultClock();

    public InterfaceExceptionHandler(
            ErrorAttributes errorAttributes,
            ResourceProperties resourceProperties,
            ErrorProperties errorProperties,
            ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, errorProperties, applicationContext);
        openApiService = applicationContext.getBean(OpenApiService.class);
    }

    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
        return super.getErrorAttributes(request, includeStackTrace);
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpRequest request = exchange.getRequest();
        logger.error("InterfaceExceptionHandler request: {} | {}, error: {}",
                request.getURI().toString(), request.getMethod(), ex.getMessage());
        if (ex instanceof NotFoundException || ex instanceof ResponseStatusException) {
            // path not found,
            // the request was not processed, the filters not apply
            loggingNFE(exchange);
        }
        if (ex instanceof NullPointerException) {
            // downstream services are not available
            // can logging the request error type
            loggingNPE(exchange);
        }
        if (ex instanceof RuntimeException) {
            loggingRTE(exchange);
        }
        return super.handle(exchange, ex);
    }

    @Override
    protected HttpStatus getHttpStatus(Map<String, Object> errorAttributes) {
        int statusCode = (int) errorAttributes.get("code");
        return HttpStatus.valueOf(statusCode);
    }

    private void loggingNFE(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();
        String ip = request.getRemoteAddress().getAddress().getHostAddress();
        logger.warn("evo-interface received unknown request {} | {} from {}",
                path, method, ip);
        boolean exist = openApiService.exist(path, method.toString());
        if (exist) {
            logger.error("important reminder: exist api {} | {} from {} "
                    + "can't be processed normally.", path, method, ip);
        }
    }

    private void loggingNPE(ServerWebExchange exchange) {
        String uniqueKey = exchange.getAttribute(MarkRequestFilter.UNIQUE_KEY_MARK);
        LoggingRecord record = new LoggingRecord();
        record.setId(uniqueKey);
        record.setStatusCode(HttpStatus.BAD_GATEWAY.value());
        record.setError(Error.DOWNSTREAM_SERVER_AVAILABLE);
        record.setEndTime(clock.getTime());
        loggingLifecycleManager.submit(new Event(
                Event.EventType.LOGGING_RESPONSE,
                new LoggingDecorator.Builder<LoggingRecord>()
                        .setData(record)
                        .setRecordId(uniqueKey)
                        .build(),
                "logging response status by global exception handler."));
        logger.info("updated {} that handle error {}", uniqueKey, Error.DOWNSTREAM_SERVER_AVAILABLE);
    }

    private void loggingRTE(ServerWebExchange exchange) {
        String uniqueKey = exchange.getAttribute(MarkRequestFilter.UNIQUE_KEY_MARK);
        LoggingRecord record = new LoggingRecord();
        record.setId(uniqueKey);
        record.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        record.setError(Error.DOWNSTREAM_SERVER_INTERNAL_ERROR);
        record.setEndTime(clock.getTime());
        loggingLifecycleManager.submit(new Event(
                Event.EventType.LOGGING_RESPONSE,
                new LoggingDecorator.Builder<LoggingRecord>()
                        .setData(record)
                        .setRecordId(uniqueKey)
                        .build(),
                "logging response status by global exception handler."));
        logger.info("updated {} that handle error {}", uniqueKey, Error.DOWNSTREAM_SERVER_INTERNAL_ERROR);
    }
}
