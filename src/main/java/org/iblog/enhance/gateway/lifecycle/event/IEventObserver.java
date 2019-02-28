package org.iblog.enhance.gateway.lifecycle.event;

/**
 * An interface about {@link Event} observer.
 * Implementations of this interface should not introduce blocking operation
 * in {@link #observe(Event)}, and must be thread-safe.
 *
 * @author shaoxiao.xu
 * @date 2018/12/26 13:39
 */
public interface IEventObserver {
    void observe(Event event);
}
