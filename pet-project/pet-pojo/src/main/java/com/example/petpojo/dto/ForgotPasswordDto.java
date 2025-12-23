package com.example.petpojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "忘记密码DTO")
public record ForgotPasswordDto(
    @Schema(description = "邮箱地址")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    String email,

    @Schema(description = "验证码")
    @NotBlank(message = "验证码不能为空")
    @Size(min = 4, max = 8, message = "验证码长度应在4-8位之间")
    String code,

    @Schema(description = "新密码")
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 12, message = "新密码长度必须在6-12位之间")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "新密码必须包含大小写字母和数字")
    String newPassword
) {}
