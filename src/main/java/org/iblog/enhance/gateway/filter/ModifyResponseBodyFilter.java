package org.iblog.enhance.gateway.filter;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.cloud.gateway.support.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.DefaultClientResponse;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.server.ServerWebExchange;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR;

/**
 * @author shaoxiao.xu
 * @date 2019/1/14 17:06
 */
@SuppressWarnings("unchecked")
public abstract class ModifyResponseBodyFilter extends InterfaceGlobalFilter {
    protected final Config config;

    public ModifyResponseBodyFilter(Config config) {
        this.config = config;
    }

    protected ServerHttpResponseDecorator decorate(ServerWebExchange exchange) {
        return new ServerHttpResponseDecorator(exchange.getResponse()) {

            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {

                Class inClass = config.getInClass();
                Class outClass = config.getOutClass();
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.add(HttpHeaders.CONTENT_TYPE, exchange.getAttribute(ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR));
                ResponseAdapter responseAdapter = new ResponseAdapter(body, httpHeaders);
                DefaultClientResponse clientResponse = new DefaultClientResponse(responseAdapter, ExchangeStrategies.withDefaults());

                //TODO: flux or mono
                Mono modifiedBody = clientResponse.bodyToMono(inClass)
                        .flatMap(originalBody -> config.rewriteFunction.apply(exchange, originalBody));

                BodyInserter bodyInserter = BodyInserters.fromPublisher(modifiedBody, outClass);
                CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, exchange.getResponse().getHeaders());
                return bodyInserter.insert(outputMessage, new BodyInserterContext())
                        .then(Mono.defer(() -> {
                            Flux<DataBuffer> messageBody = outputMessage.getBody();
                            HttpHeaders headers = getDelegate().getHeaders();
                            if (!headers.containsKey(HttpHeaders.TRANSFER_ENCODING)) {
                                messageBody = messageBody.doOnNext(data -> headers.setContentLength(data.readableByteCount()));
                            }
                            //TODO: use isStreamingMediaType?
                            return getDelegate().writeWith(messageBody);
                        }));
            }

            @Override
            public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
                return writeWith(Flux.from(body)
                        .flatMapSequential(p -> p));
            }
        };
    }

    public class ResponseAdapter implements ClientHttpResponse {

        private final Flux<DataBuffer> flux;
        private final HttpHeaders headers;

        public ResponseAdapter(Publisher<? extends DataBuffer> body, HttpHeaders headers) {
            this.headers = headers;
            if (body instanceof Flux) {
                flux = (Flux) body;
            } else {
                flux = ((Mono)body).flux();
            }
        }

        @Override
        public Flux<DataBuffer> getBody() {
            return flux;
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        @Override
        public HttpStatus getStatusCode() {
            return null;
        }

        @Override
        public int getRawStatusCode() {
            return 0;
        }

        @Override
        public MultiValueMap<String, ResponseCookie> getCookies() {
            return null;
        }
    }

    public static class Config {
        private String filterName = "modifyResponseBodyFilter";
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
