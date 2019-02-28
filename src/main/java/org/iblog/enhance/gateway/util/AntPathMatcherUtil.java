package org.iblog.enhance.gateway.util;

import org.springframework.util.AntPathMatcher;

/**
 * @author shaoxiao.xu
 * @date 2019/1/9 19:34
 */
public class AntPathMatcherUtil {
    private static final AntPathMatcher matcher = new AntPathMatcher();

    public static boolean match(String pattern, String path) {
        return matcher.match(pattern, path);
    }
}
