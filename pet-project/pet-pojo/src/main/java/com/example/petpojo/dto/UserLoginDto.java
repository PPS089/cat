package com.example.petpojo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "用户登录DTO")
public class UserLoginDto {

    @NotBlank(message = "用户名不能为空")
    @JsonProperty("username")
    @Schema(description = "用户名", example = "john_doe")
    private String userName;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    @JsonProperty("password")
    @Schema(description = "密码", example = "password123")
    private String passwordHash;
}