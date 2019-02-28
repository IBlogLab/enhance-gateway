package org.iblog.enhance.gateway.lifecycle.event;

/**
 * An interface about {@link Event} observer.
 * Implementations of this interface should not introduce blocking operation
 * in {@link #observe(Event)}, and must be thread-safe.
 *
 * @author lance
 */
public interface IEventObserver {
    void observe(Event event);
}
