package org.iblog.enhance.gateway.core;

import lombok.Data;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.iblog.enhance.gateway.util.ObjectUpdateUtil;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author lance
 */
@Data
@Document(collection = "schedulable_works")
public class SchedulableWork {
    private String id;
    private String type;
    private int maxRetries;
    private int retry;
    private long intervalTime;
    private long scheduledAt;
    private boolean finished;
    private boolean discarded;
    private String possessor;

    public boolean updateFrom(SchedulableWork other) {
        MutableBoolean updated = new MutableBoolean();
        type = ObjectUpdateUtil.updateField(type, other.getType(), updated);
        maxRetries = ObjectUpdateUtil.updateField(maxRetries, other.getMaxRetries(), updated);
        retry = ObjectUpdateUtil.updateField(retry, other.getRetry(), updated);
        intervalTime = ObjectUpdateUtil.updateField(intervalTime, other.getIntervalTime(), updated);
        scheduledAt = ObjectUpdateUtil.updateField(scheduledAt, other.getScheduledAt(), updated);
        finished = ObjectUpdateUtil.updateField(finished, other.isFinished(), updated);
        discarded = ObjectUpdateUtil.updateField(discarded, other.isDiscarded(), updated);
        return updated.booleanValue();
    }
}
