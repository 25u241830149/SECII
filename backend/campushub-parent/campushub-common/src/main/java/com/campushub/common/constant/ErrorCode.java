package com.campushub.common.constant;

public final class ErrorCode {

    public static final int SUCCESS = 200;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int CONFLICT = 409;
    public static final int BUSINESS_ERROR = 422;
    public static final int INTERNAL_SERVER_ERROR = 500;

    public static final String SUCCESS_MESSAGE = "success";
    public static final String BAD_REQUEST_MESSAGE = "请求格式错误";
    public static final String UNAUTHORIZED_MESSAGE = "未认证或 Token 已失效";
    public static final String FORBIDDEN_MESSAGE = "无权限访问该资源";
    public static final String NOT_FOUND_MESSAGE = "资源不存在";
    public static final String CONFLICT_MESSAGE = "资源冲突";
    public static final String BUSINESS_ERROR_MESSAGE = "业务校验失败";
    public static final String INTERNAL_SERVER_ERROR_MESSAGE = "服务器内部错误";

    private ErrorCode() {
    }

    public static boolean isSuccess(int code) {
        return code == SUCCESS;
    }

    public static boolean isClientError(int code) {
        return code >= BAD_REQUEST && code < INTERNAL_SERVER_ERROR;
    }

    public static boolean isServerError(int code) {
        return code >= INTERNAL_SERVER_ERROR;
    }
}
