package org.iblog.enhance.gateway.filter.extract;

import java.util.List;
import java.util.function.Function;
import org.iblog.enhance.gateway.core.KeyWord;

/**
 * @author lance
 */
public interface ExtractFunction<T> extends Function<T, List<KeyWord>> {
    @Override
    List<KeyWord> apply(T t);
}
