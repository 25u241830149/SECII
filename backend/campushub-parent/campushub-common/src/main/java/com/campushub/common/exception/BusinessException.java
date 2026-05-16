package com.campushub.common.exception;

/**
 * Exception for expected business-rule failures that should be returned to clients.
 */
public class BusinessException extends RuntimeException {

    private static final int DEFAULT_CODE = 422;

    private final int code;

    public BusinessException(String message) {
        this(DEFAULT_CODE, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
