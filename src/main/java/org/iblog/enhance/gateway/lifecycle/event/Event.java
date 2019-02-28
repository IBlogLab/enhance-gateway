package org.iblog.enhance.gateway.lifecycle.event;

import org.iblog.enhance.gateway.filter.logging.LoggingDecorator;

/**
 * @author lance
 */
public class Event {
    public enum EventType {
        LOGGING_REQUEST,
        LOGGING_BODY,
        LOGGING_RESPONSE,
        EXTRACTING_BODY,
        LOGGING_CONVERTED_BODY,
    }
    public final EventType type;
    public final LoggingDecorator decorator;
    public final String message;

    public Event(EventType type, LoggingDecorator decorator, String message) {
        this.type = type;
        this.decorator = decorator;
        this.message = message;
    }
}
