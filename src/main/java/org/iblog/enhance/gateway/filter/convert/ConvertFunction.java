package org.iblog.enhance.gateway.filter.convert;

import java.util.function.Function;
import com.google.common.base.Optional;

/**
 * @author lance
 */
public interface ConvertFunction<T, R> extends Function<T, Optional<R>> {
    @Override
    Optional<R> apply(T t);
}
