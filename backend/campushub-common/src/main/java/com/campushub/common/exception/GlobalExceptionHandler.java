package com.campushub.common.exception;

import com.campushub.common.response.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException exception) {
        return ApiResponse.error(400, exception.getMessage());
    }

    @ExceptionHandler(SystemException.class)
    public ApiResponse<Void> handleSystemException(SystemException exception) {
        return ApiResponse.error(500, exception.getMessage());
    }
}