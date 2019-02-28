package org.iblog.enhance.gateway.config;

import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import org.iblog.enhance.gateway.core.KeyWord;
import org.iblog.enhance.gateway.filter.Config;
import org.iblog.enhance.gateway.filter.InterfaceGlobalFilter;
import org.iblog.enhance.gateway.filter.MarkRequestFilter;
import org.iblog.enhance.gateway.filter.ModifyRequestBodyFilter;
import org.iblog.enhance.gateway.filter.ModifyResponseBodyFilter;
import org.iblog.enhance.gateway.filter.async.AsyncProcessingFilter;
import org.iblog.enhance.gateway.filter.convert.ConvertRequestBodyFilter;
import org.iblog.enhance.gateway.filter.convert.ConvertResponseBodyFilter;
import org.iblog.enhance.gateway.filter.convert.Converter;
import org.iblog.enhance.gateway.filter.convert.ConverterWrapper;
import org.iblog.enhance.gateway.filter.extract.ExtractRequestBodyFilter;
import org.iblog.enhance.gateway.filter.extract.ExtractResponseBodyFilter;
import org.iblog.enhance.gateway.filter.extract.Extractor;
import org.iblog.enhance.gateway.filter.extract.ExtractorWrapper;
import org.iblog.enhance.gateway.filter.logging.LoggingDecorator;
import org.iblog.enhance.gateway.filter.logging.LoggingRealPathFilter;
import org.iblog.enhance.gateway.filter.logging.LoggingRequestBodyFilter;
import org.iblog.enhance.gateway.filter.logging.LoggingResponseBodyFilter;
import org.iblog.enhance.gateway.filter.logging.LoggingResponseStatusFilter;
import org.iblog.enhance.gateway.lifecycle.LoggingLifecycleManager;
import org.iblog.enhance.gateway.lifecycle.event.Event;
import org.iblog.enhance.gateway.util.ObjectMappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author lance
 */
@Configuration
public class GlobalFilterRegistry {
    private static final Logger logger = LoggerFactory.getLogger(GlobalFilterRegistry.class);

    @Resource
    private LoggingLifecycleManager loggingLifecycleManager;
    @Resource
    private ConverterWrapper converterWrapper;
    @Resource
    private ExtractorWrapper extractorWrapper;

    @Bean
    public MarkRequestFilter markRequestFilter() {
        return new MarkRequestFilter();
    }

    @Bean
    @SuppressWarnings("unchecked")
    public InterfaceGlobalFilter loggingRequestFilter() {
        ModifyRequestBodyFilter.Config requestConfig = new ModifyRequestBodyFilter.Config()
                .setInClass(String.class)
                .setOutClass(String.class)
                .setRewriteFunction(((exchange, s) -> {
                    LoggingDecorator decorator = new LoggingDecorator.Builder<String>()
                            .setServerWebExchange((ServerWebExchange) exchange)
                            .setData(s)
                            .setFrom(LoggingDecorator.BodyFrom.REQUEST)
                            .build();
                    Event event = new Event(
                            Event.EventType.LOGGING_BODY,
                            decorator,
                            "logging request body");
                    loggingLifecycleManager.submit(event);
                    return Mono.just(decorator.getData());
                }));
        InterfaceGlobalFilter requestFilter = new LoggingRequestBodyFilter(requestConfig);
        return requestFilter;
    }

    @Bean
    @SuppressWarnings("unchecked")
    public InterfaceGlobalFilter loggingResponseFilter() {
        ModifyResponseBodyFilter.Config responseConfig = new ModifyResponseBodyFilter.Config()
                .setInClass(String.class)
                .setOutClass(String.class)
                .setRewriteFunction(((exchange, o) -> {
                    LoggingDecorator decorator = new LoggingDecorator.Builder<String>()
                            .setServerWebExchange((ServerWebExchange) exchange)
                            .setData(o)
                            .setFrom(LoggingDecorator.BodyFrom.RESPONSE)
                            .build();
                    Event event = new Event(
                            Event.EventType.LOGGING_BODY,
                            decorator,
                            "logging response body");
                    loggingLifecycleManager.submit(event);
                    return Mono.just(decorator.getData());
                }));
        InterfaceGlobalFilter responseFilter = new LoggingResponseBodyFilter(responseConfig);
        return responseFilter;
    }

    @Bean
    public LoggingResponseStatusFilter loggingResponseStatusFilter() {
        LoggingResponseStatusFilter loggingResponseStatusFilter = new LoggingResponseStatusFilter(
                new LoggingResponseStatusFilter.Config());
        return loggingResponseStatusFilter;
    }

