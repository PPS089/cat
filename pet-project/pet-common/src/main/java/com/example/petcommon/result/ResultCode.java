package com.example.petcommon.result;

import lombok.Getter;

/**
 * 基础响应码
 */
@Getter
public enum ResultCode {
    SUCCESS(200, "成功"),
    FAIL(201, "失败");

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}

