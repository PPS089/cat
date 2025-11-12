package com.example.petpojo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 用户更新DTO
 */
@Data
@Schema(description = "用户更新DTO")
public class UserUpdateDto {

    @Schema(description = "用户名")
    private String userName;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "个人简介")
    private String introduce;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱")
    private String email;

}