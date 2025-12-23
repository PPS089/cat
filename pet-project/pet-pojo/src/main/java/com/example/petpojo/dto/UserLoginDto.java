package com.example.petpojo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author 33185
 */
@Data
@Schema(description = "用户登录DTO")
public class UserLoginDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "用户名不能为空")
    @JsonProperty("username")
    @Size(min = 1, max = 10, message = "用户名长度必须在1-10位之间")
    @Schema(description = "用户名", example = "john_doe")
    private String userName;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 12, message = "密码长度必须在6-12位之间")
    @JsonProperty("password")
    @Schema(description = "密码", example = "password123")
    private String passwordHash;
}