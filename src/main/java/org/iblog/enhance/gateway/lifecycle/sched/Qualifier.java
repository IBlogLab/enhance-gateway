package org.iblog.enhance.gateway.lifecycle.sched;

/**
 * @author shaoxiao.xu
 * @date 2019/2/21 14:23
 */
public interface Qualifier {
    boolean canHandle(String content);
}
