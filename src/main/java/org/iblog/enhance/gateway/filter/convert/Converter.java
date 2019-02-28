package org.iblog.enhance.gateway.filter.convert;

import org.iblog.enhance.gateway.filter.Config;
import org.springframework.web.server.ServerWebExchange;
import com.google.common.base.Optional;

/**
 * Service Provider Interface.
 *
 * @author lance
 */
public interface Converter {
    /**
     * Converter identity
     * @return
     */
    default String getName() {
        return "default";
    }

    /**
     * decide whether to match
     * @param exchange
     * @param request true match for internalize | false match for externalize
     * @return
     */
    boolean match(ServerWebExchange exchange, boolean request);

    /**
     * externalize object (e.g. json、xml) string mapping to internalize standard object (must is json).
     * @param config
     * @return
     */
    Optional<?> internalize(Config<?> config);

    /**
     * internalize standard object (must is json) string mapping to externalize object (e.g. json、xml) .
     * @param config
     * @return
     */
    Optional<?> externalize(Config<?> config);
}
