package org.iblog.enhance.gateway.db.filter;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author lance
 */
public class SchedulableWorkFilter {
    public final List<String> types;
    public final boolean excludeFinished;
    public final boolean uncredited;
    public final boolean excludeDiscarded;
    public final long start;
    public final long end;
    public final int from;
    public final int to;

    public SchedulableWorkFilter() {
        this(new Builder());
    }

    public SchedulableWorkFilter(Builder builder) {
        this.types = builder.types;
        this.excludeFinished = builder.excludeFinished;
        this.uncredited = builder.uncredited;
        this.excludeDiscarded = builder.excludeDiscarded;
        this.start = builder.start;
        this.end = builder.end;
        this.from = builder.from;
        this.to = builder.to;
    }

    public static class Builder {
        private List<String> types;
        private boolean excludeFinished;
        private boolean uncredited;
        private boolean excludeDiscarded;
        private long start = Long.MIN_VALUE;
        private long end = Long.MAX_VALUE;
        private int from = 0;
        private int to = 1;

        public SchedulableWorkFilter build() {
            return new SchedulableWorkFilter(this);
        }

        public Builder setTypes(List<String> types) {
            if (CollectionUtils.isEmpty(types)) {
                this.types = null;
            } else {
                this.types = types;
            }
            return this;
        }

        public Builder setExcludeFinished(boolean excludeFinished) {
            this.excludeFinished = excludeFinished;
            return this;
        }

        public Builder setUncredited(boolean uncredited) {
            this.uncredited = uncredited;
            return this;
        }

        public Builder setExcludeDiscarded(boolean excludeDiscarded) {
            this.excludeDiscarded = excludeDiscarded;
            return this;
        }

        public Builder setStart(long start) {
            this.start = start;
            return this;
        }

        public Builder setEnd(long end) {
            this.end = end;
            return this;
        }

        public Builder setFrom(int from) {
            this.from = from;
            return this;
        }

        public Builder setTo(int to) {
            this.to = to;
            return this;
        }
    }
}
