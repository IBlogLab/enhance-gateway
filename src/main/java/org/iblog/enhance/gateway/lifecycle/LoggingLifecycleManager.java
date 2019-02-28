package org.iblog.enhance.gateway.lifecycle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import org.iblog.enhance.gateway.lifecycle.event.Event;
import org.iblog.enhance.gateway.lifecycle.event.IEventObserver;
import org.iblog.enhance.gateway.util.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.google.common.collect.Lists;

/**
 * @author shaoxiao.xu
 * @date 2018/12/25 20:05
 */
@Component
public class LoggingLifecycleManager {
    private static final Logger logger = LoggerFactory.getLogger(LoggingLifecycleManager.class);

    private final List<IEventObserver> observers = Lists.newArrayList();
    private final Clock clock = Clock.defaultClock();

    @PostConstruct
    public void start() throws Exception {
        logger.info("LoggingLifecycleManager start at {}", clock.getTime());
    }

    @PreDestroy
    public void destroy() throws Exception {
        logger.info("LoggingLifecycleManager destroy at {}", clock.getTime());
    }

    public void registerObserver(IEventObserver observer) {
        synchronized (this.observers) {
            this.observers.add(observer);
        }
    }

    public void submit(Event event) {
        for (IEventObserver observer : observers) {
            observer.observe(event);
        }
    }
}
