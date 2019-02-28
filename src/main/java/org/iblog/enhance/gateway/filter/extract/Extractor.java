package org.iblog.enhance.gateway.filter.extract;

import java.util.List;
import org.iblog.enhance.gateway.core.KeyWord;
import org.iblog.enhance.gateway.filter.Config;
import org.springframework.web.server.ServerWebExchange;

/**
 * Service Provider Interface.
 *
 * @author lance
 */
public interface Extractor {
    default String getName() {
        return "default";
    }

    /**
     *
     * @param config (json String)
     * @return
     */
    List<KeyWord> extractRequest(Config<?> config);

    /**
     *
     * @param config (json String)
     * @return
     */
    List<KeyWord> extractResponse(Config<?> config);

    /**
     * decide whether to match
     * @param exchange
     * @param request true match for internalize | false match for externalize
     * @return
     */
    boolean match(ServerWebExchange exchange, boolean request);
}
