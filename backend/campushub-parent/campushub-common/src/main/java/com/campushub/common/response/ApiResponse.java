package com.campushub.common.response;

public record ApiResponse<T>(int code, String message, T data) {

    public static final int SUCCESS_CODE = 200;
    public static final String SUCCESS_MESSAGE = "success";

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(SUCCESS_CODE, SUCCESS_MESSAGE, data);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(SUCCESS_CODE, SUCCESS_MESSAGE, null);
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
