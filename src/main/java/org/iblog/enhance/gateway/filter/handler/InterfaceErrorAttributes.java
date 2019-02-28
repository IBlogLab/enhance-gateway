package org.iblog.enhance.gateway.filter.handler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.iblog.enhance.gateway.util.Clock;
import org.iblog.enhance.gateway.util.TimeFormatUtil;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import com.google.common.base.Strings;

/**
 * @author shaoxiao.xu
 * @date 2019/1/21 17:13
 */
@Component
public class InterfaceErrorAttributes implements ErrorAttributes {
    private static final String ERROR_ATTRIBUTE = InterfaceErrorAttributes.class.getName()
            + ".ERROR";

    private final boolean includeException;
    private final Clock clock = Clock.defaultClock();

    /**
     * Create a new {@link InterfaceErrorAttributes} instance that does not include the
     * "exception" attribute.
     */
    public InterfaceErrorAttributes() {
        this(false);
    }

    /**
     * Create a new {@link InterfaceErrorAttributes} instance.
     * @param includeException whether to include the "exception" attribute
     */
    public InterfaceErrorAttributes(boolean includeException) {
        this.includeException = includeException;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getErrorAttributes(
            ServerRequest request, boolean includeStackTrace) {
        Map<String, Object> errorAttributes = new LinkedHashMap<>();
        Throwable error = getError(request);
        HttpStatus errorStatus = determineHttpStatus(error);
        errorAttributes.put("success", false);
        errorAttributes.put("timestamp", TimeFormatUtil.timeInCST(clock.getTime()));
        errorAttributes.put("path", request.path());
        errorAttributes.put("code", errorStatus.value());
        errorAttributes.put("error", errorStatus.getReasonPhrase());
        errorAttributes.put("message", determineMessage(error));
        handleException(errorAttributes, determineException(error), includeStackTrace);
        return errorAttributes;
    }

    private HttpStatus determineHttpStatus(Throwable error) {
        if (error instanceof ResponseStatusException) {
            return ((ResponseStatusException) error).getStatus();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String determineMessage(Throwable error) {
        if (error instanceof WebExchangeBindException) {
            return error.getMessage();
        }
        if (error instanceof ResponseStatusException) {
            return ((ResponseStatusException) error).getReason();
        }
        if (!Strings.isNullOrEmpty(error.getMessage())) {
            return error.getMessage();
        }
        if (error.getSuppressed() != null && error.getSuppressed().length != 0) {
            return error.getSuppressed()[0].getMessage();
        }
        return null;
    }

    private Throwable determineException(Throwable error) {
        if (error instanceof ResponseStatusException) {
            return (error.getCause() != null) ? error.getCause() : error;
        }
        return error;
    }

    private void addStackTrace(Map<String, Object> errorAttributes, Throwable error) {
        StringWriter stackTrace = new StringWriter();
        error.printStackTrace(new PrintWriter(stackTrace));
        stackTrace.flush();
        errorAttributes.put("trace", stackTrace.toString());
    }

    private void handleException(Map<String, Object> errorAttributes, Throwable error,
                                 boolean includeStackTrace) {
        if (this.includeException) {
            errorAttributes.put("exception", error.getClass().getName());
        }
        if (includeStackTrace) {
            addStackTrace(errorAttributes, error);
        }
        if (error instanceof BindingResult) {
            BindingResult result = (BindingResult) error;
            if (result.getErrorCount() > 0) {
                errorAttributes.put("errors", result.getAllErrors());
            }
        }
    }

    @Override
    public Throwable getError(ServerRequest request) {
        return (Throwable) request.attribute(ERROR_ATTRIBUTE)
                .orElseThrow(() -> new IllegalStateException(
                        "Missing exception attribute in ServerWebExchange"));
    }

    @Override
    public void storeErrorInformation(Throwable error, ServerWebExchange exchange) {
        exchange.getAttributes().putIfAbsent(ERROR_ATTRIBUTE, error);
    }
}
