package com.campushub.common.exception;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final int BAD_REQUEST = 400;
    private static final int UNAUTHORIZED = 401;
    private static final int FORBIDDEN = 403;
    private static final int NOT_FOUND = 404;
    private static final int INTERNAL_SERVER_ERROR = 500;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorBody> handleBusinessException(BusinessException ex) {
        return error(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ErrorBody> handleSystemException(SystemException ex) {
        log.error("System exception", ex);
        return error(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorBody> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        return error(BAD_REQUEST, resolveFieldErrors(ex.getBindingResult().getFieldErrors()));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorBody> handleBindException(BindException ex) {
        return error(BAD_REQUEST, resolveFieldErrors(ex.getBindingResult().getFieldErrors()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorBody> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));
        return error(BAD_REQUEST, defaultIfBlank(message, "请求参数不合法"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorBody> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        return error(BAD_REQUEST, "缺少必填参数: " + ex.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorBody> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return error(BAD_REQUEST, "参数类型不正确: " + ex.getName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorBody> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return error(BAD_REQUEST, "请求体格式错误");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorBody> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return error(BAD_REQUEST, "请求方法不支持: " + ex.getMethod());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorBody> handleAuthenticationException(AuthenticationException ex) {
        return error(UNAUTHORIZED, "未认证或 Token 已失效");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorBody> handleAccessDeniedException(AccessDeniedException ex) {
        return error(FORBIDDEN, "无权限访问该资源");
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorBody> handleNoHandlerFound(NoHandlerFoundException ex) {
        return error(NOT_FOUND, "资源不存在");
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErrorBody> handleErrorResponseException(ErrorResponseException ex) {
        int code = ex.getStatusCode().value();
        String message = defaultIfBlank(ex.getBody().getDetail(), ex.getBody().getTitle());
        return error(code, defaultIfBlank(message, "请求处理失败"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorBody> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return error(INTERNAL_SERVER_ERROR, "服务器内部错误");
    }

    private ResponseEntity<ErrorBody> error(int code, String message) {
        return ResponseEntity.status(resolveHttpStatus(code))
                .body(new ErrorBody(code, defaultIfBlank(message, "请求处理失败"), null));
    }

    private HttpStatus resolveHttpStatus(int code) {
        return HttpStatus.resolve(code) == null ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.valueOf(code);
    }

    private String resolveFieldErrors(Iterable<FieldError> fieldErrors) {
        String message = "";
        for (FieldError fieldError : fieldErrors) {
            String fieldMessage = fieldError.getField() + ": " + fieldError.getDefaultMessage();
            message = message.isBlank() ? fieldMessage : message + "; " + fieldMessage;
        }
        return defaultIfBlank(message, "请求参数不合法");
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    public record ErrorBody(int code, String message, Object data) {
    }
}