    @Bean
    @SuppressWarnings("unchecked")
    public ConvertRequestBodyFilter convertRequestBodyFilter() {
        ModifyRequestBodyFilter.Config requestConfig = new ModifyRequestBodyFilter.Config()
                .setInClass(String.class)
                .setOutClass(String.class)
                .setRewriteFunction(((exchange, o) -> {
                    Converter converter = converterWrapper.match(
                            (ServerWebExchange) exchange, false).orNull();
                    if (converter != null) {
                        Object meat = converter.internalize(new Config<>().setData(o)).orNull();
                        if (meat == null) {
                            logger.error("should never occurred: converter {} internalized null.",
                                    converter.getName());
                            return Mono.empty();
                        }
                        LoggingDecorator decorator = new LoggingDecorator.Builder<String>()
                                .setServerWebExchange((ServerWebExchange) exchange)
                                .setData(ObjectMappers.mustWriteValue(meat))
                                .setFrom(LoggingDecorator.BodyFrom.REQUEST)
                                .build();
                        Event event = new Event(
                                Event.EventType.LOGGING_CONVERTED_BODY,
                                decorator,
                                "logging request converted body");
                        loggingLifecycleManager.submit(event);
                        if (meat instanceof Map || meat instanceof List) {
                            return Mono.just(ObjectMappers.mustWriteValue(meat));
                        } else {
                            return Mono.just(meat);
                        }
                    }
                    return Mono.just(o);
                }));
        return new ConvertRequestBodyFilter(requestConfig);
    }

    @Bean
    @SuppressWarnings("unchecked")
    public ConvertResponseBodyFilter convertResponseBodyFilter() {
        ModifyResponseBodyFilter.Config responseConfig = new ModifyResponseBodyFilter.Config()
                .setInClass(String.class)
                .setOutClass(String.class)
                .setRewriteFunction(((exchange, o) -> {
                    Converter converter = converterWrapper.match(
                            (ServerWebExchange) exchange, false).orNull();
                    if (converter != null) {
                        Object meat = converter.externalize(new Config<>().setData(o)).orNull();
                        if (meat == null) {
                            logger.error("should never occurred: converter {} externalized null.",
                                    converter.getName());
                            return Mono.empty();
                        }
                        LoggingDecorator decorator = new LoggingDecorator.Builder<String>()
                                .setServerWebExchange((ServerWebExchange) exchange)
                                .setData(ObjectMappers.mustWriteValue(meat))
                                .setFrom(LoggingDecorator.BodyFrom.RESPONSE)
                                .build();
                        Event event = new Event(
                                Event.EventType.LOGGING_CONVERTED_BODY,
                                decorator,
                                "logging response converted body");
                        loggingLifecycleManager.submit(event);
                        if (meat instanceof Map || meat instanceof List) {
                            return Mono.just(ObjectMappers.mustWriteValue(meat));
                        } else {
                            return Mono.just(meat);
                        }
                    }
                    return Mono.just(o);
                }));
        return new ConvertResponseBodyFilter(responseConfig);
    }

    @Bean
    @SuppressWarnings("unchecked")
    public ExtractRequestBodyFilter extractRequestBodyFilter() {
        ModifyRequestBodyFilter.Config requestConfig = new ModifyRequestBodyFilter.Config()
                .setInClass(String.class)
                .setOutClass(String.class)
                .setRewriteFunction(((exchange, o) -> {
                    Extractor extractor = extractorWrapper.match((ServerWebExchange) exchange, false).orNull();
                    if (extractor != null) {
                        List<KeyWord> keyWords = extractor.extractRequest(new Config<>().setData(o));
                        LoggingDecorator decorator = new LoggingDecorator.Builder<String>()
                                .setServerWebExchange((ServerWebExchange) exchange)
                                .setData(keyWords)
                                .build();
                        Event event = new Event(
                                Event.EventType.EXTRACTING_BODY,
                                decorator,
                                "extract request body");
                        loggingLifecycleManager.submit(event);
                    }
                    return Mono.just(o);
                }));
        return new ExtractRequestBodyFilter(requestConfig);
    }

    @Bean
    @SuppressWarnings("unchecked")
    public ExtractResponseBodyFilter extractResponseBodyFilter() {
        ModifyResponseBodyFilter.Config responseConfig = new ModifyResponseBodyFilter.Config()
                .setInClass(String.class)
                .setOutClass(String.class)
                .setRewriteFunction(((exchange, o) -> {
                    Extractor extractor = extractorWrapper.match((ServerWebExchange) exchange, false).orNull();
                    if (extractor != null) {
                        List<KeyWord> keyWords = extractor.extractResponse(new Config<>().setData(o));
                        LoggingDecorator decorator = new LoggingDecorator.Builder<String>()
                                .setServerWebExchange((ServerWebExchange) exchange)
                                .setData(keyWords)
                                .build();
                        Event event = new Event(
                                Event.EventType.EXTRACTING_BODY,
                                decorator,
                                "extract response body");
                        loggingLifecycleManager.submit(event);
                    }
                    return Mono.just(o);
                }));
        return new ExtractResponseBodyFilter(responseConfig);
    }

    /**
     * temporarily disabled
     * @return
     */
    @Bean
    public AsyncProcessingFilter asyncProcessingFilter() {
        return new AsyncProcessingFilter(new AsyncProcessingFilter.Config());
    }

    @Bean
    public LoggingRealPathFilter loggingRealPathFilter() {
        return new LoggingRealPathFilter();
    }
}
