package com.example.petcommon.result;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 错误响应结果类，用于返回参数错误详情
 * data字段为Map<String, String>类型，存储参数名和错误信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultMapStringString {

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据，键值对形式存储参数错误信息
     */
    private Map<String, String> data;

    /**
     * 错误响应
     */
    public static ResultMapStringString error(String msg) {
        return new ResultMapStringString(400, msg, null);
    }

    /**
     * 错误响应，自定义状态码
     */
    public static ResultMapStringString error(Integer code, String msg) {
        return new ResultMapStringString(code, msg, null);
    }

    /**
     * 错误响应，自定义状态码和数据
     */
    public static ResultMapStringString error(Integer code, String msg, Map<String, String> data) {
        return new ResultMapStringString(code, msg, data);
    }
}