package org.iblog.enhance.gateway.filter;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.cloud.gateway.support.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.DefaultServerRequest;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.server.ServerWebExchange;
import com.google.common.base.Strings;

/**
 * @author shaoxiao.xu
 * @date 2019/1/14 16:34
 */
@SuppressWarnings("unchecked")
public abstract class ModifyRequestBodyFilter extends InterfaceGlobalFilter {
    protected final Config config;

    public ModifyRequestBodyFilter(Config config) {
        this.config = config;
    }

    public Mono<Void> decorate(ServerWebExchange exchange, GatewayFilterChain chain) {
        Class inClass = config.getInClass();
        DefaultServerRequest serverRequest = new DefaultServerRequest(exchange);
        Mono<?> loggingBody = serverRequest.bodyToMono(inClass)
                .flatMap(o -> config.rewriteFunction.apply(exchange, o));
        BodyInserter bodyInserter = BodyInserters.fromPublisher(loggingBody, config.getOutClass());

        HttpHeaders headers = new HttpHeaders();
        headers.putAll(exchange.getRequest().getHeaders());
        // the new content type will be computed by bodyInserter
        // and then set in the request decorator
        headers.remove(HttpHeaders.CONTENT_LENGTH);

        CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(
                exchange, exchange.getRequest().getHeaders());

        return bodyInserter.insert(outputMessage, new BodyInserterContext())
                .then(Mono.defer(() -> {
                    ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(exchange.getRequest()) {
                        @Override
                        public HttpHeaders getHeaders() {
                            long contentLength = headers.getContentLength();
                            HttpHeaders httpHeaders = new HttpHeaders();
                            httpHeaders.putAll(super.getHeaders());
                            if (contentLength > 0) {
                                httpHeaders.setContentLength(contentLength);
                            } else {
                                // TODO: this causes a 'HTTP/1.1 411 Length Required' on httpbin.org
                                httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
                            }
                            return httpHeaders;
                        }

                        @Override
                        public Flux<DataBuffer> getBody() {
                            return outputMessage.getBody();
                        }
                    };
                    return chain.filter(exchange.mutate().request(decorator).build());
                }));
    }

    protected boolean checkContentLength(ServerWebExchange exchange) {
        String contentLen = exchange.getRequest().getHeaders().getFirst(HttpHeaders.CONTENT_LENGTH);
        if (Strings.isNullOrEmpty(contentLen) || Integer.valueOf(contentLen) <= 0) {
            return false;
        }
        return true;
    }

    public static class Config {
        private String filterName = "modifyRequestBodyFilter";
        private boolean start = true;
        private int order;

        private Class inClass;
        private Class outClass;
        private RewriteFunction rewriteFunction;

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

        public Class getInClass() {
            return inClass;
        }

        public Config setInClass(Class inClass) {
            this.inClass = inClass;
            return this;
        }

        public Class getOutClass() {
            return outClass;
        }

        public Config setOutClass(Class outClass) {
            this.outClass = outClass;
            return this;
        }

        public RewriteFunction getRewriteFunction() {
            return rewriteFunction;
        }

        public <T, R> Config setRewriteFunction(Class<T> inClass, Class<R> outClass,
                                                RewriteFunction<T, R> rewriteFunction) {
            setInClass(inClass);
            setOutClass(outClass);
            setRewriteFunction(rewriteFunction);
            return this;
        }

        public Config setRewriteFunction(RewriteFunction rewriteFunction) {
            this.rewriteFunction = rewriteFunction;
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
