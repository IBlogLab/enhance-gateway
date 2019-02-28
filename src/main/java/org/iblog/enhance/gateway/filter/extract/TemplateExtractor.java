package org.iblog.enhance.gateway.filter.extract;

import java.util.List;
import java.util.Map;
import org.iblog.enhance.gateway.core.KeyWord;
import org.iblog.enhance.gateway.filter.Config;
import org.iblog.enhance.gateway.util.ObjectMappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.server.ServerWebExchange;
import com.google.common.collect.Lists;

/**
 * @author lance
 */
public class TemplateExtractor extends AbstractExtractor {
    private static final Logger logger = LoggerFactory.getLogger(TemplateExtractor.class);

    @Override
    public String getName() {
        return "template";
    }

    @Override
    public List<KeyWord> extractRequest(Config<?> config) {
        config.setExtractFunction(o -> {
            Map map = ObjectMappers.mustReadValue((String) o, Map.class);
            return Lists.newArrayList(KeyWord.build("id", String.valueOf(map.get("id"))));
        });
        return super.extractRequest(config);
    }

    @Override
    public List<KeyWord> extractResponse(Config<?> config) {
        config.setExtractFunction(o -> {
            Map map = ObjectMappers.mustReadValue((String) o, Map.class);
            return Lists.newArrayList(KeyWord.build("code", (String) map.get("code")));
        });
        return super.extractResponse(config);
    }

    @Override
    public boolean match(ServerWebExchange exchange, boolean request) {
        Config config = new Config()
                .setMethods(Lists.newArrayList(HttpMethod.POST))
                .setUrls(Lists.newArrayList("/basic/agv/"))
                .setClient("user_user_user")
                .setInternalize(true)
                .setExternalize(true)
                .setExchange(exchange);
        return match(config);
    }
}
