package org.iblog.enhance.gateway.controller;

import org.iblog.enhance.gateway.lifecycle.sched.Schedulable;
import org.iblog.enhance.gateway.lifecycle.sched.SchedulableHandler;
import org.springframework.stereotype.Component;

/**
 * @author shaoxiao.xu
 * @date 2019/2/21 16:01
 */
@Component
public class RehandleHandler implements SchedulableHandler {
    private final String TYPE = "external_async_schedulable";

    @Override
    public boolean handle(Schedulable task) {
        return true;
    }

    @Override
    public String type() {
        return TYPE;
    }
}
