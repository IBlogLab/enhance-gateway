package org.iblog.enhance.gateway.core;

import java.util.List;
import com.google.common.collect.Lists;

/**
 * @author lance
 */
public class KeyWord {
    private String key;
    private List<String> values;

    public static KeyWord build(String key, String... values) {
        return new KeyWord()
                .setKey(key)
                .setValues(Lists.newArrayList(values));
    }

    public KeyWord setKey(String key) {
        this.key = key;
        return this;
    }

    public KeyWord setValues(List<String> values) {
        this.values = values;
        return this;
    }

    public List<String> getValues() {
        return values;
    }

    public String getKey() {
        return key;
    }
}
