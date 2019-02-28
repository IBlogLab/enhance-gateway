package org.iblog.enhance.gateway.filter.convert;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.iblog.enhance.gateway.filter.Config;
import org.iblog.enhance.gateway.util.ObjectMappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.server.ServerWebExchange;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

/**
 * @author lance
 */
public class TemplateConverter extends AbstractConverter {
    private static final Logger logger = LoggerFactory.getLogger(TemplateConverter.class);

    @Override
    public String getName() {
        return "template";
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

    @Override
    @SuppressWarnings("unchecked")
    public Optional<?> internalize(Config<?> config) {
        config.setConvertFunction(o -> {
            Map map = ObjectMappers.mustReadValue((String) o, HashMap.class);
            map.put("code", UUID.randomUUID().toString());
            return Optional.of(map);
        });
        return super.internalize(config);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<?> externalize(Config<?> config) {
        config.setConvertFunction(o -> {
            Map map = ObjectMappers.mustReadValue((String) o, HashMap.class);
            map.put("converter_response", true);
            return Optional.of(map);
        });
        return super.externalize(config);
    }
}
