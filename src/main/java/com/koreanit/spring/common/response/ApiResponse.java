package com.koreanit.spring.common.response;

public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final String code; // 실패 식별자

    private ApiResponse(boolean success, String message, T data, String code) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.code = code;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public String getCode() {
        return code;
    }

    /* ---------- 성공 ---------- */

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", data, null);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, null);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, "OK", null, null);
    }

    public static ApiResponse<Void> ok(String message) {
        return new ApiResponse<>(true, message, null, null);
    }

    /* ---------- 성공(별칭) : 컨트롤러가 success를 쓰고 있으면 이걸로 호환 ---------- */

    public static <T> ApiResponse<T> success(T data) {
        return ok(data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ok(message, data);
    }

    public static ApiResponse<Void> success() {
        return ok();
    }

    public static ApiResponse<Void> success(String message) {
        return ok(message);
    }

    /* ---------- 실패 ---------- */

    public static ApiResponse<Void> fail(String code, String message) {
        return new ApiResponse<>(false, message, null, code);
    }
}
