package com.example.petweb.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.example.petcommon.error.ErrorCode;
import com.example.petcommon.exception.BizException;
import com.example.petcommon.result.Result;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

/**
 * 全局异常处理器，统一处理 Web 层异常
 * @author 33185
 */
@RestControllerAdvice(basePackages = "com.example.petweb.controller")
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Map<String, String>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("参数校验异常: {}", ex.getMessage());
        return buildValidationError(ex.getBindingResult());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Map<String, String>>> handleBindException(BindException ex) {
        log.error("表单参数校验异常: {}", ex.getMessage());
        return buildValidationError(ex.getBindingResult());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Map<String, String>>> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("约束违反异常: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        }
        String firstError = errors.values().iterator().next();
        return error(ErrorCode.VALIDATION_ERROR, firstError, errors);
    }

    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<String>> handleBizException(BizException e) {
        log.error("业务异常：{}", e.getMessage());
        Result<String> body = Result.error(e.getCode(), e.getMessage());
        return new ResponseEntity<>(body, e.getStatus());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<String>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("参数异常：", e);
        return error(ErrorCode.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<String>> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常：", e);
        return error(ErrorCode.INTERNAL_ERROR, "运行时异常：" + e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<String>> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error("请求参数缺失异常: {}", e.getMessage());
        return error(ErrorCode.BAD_REQUEST, "请求参数缺失: " + e.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<String>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("参数类型转换异常: {}", e.getMessage());
        String requiredTypeName = e.getRequiredType() == null ? "未知类型" : e.getRequiredType().getSimpleName();
        return error(ErrorCode.BAD_REQUEST, "参数类型错误: " + e.getName() + " 应该是 " + requiredTypeName);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("请求体解析异常: {}", e.getMessage());
        return error(ErrorCode.BAD_REQUEST, "请求体格式错误，请检查JSON格式");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<String>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("请求方法不支持: {}", e.getMethod());
        return error(ErrorCode.METHOD_NOT_ALLOWED, ErrorCode.METHOD_NOT_ALLOWED.getMessage() + ": " + e.getMethod());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Result<String>> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        log.error("媒体类型不支持: {}", e.getContentType());
        return error(ErrorCode.UNSUPPORTED_MEDIA_TYPE, ErrorCode.UNSUPPORTED_MEDIA_TYPE.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Result<String>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.error("接口不存在: {}", e.getRequestURL());
        return error(ErrorCode.NOT_FOUND, "接口不存在: " + e.getRequestURL());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<String>> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        String message = e.getMessage() == null ? ErrorCode.INTERNAL_ERROR.getMessage() : e.getMessage();
        return error(ErrorCode.INTERNAL_ERROR, "系统异常: " + message);
    }

    private ResponseEntity<Result<Map<String, String>>> buildValidationError(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        bindingResult.getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        String allErrors = errors.values().stream().collect(Collectors.joining(", "));
        return error(ErrorCode.VALIDATION_ERROR, allErrors, errors);
    }

    private <T> ResponseEntity<Result<T>> error(ErrorCode code, String message) {
        return error(code, message, null);
    }

    private <T> ResponseEntity<Result<T>> error(ErrorCode code, String message, T data) {
        Result<T> body = Result.error(code.getCode(), message, data);
        return ResponseEntity.status(code.getStatus()).body(body);
    }
}
