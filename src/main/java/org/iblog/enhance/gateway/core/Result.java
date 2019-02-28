package org.iblog.enhance.gateway.core;

/**
 * @author shaoxiao.xu
 * @date 2019/2/28 16:45
 */
public class Result<T> {
    private static final String DEFAULT_CODE_SUCCESS = "200";
    private static final String DEFAULT_CODE_FAILURE = "500";
    private final String code;
    private final T data;
    private final String message;
    private final boolean success;

    public Result(String code, T data, String message, boolean success) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.success = success;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public String getCode() {
        return this.code;
    }

    public T getData() {
        return this.data;
    }

    public String getMessage() {
        return this.message;
    }

    public static <T> Result<T> build(String code, T data, String message, boolean success) {
        return new Result(code, data, message, success);
    }

    public static <T> Result<T> success(T data, String message) {
        return new Result("200", data, message, true);
    }

    public static <T> Result<T> success(T data) {
        return new Result("200", data, (String)null, true);
    }

    public static Result<Void> success() {
        return new Result("200", (Object)null, (String)null, true);
    }

    public static Result failure(String message) {
        return new Result("500", (Object)null, message, false);
    }

    public static Result failure(String message, String code) {
        if (code == null) {
            code = "500";
        }

        return new Result(code, (Object)null, message, false);
    }
}
