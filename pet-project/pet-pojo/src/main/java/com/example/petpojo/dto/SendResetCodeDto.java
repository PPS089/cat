package com.example.petpojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "发送重置验证码DTO")
public record SendResetCodeDto(
    @Schema(description = "邮箱地址")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    String email
) {}
