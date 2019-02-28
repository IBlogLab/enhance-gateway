package org.iblog.enhance.gateway.lifecycle.sched;

/**
 * @author lance
 */
public interface Qualifier {
    boolean canHandle(String content);
}
