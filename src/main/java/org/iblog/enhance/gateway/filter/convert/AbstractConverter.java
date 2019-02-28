package org.iblog.enhance.gateway.filter.convert;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.iblog.enhance.gateway.filter.Config;
import org.iblog.enhance.gateway.filter.MarkRequestFilter;
import org.iblog.enhance.gateway.util.AntPathMatcherUtil;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * @author lance
 */
public abstract class AbstractConverter implements Converter {

    private final String CLIENT = "";
    private final List<HttpMethod> METHODS = Lists.newArrayList();
    private final List<String> URLS = Lists.newArrayList();

    @Override
    public String getName() {
        return "default";
    }

    @Override
    public boolean match(ServerWebExchange exchange, boolean request) {
        if (exchange == null) {
            return false;
        }
        return match(new Config().setClient(CLIENT)
                .setMethods(METHODS)
                .setUrls(URLS)
                .setRequest(request)
                .setExchange(exchange)
                .setInternalize(false)
                .setExternalize(false)
        );
    }

    protected boolean match(Config<?> config) {
        if (config == null) {
            return false;
        }
        if (!config.isInternalize() && !config.isExternalize()) {
            return false;
        }
        if (config.isRequest() && !config.isInternalize()) {
            return false;
        }
        if (!config.isRequest() && !config.isExternalize()) {
            return false;
        }
        ServerHttpRequest request = config.getExchange().getRequest();
        String client = request.getHeaders().getFirst(MarkRequestFilter.X_Session_User);
        if (!Strings.isNullOrEmpty(config.getClient()) && !config.getClient().equals(client)) {
            return false;
        }
        HttpMethod method = request.getMethod();
        if (CollectionUtils.isNotEmpty(config.getMethods()) && !config.getMethods().contains(method)) {
            return false;
        }
        if (CollectionUtils.isEmpty(config.getUrls())) {
            // not supply every path.
            return false;
        }
        String path = request.getURI().getPath();
        for (String url : config.getUrls()) {
            if (AntPathMatcherUtil.match(url, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<?> internalize(Config<?> config) {
        return config.getConvertFunction().apply(config.getData());
    }

    @Override
    public Optional<?> externalize(Config<?> config) {
        return config.getConvertFunction().apply(config.getData());
    }
}
