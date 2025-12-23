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
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), null);
    }

    /**
     * 成功响应，带数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    /**
     * 失败响应
     */
    public static Result<Void> fail() {
        return new Result<>(ResultCode.FAIL.getCode(), ResultCode.FAIL.getMsg(), null);
    }

    /**
     * 失败响应，带自定义消息
     */
    public static Result<String> fail(String msg) {
        return new Result<>(ResultCode.FAIL.getCode(), msg, null);
    }

    /**
     * 错误响应（兼容旧命名）
     */
    public static Result<String> error(String msg) {
        return new Result<>(ResultCode.FAIL.getCode(), msg, null);
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
