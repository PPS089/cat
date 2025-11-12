package com.example.petcommon.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    USERNAME_EXISTS(400001, "用户名已存在", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(404001, "用户不存在", HttpStatus.NOT_FOUND),
    PASSWORD_INCORRECT(401001, "密码错误", HttpStatus.UNAUTHORIZED),
    JWT_CONFIG_ERROR(500001, "JWT配置异常", HttpStatus.INTERNAL_SERVER_ERROR),
    TOKEN_INVALID_OR_EXPIRED(401002, "token无效或已过期", HttpStatus.UNAUTHORIZED),
    FILE_SAVE_FAILED(500010, "文件保存失败", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_UPLOAD_FAILED(500011, "文件上传失败", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_UPDATE_FAILED(500012, "用户更新失败", HttpStatus.INTERNAL_SERVER_ERROR),
    PET_NOT_FOUND(404010, "宠物不存在", HttpStatus.NOT_FOUND),
    ADOPTION_CREATE_FAILED(500020, "领养信息创建失败", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR(400000, "参数校验异常", HttpStatus.BAD_REQUEST),
    BAD_REQUEST(400000, "请求参数错误", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(401000, "未授权", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(403000, "没有权限", HttpStatus.FORBIDDEN),
    NOT_FOUND(404000, "资源不存在", HttpStatus.NOT_FOUND),
    INTERNAL_ERROR(500000, "服务器内部错误", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String defaultMessage;
    private final HttpStatus status;

    ErrorCode(int code, String defaultMessage, HttpStatus status) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
