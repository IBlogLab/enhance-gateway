package org.iblog.enhance.gateway.core;

import lombok.Getter;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author lance
 * @param <T>
 */
@Getter
public class PageResult<T> extends Result<T> {
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private final int page;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private final int pageSize;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private final long totalCount;

    private PageResult(
            String code, T data, String message, boolean success,
            int page, int pageSize, long totalCount) {
        super(code, data, message, success);
        this.page = page;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
    }

    public static class Builder<T> {
        private String code = "200";
        private T data;
        private String message;
        private boolean success = true;
        private int page;
        private int pageSize;
        private long totalCount;

        public PageResult<T> build() {
            return new PageResult<>(
                    this.code, this.data, this.message, this.success,
                    this.page, this.pageSize, this.totalCount);
        }

        public Builder setCode(ResultCode code) {
            this.code = code.toString();
            return this;
        }

        public Builder setData(T data) {
            this.data = data;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setSuccess(boolean success) {
            this.success = success;
            return this;
        }

        public Builder setTotalCount(long totalCount) {
            this.totalCount = totalCount;
            return this;
        }

        public Builder setPageInfo(int from, int to, long totalCount) {
            this.totalCount = totalCount;
            this.pageSize = to - from;
            this.page = from / (to - from) + 1;
            return this;
        }
    }
}
