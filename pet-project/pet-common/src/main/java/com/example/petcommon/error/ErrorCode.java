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
    
    // 寄养相关错误码
    FOSTER_NOT_FOUND(404030, "寄养记录不存在", HttpStatus.NOT_FOUND),
    FOSTER_PET_IS_FOSTERING(400030, "宠物正在寄养中，无法删除记录", HttpStatus.BAD_REQUEST),
    FOSTER_DELETE_FAILED(500030, "寄养记录删除失败", HttpStatus.INTERNAL_SERVER_ERROR),
    FOSTER_CREATE_FAILED(500031, "寄养记录创建失败", HttpStatus.INTERNAL_SERVER_ERROR),
    FOSTER_END_FAILED(500032, "寄养记录结束失败", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // 登录历史相关错误码
    LOGIN_HISTORY_CONVERT_FAILED(500040, "登录记录转换失败", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // 媒体文件相关错误码
    MEDIA_FILE_NOT_FOUND(404040, "媒体文件不存在", HttpStatus.NOT_FOUND),
    MEDIA_FILE_UPLOAD_FAILED(500041, "媒体文件上传失败", HttpStatus.INTERNAL_SERVER_ERROR),
    MEDIA_FILE_DELETE_FAILED(500042, "媒体文件删除失败", HttpStatus.INTERNAL_SERVER_ERROR),
    MEDIA_FILE_SIZE_EXCEEDED(400040, "媒体文件大小超出限制", HttpStatus.BAD_REQUEST),
    MEDIA_FILE_TYPE_UNSUPPORTED(400041, "不支持的媒体文件类型", HttpStatus.BAD_REQUEST),
    MEDIA_FILE_COUNT_EXCEEDED(400042, "媒体文件数量超出限制", HttpStatus.BAD_REQUEST),
    
    VALIDATION_ERROR(400000, "参数校验异常", HttpStatus.BAD_REQUEST),
    BAD_REQUEST(400000, "请求参数错误", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(401000, "未授权", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(403000, "没有权限", HttpStatus.FORBIDDEN),
    NOT_FOUND(404000, "资源不存在", HttpStatus.NOT_FOUND),
    INTERNAL_ERROR(500000, "服务器内部错误", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(int code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
