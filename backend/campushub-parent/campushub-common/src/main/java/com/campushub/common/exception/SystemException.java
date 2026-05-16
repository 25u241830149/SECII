package com.campushub.common.exception;

/**
 * Exception for infrastructure or system failures with controlled client output.
 */
public class SystemException extends RuntimeException {

    private static final int DEFAULT_CODE = 500;

    private final int code;

    public SystemException(String message) {
        this(DEFAULT_CODE, message);
    }

    public SystemException(int code, String message) {
        super(message);
        this.code = code;
    }

    public SystemException(String message, Throwable cause) {
        this(DEFAULT_CODE, message, cause);
    }

    public SystemException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
