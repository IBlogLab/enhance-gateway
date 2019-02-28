package org.iblog.enhance.gateway.db.filter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.iblog.enhance.gateway.core.KeyWord;
import org.springframework.http.HttpMethod;
import org.iblog.enhance.gateway.core.Error;

/**
 * @author lance
 */
public class LoggingRecordFilter {
    public final List<String> uris;
    public final List<String> types;
    public final boolean excludeAsync;
    public final boolean excludeSync;
    public final boolean excludeHandleSuccess;
    public final boolean excludeHandleFail;
    public final List<String> errors;
    public final List<String> methods;
    public final List<Integer> statuses;
    public final List<String> froms;
    public final List<String> tos;
    public final List<KeyWord> tags;
    public final long start;
    public final long end;
    public final long[] reqRange;
    public final long[] respRange;
    public final int from;
    public final int to;

    private LoggingRecordFilter() {
        this(null, null,
                false, false,
                false, false,
                null, null, null, null,
                null, null,
                Long.MIN_VALUE, Long.MAX_VALUE,
                new long[] {Long.MIN_VALUE, Long.MAX_VALUE},new long[] {Long.MIN_VALUE, Long.MAX_VALUE},
                0, Integer.MAX_VALUE);
    }

    private LoggingRecordFilter(
            List<String> uris, List<String> types,
            boolean excludeAsync, boolean excludeSync,
            boolean excludeHandleSuccess, boolean excludeHandleFail,
            List<String> errors, List<String> methods,
            List<Integer> statuses, List<String> froms,
            List<String> tos,List<KeyWord> tags,
            long start, long end,
            long[] reqRange, long[] respRange,
            int from, int to) {
        this.uris = uris;
        this.types = types;
        this.statuses = statuses;
        this.excludeAsync = excludeAsync;
        this.excludeSync = excludeSync;
        this.excludeHandleSuccess = excludeHandleSuccess;
        this.excludeHandleFail = excludeHandleFail;
        this.errors = errors;
        this.methods = methods;
        this.froms = froms;
        this.tos = tos;
        this.tags = tags;
        this.start = start;
        this.end = end;
        this.reqRange = reqRange;
        this.respRange = respRange;
        this.from = from;
        this.to = to;
    }

    public static class Builder {
        private List<String> uris;
        private List<String> types;
        private boolean excludeAsync;
        private boolean excludeSync;
        private boolean excludeHandleSuccess;
        private boolean excludeHandleFail;
        private List<String> errors;
        private List<String> methods;
        private List<Integer> statuses;
        private List<String> froms;
        private List<String> tos;
        private List<KeyWord> tags;
        private long start = Long.MIN_VALUE;
        private long end = Long.MAX_VALUE;
        private long[] reqRange = new long[] {Long.MIN_VALUE, Long.MAX_VALUE};
        private long[] respRange = new long[] {Long.MIN_VALUE, Long.MAX_VALUE};
        private int from = 0;
        private int to = Integer.MAX_VALUE;

        public LoggingRecordFilter build() {
            return new LoggingRecordFilter(
                    this.uris, this.types,
                    this.excludeAsync, this.excludeSync,
                    this.excludeHandleSuccess, this.excludeHandleFail,
                    this.errors, this.methods,
                    this.statuses, this.froms,
                    this.tos, this.tags,
                    this.start, this.end,
                    this.reqRange, this.respRange,
                    this.from, this.to);
        }

        public Builder setUris(List<String> uris) {
            if (CollectionUtils.isEmpty(uris)) {
                this.uris = null;
            } else {
                this.uris = uris;
            }
            return this;
        }

        public Builder setTypes(List<String> types) {
            if (CollectionUtils.isEmpty(types)) {
                this.types = null;
            } else {
                this.types = types;
            }
            return this;
        }

        public Builder setStatuses(List<Integer> statuses) {
            if (CollectionUtils.isEmpty(statuses)) {
                this.statuses = null;
            } else {
                this.statuses = statuses;
            }
            return this;
        }

        public Builder setExcludeAsync(boolean excludeAsync) {
            this.excludeAsync = excludeAsync;
            return this;
        }

        public Builder setExcludeSync(boolean excludeSync) {
            this.excludeSync = excludeSync;
            return this;
        }

        public Builder setExcludeHandleSuccess(boolean excludeHandleSuccess) {
            this.excludeHandleSuccess = excludeHandleSuccess;
            return this;
        }

        public Builder setExcludeHandleFail(boolean excludeHandleFail) {
            this.excludeHandleFail = excludeHandleFail;
            return this;
        }

        public Builder setErrors(List<Error> errors) {
            if (CollectionUtils.isEmpty(errors)) {
                this.errors = null;
            } else {
                this.errors = errors.stream()
                        .filter(Objects::nonNull)
                        .map(Objects::toString)
                        .collect(Collectors.toList());
            }
            return this;
        }

        public Builder setMethods(List<HttpMethod> methods) {
            if (CollectionUtils.isEmpty(methods)) {
                this.methods = null;
            } else {
                this.methods = methods.stream()
                        .filter(Objects::nonNull)
                        .map(Objects::toString)
                        .collect(Collectors.toList());
            }
            return this;
        }

        public Builder setFroms(List<String> froms) {
            if (CollectionUtils.isEmpty(froms)) {
                this.froms = froms;
            } else {
                this.froms = froms;
            }
            return this;
        }

        public Builder setTos(List<String> tos) {
            if (CollectionUtils.isEmpty(tos)) {
                this.tos = null;
            } else {
                this.tos = tos;
            }
            return this;
        }

        public Builder setTags(List<KeyWord> tags) {
            if (CollectionUtils.isEmpty(tags)) {
                this.tags = null;
            } else {
                this.tags = tags;
            }
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

        public Builder setReqRange(long left, long right) {
            long[] range = new long[2];
            if (left > right) {
                range[0] = Long.MIN_VALUE;
                range[1] = Long.MAX_VALUE;
            } else {
                range[0] = left;
                range[1] = right;
            }
            this.reqRange = range;
            return this;
        }

        public Builder setRespRange(long left, long right) {
            long[] range = new long[2];
            if (left > right) {
                range[0] = Long.MIN_VALUE;
                range[1] = Long.MAX_VALUE;
            } else {
                range[0] = left;
                range[1] = right;
            }
            this.respRange = range;
            return this;
        }
    }
}
