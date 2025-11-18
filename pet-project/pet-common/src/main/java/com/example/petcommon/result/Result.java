package com.example.petcommon.result;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 通用响应结果类
 * 统一 API 响应格式
 */
@Data
@NoArgsConstructor
public class Result<T> {

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 构造函数
     */
    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 成功响应
     */
    public static Result<Void> success() {
        return new Result<>(200, "success", null);
    }

    /**
     * 成功响应，带数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 错误响应
     */
    public static Result<String> error(String msg) {
        return new Result<>(500, msg, null);
    }


    /**
     * 错误响应，自定义状态码
     */
    public static Result<String> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    /**
     * 错误响应，自定义状态码和数据
     */
    public static <T> Result<T> error(Integer code, String msg, T data) {
        return new Result<>(code, msg, data);
    }
}