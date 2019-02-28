package org.iblog.enhance.gateway.filter;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.iblog.enhance.gateway.filter.async.AsyncProcessingFilter;
import org.iblog.enhance.gateway.filter.convert.ConvertRequestBodyFilter;
import org.iblog.enhance.gateway.filter.convert.ConvertResponseBodyFilter;
import org.iblog.enhance.gateway.filter.extract.ExtractRequestBodyFilter;
import org.iblog.enhance.gateway.filter.extract.ExtractResponseBodyFilter;
import org.iblog.enhance.gateway.filter.logging.LoggingRequestBodyFilter;
import org.iblog.enhance.gateway.filter.logging.LoggingResponseBodyFilter;
import org.iblog.enhance.gateway.filter.logging.LoggingResponseStatusFilter;
import org.springframework.core.Ordered;
import com.google.common.collect.Maps;

/**
 * manage priorities of filters by default.
 *
 * @author lance
 */
public class PriorityAccess {

    private static final AtomicInteger PRIORITY_ERASER = new AtomicInteger(0);

    private static final Map<String, Integer> priorities = Maps.newHashMap();

    static {
        /**
         * {@link MarkRequestFilter}
         */
        priorities.put(MarkRequestFilter.class.getName(), Ordered.HIGHEST_PRECEDENCE + 1);
        /**
         * {@link LoggingRequestBodyFilter}
         */
        priorities.put(LoggingRequestBodyFilter.class.getName(), -1000);

        /**
         * {@link ConvertRequestBodyFilter}
         */
        priorities.put(ConvertRequestBodyFilter.class.getName(), -900);

        /**
         * {@link ExtractRequestBodyFilter}
         */
        priorities.put(ExtractRequestBodyFilter.class.getName(), - 800);

        priorities.put(AsyncProcessingFilter.class.getName(), - 700);

        /**
         * {@link LoginFilter}
         */
//        priorities.put(LoginFilter.class.getName(), PRIORITY_ERASER.incrementAndGet());
//        /**
//         * {@link AuthorizationFilter}
//         */
//        priorities.put(AuthorizationFilter.class.getName(), PRIORITY_ERASER.incrementAndGet());

        /**
         * {@link LoggingResponseBodyFilter}
         */
        priorities.put(LoggingResponseBodyFilter.class.getName(), -20);

        /**
         * {@link ConvertResponseBodyFilter}
         */
        priorities.put(ConvertResponseBodyFilter.class.getName(), -25);

        /**
         * {@link ExtractResponseBodyFilter}
         */
        priorities.put(ExtractResponseBodyFilter.class.getName(), -10);

        /**
         * {@link LoggingResponseStatusFilter} after
         * {@link org.springframework.cloud.gateway.filter.NettyWriteResponseFilter}
         */
        priorities.put(LoggingResponseStatusFilter.class.getName(), 1);
    }

    public static int get(@NotNull String clazz) {
        Integer priority = priorities.get(clazz);
        return priority == null ? -1 : priority.intValue();
    }
}
