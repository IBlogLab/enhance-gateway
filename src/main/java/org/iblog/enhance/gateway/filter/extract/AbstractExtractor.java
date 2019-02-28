package org.iblog.enhance.gateway.filter.extract;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.iblog.enhance.gateway.core.KeyWord;
import org.iblog.enhance.gateway.filter.Config;
import org.iblog.enhance.gateway.util.AntPathMatcherUtil;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import com.google.common.collect.Lists;

/**
 * the default implementation does not provide any capabilities.
 *
 * @author lance
 */
@SuppressWarnings("unchecked")
public abstract class AbstractExtractor implements Extractor {
    private final List<String> URIS = Lists.newArrayList();
    private final List<String> METHODS = Lists.newArrayList();

    @Override
    public List<KeyWord> extractRequest(Config<?> config) {
        return config.getExtractFunction().apply(config.getData());
    }

    @Override
    public List<KeyWord> extractResponse(Config<?> config) {
        return config.getExtractFunction().apply(config.getData());
    }

    @Override
    public boolean match(ServerWebExchange exchange, boolean request) {
        if (exchange == null) {
            return false;
        }
        Config config = new Config()
                .setUrls(URIS)
                .setMethods(METHODS)
                .setExchange(exchange)
                .setRequest(request)
                .setInternalize(false)
                .setExternalize(false);
        return match(config);
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
}
