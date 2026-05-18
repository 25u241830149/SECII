package com.campushub.common.exception;

import com.campushub.common.constant.ErrorCode;

/**
 * Exception for infrastructure or system failures with controlled client output.
 */
public class SystemException extends RuntimeException {

    private final int code;

    public SystemException(String message) {
        this(ErrorCode.INTERNAL_SERVER_ERROR, message);
    }

    public SystemException(int code, String message) {
        super(message);
        this.code = code;
    }

    public SystemException(String message, Throwable cause) {
        this(ErrorCode.INTERNAL_SERVER_ERROR, message, cause);
    }

    public SystemException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
