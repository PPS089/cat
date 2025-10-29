package com.example.petpojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangePasswordDto {
    /**
     * 当前密码
     */
    @NotNull(message = "当前密码不能为空")
    private String currentPassword;
    /**
     * 新密码
     */
    @NotNull(message = "新密码不能为空")
    private String newPassword;


}
