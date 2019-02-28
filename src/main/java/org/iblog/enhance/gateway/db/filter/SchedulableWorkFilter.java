package org.iblog.enhance.gateway.db.filter;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author shaoxiao.xu
 * @date 2019/2/21 10:34
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
        this(null, false, false, false,
                Long.MIN_VALUE, Long.MAX_VALUE, 0, Integer.MAX_VALUE);
    }

    public SchedulableWorkFilter(
            List<String> types, boolean excludeFinished,
            boolean uncredited, boolean excludeDiscarded,
            long start, long end, int from, int to) {
        this.types = types;
        this.excludeFinished = excludeFinished;
        this.uncredited = uncredited;
        this.excludeDiscarded = excludeDiscarded;
        this.start = start;
        this.end = end;
        this.from = from;
        this.to = to;
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
            return new SchedulableWorkFilter(
                    this.types, this.excludeFinished, this.uncredited, this.excludeDiscarded,
                    this.start, this.end, this.from, this.to);
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
