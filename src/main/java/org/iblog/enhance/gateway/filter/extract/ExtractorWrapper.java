package org.iblog.enhance.gateway.filter.extract;

import javax.annotation.PostConstruct;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

/**
 * @author lance
 */
@Component
public class ExtractorWrapper {
    private static final Logger logger = LoggerFactory.getLogger(ExtractorWrapper.class);

    private final List<Extractor> extractors = Lists.newArrayList();

    @PostConstruct
    public void inject() {
        ServiceLoader<Extractor> loader = ServiceLoader.load(Extractor.class);
        Iterator<Extractor> iterator = loader.iterator();
        while (iterator.hasNext()) {
            Extractor converter = iterator.next();
            extractors.add(converter);
        }
        logger.info("ExtractorWrapper inject Extractor: size {}", extractors.size());
    }

    public Optional<Extractor> match(ServerWebExchange exchange, boolean request) {
        Extractor target = extractors.stream()
                .filter(extractor -> extractor.match(exchange, request))
                .findFirst()
                .orElse(null);
        return target == null ? Optional.absent() : Optional.of(target);
    }
}
