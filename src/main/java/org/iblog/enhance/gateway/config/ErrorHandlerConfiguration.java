package org.iblog.enhance.gateway.config;

import java.util.Collections;
import java.util.List;
import org.iblog.enhance.gateway.filter.handler.InterfaceExceptionHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

/**
 * @author shaoxiao.xu
 * @date 2019/1/18 16:28
 */
@Configuration
@EnableConfigurationProperties({ServerProperties.class, ResourceProperties.class})
public class ErrorHandlerConfiguration {

    @Primary
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ErrorWebExceptionHandler errorWebExceptionHandler(
            ErrorAttributes errorAttributes,
            ServerProperties serverProperties,
            ResourceProperties resourceProperties,
            ObjectProvider<List<ViewResolver>> viewResolversProvider,
            ServerCodecConfigurer serverCodecConfigurer,
            ApplicationContext applicationContext) {
        InterfaceExceptionHandler exceptionHandler = new InterfaceExceptionHandler(
                errorAttributes, resourceProperties, serverProperties.getError(), applicationContext);
        exceptionHandler.setViewResolvers(viewResolversProvider.getIfAvailable(Collections::emptyList));
        exceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
        exceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
        return exceptionHandler;
    }
}
