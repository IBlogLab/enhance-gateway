package org.iblog.enhance.gateway.lifecycle.event;

import org.iblog.enhance.gateway.filter.logging.LoggingDecorator;

/**
 * @author shaoxiao.xu
 * @date 2018/12/26 13:35
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
