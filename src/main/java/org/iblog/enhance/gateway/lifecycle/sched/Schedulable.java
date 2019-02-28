package org.iblog.enhance.gateway.lifecycle.sched;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author shaoxiao.xu
 * @date 2018/12/25 20:23
 */
@Data
public class Schedulable {
    /**
     * Type of the Schedulable.
     * The scheduler will use this filed for dispatching tasks.
     * So must not is null.
     */
    @NotNull
    private String type;

    /**
     * Content of the task.
     * Interpretation of the content is up for the components that create and execute the task to negotiate.
     */
    private String data;

    public Schedulable() {
        // json
    }

    public Schedulable(String type, String data) {
        this.type = type;
        this.data = data;
    }
}
