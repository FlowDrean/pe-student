package com.student.vo;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;
    private long timestamp;

    private Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = "success";
        r.data = data;
        return r;
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        return r;
    }

    public static <T> Result<T> fail(String message) {
        return fail(500, message);
    }

    public static <T> Result<T> unauthorized(String message) {
        return fail(401, message);
    }

    public static <T> Result<T> forbidden(String message) {
        return fail(403, message);
    }

    public static <T> Result<T> notFound(String message) {
        return fail(404, message);
    }

    public static <T> Result<T> badRequest(String message) {
        return fail(400, message);
    }

    public static <T> Result<T> error(String message) {
        return fail(500, message);
    }
}
