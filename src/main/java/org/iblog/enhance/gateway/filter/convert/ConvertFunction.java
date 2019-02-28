package org.iblog.enhance.gateway.filter.convert;

import java.util.function.Function;
import com.google.common.base.Optional;

/**
 * @author shaoxiao.xu
 * @date 2019/1/10 14:53
 */
public interface ConvertFunction<T, R> extends Function<T, Optional<R>> {
    @Override
    Optional<R> apply(T t);
}
