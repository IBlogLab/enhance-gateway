package org.iblog.enhance.gateway.filter.convert;

import javax.annotation.PostConstruct;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

/**
 * @author shaoxiao.xu
 * @date 2019/1/10 15:10
 */
@Component
public class ConverterWrapper {
    private static final Logger logger = LoggerFactory.getLogger(ConverterWrapper.class);

    // TODO  need metrics ?
    private final List<Converter> converters = Lists.newArrayList();

    @PostConstruct
    public void inject() {
        ServiceLoader<Converter> loader = ServiceLoader.load(Converter.class);
        Iterator<Converter> iterator = loader.iterator();
        while (iterator.hasNext()) {
            Converter converter = iterator.next();
            converters.add(converter);
        }
        logger.info("finish injecting converters: size {}", converters.size());
    }

    /**
     * match for {@link ServerHttpRequest}
     * @param exchange
     * @return
     */
    public Optional<Converter> match(ServerWebExchange exchange, boolean request) {
        Converter target = converters.stream()
                .filter(converter -> converter.match(exchange, request))
                .findFirst()
                .orElse(null);
        return target == null ? Optional.absent()
                : Optional.of(target);
    }
}
