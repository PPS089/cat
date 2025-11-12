package com.example.petpojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "修改密码DTO")
public class ChangePasswordDto {
    /**
     * 当前密码
     */
    @NotNull(message = "当前密码不能为空")
    @Schema(description = "当前密码")
    private String currentPassword;
    /**
     * 新密码
     */
    @NotNull(message = "新密码不能为空")
    @Schema(description = "新密码")
    private String newPassword;


}