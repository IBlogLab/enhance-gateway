package org.iblog.enhance.gateway.lifecycle.sched;

/**
 * @author lance
 */
public interface SchedulableHandler {
    boolean handle(Schedulable task);
    String type();
}
