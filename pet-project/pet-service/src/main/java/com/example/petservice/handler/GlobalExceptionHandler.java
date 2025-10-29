package com.example.petservice.handler;

import com.example.petcommon.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理应用程序中的异常
 */
@RestControllerAdvice(basePackages = "com.example.petweb.controller")
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理方法参数校验异常（@Valid注解校验失败）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("参数校验异常: {}", ex.getMessage());
        
        // 收集所有字段错误
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        
        // 返回所有错误信息拼接作为消息，所有错误作为数据
        String allErrors = errors.values().stream().collect(Collectors.joining(", "));
        return Result.error(400, allErrors, errors);
    }

    /**
     * 处理约束违反异常（@Validated注解校验失败）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("约束违反异常: {}", ex.getMessage());
        
        // 收集所有约束违反错误
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        }
        
        // 返回第一个错误信息作为消息，所有错误作为数据
        String firstError = errors.values().iterator().next();
        return Result.error(400, firstError, errors);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("参数异常：", e);
        return Result.error(e.getMessage());
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常：", e);
        return Result.error("运行时异常：" + e.getMessage());
    }

    /**
     * 处理请求参数缺失异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error("请求参数缺失异常: {}", e.getMessage());
        return Result.error(400, "请求参数缺失: " + e.getParameterName());
    }

    /**
     * 处理请求参数类型转换异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("参数类型转换异常: {}", e.getMessage());
        return Result.error(400, "参数类型错误: " + e.getName() + " 应该是 " + e.getRequiredType().getSimpleName() + " 类型");
    }

    /**
     * 处理请求体解析异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("请求体解析异常: {}", e.getMessage());
        return Result.error(400, "请求体格式错误，请检查JSON格式");
    }

    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {

        
        String exceptionType = e.getClass().getSimpleName();
        String exceptionMessage = e.getMessage();
        
        log.error("=== 系统异常捕获 ===");
        log.error("异常类型: {}", exceptionType);
        log.error("异常消息: {}", exceptionMessage);
        log.error("异常堆栈:", e);
        
        // 如果是特定的异常，返回更详细的信息
        if (exceptionMessage != null && exceptionMessage.contains("No endpoint")) {
            log.error("检测到 'No endpoint' 错误，这通常是自定义异常消息");
            return Result.error("系统异常：" + exceptionMessage + " (异常类型: " + exceptionType + ")");
        }
        
        return Result.error("系统异常：" + exceptionMessage);
    }
}