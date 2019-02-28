package org.iblog.enhance.gateway.util;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import com.google.common.collect.Lists;

import static org.iblog.enhance.gateway.filter.MarkRequestFilter.X_Session_Secret;
import static org.iblog.enhance.gateway.filter.MarkRequestFilter.X_Session_User;

/**
 * @author lance
 */
public class LoggingUUIDUtil {
    /**
     * produce a uuid for request
     *
     * @param exchange
     * @return
     */
    public static String generateUUID(@NotNull ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        List<String> keys = Lists.newArrayList(
                request.getURI().getPath(),
                request.getHeaders().getFirst(X_Session_User),
                request.getHeaders().getFirst(X_Session_Secret));
        if (request.getRemoteAddress() != null && request.getRemoteAddress().getAddress() != null) {
            keys.add(request.getRemoteAddress().getAddress().getHostAddress());
        }
        // salting
        keys.add(UUID.randomUUID().toString());
        return SecurityUtil.md5sum(StringUtils.join(keys, "_"));
    }
}
