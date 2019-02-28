package org.iblog.enhance.gateway.lifecycle.sched;

/**
 * @author shaoxiao.xu
 * @date 2018/12/25 20:22
 */
public interface SchedulableHandler {
    boolean handle(Schedulable task);
    String type();
}
