package org.iblog.enhance.gateway.filter;

import java.util.List;
import org.iblog.enhance.gateway.filter.convert.ConvertFunction;
import org.iblog.enhance.gateway.filter.extract.ExtractFunction;
import org.springframework.http.HttpMethod;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author lance
 */
public class Config<T> {
    private String client;
    private List<HttpMethod> methods;
    private List<String> urls;
    private boolean internalize;
    private boolean externalize;
    private ServerWebExchange exchange;
    private T data;

    private boolean request;
    private ConvertFunction convertFunction;
    private ExtractFunction extractFunction;

    public List<HttpMethod> getMethods() {
        return methods;
    }

    public Config setMethods(List<HttpMethod> methods) {
        this.methods = methods;
        return this;
    }

    public List<String> getUrls() {
        return urls;
    }

    public Config setUrls(List<String> urls) {
        this.urls = urls;
        return this;
    }

    public String getClient() {
        return client;
    }

    public Config setClient(String client) {
        this.client = client;
        return this;
    }

    public ConvertFunction getConvertFunction() {
        return convertFunction;
    }

    public Config setConvertFunction(ConvertFunction convertFunction) {
        this.convertFunction = convertFunction;
        return this;
    }

    public ServerWebExchange getExchange() {
        return exchange;
    }

    public Config setExchange(ServerWebExchange exchange) {
        this.exchange = exchange;
        return this;
    }

    public T getData() {
        return data;
    }

    public Config setData(T data) {
        this.data = data;
        return this;
    }

    public boolean isInternalize() {
        return internalize;
    }

    public Config setInternalize(boolean internalize) {
        this.internalize = internalize;
        return this;
    }

    public boolean isExternalize() {
        return externalize;
    }

    public Config setExternalize(boolean externalize) {
        this.externalize = externalize;
        return this;
    }

    public boolean isRequest() {
        return request;
    }

    public Config setRequest(boolean request) {
        this.request = request;
        return this;
    }

    public ExtractFunction getExtractFunction() {
        return extractFunction;
    }

    public Config setExtractFunction(ExtractFunction extractFunction) {
        this.extractFunction = extractFunction;
        return this;
    }
}
