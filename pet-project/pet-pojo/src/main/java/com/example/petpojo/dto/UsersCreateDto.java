package com.example.petpojo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author 33185
 */
@Data
@Schema(description = "用户创建DTO")
public class UsersCreateDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @NotBlank(message = "用户名不能为空")
    @Size(min = 1, max = 10, message = "用户名长度需在1-10字符内")
    @JsonProperty("username")
    @Schema(description = "用户名", example = "john_doe")
    private String userName;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?![a-zA-Z]+$)(?![A-Z0-9]+$)(?![a-z0-9]+$)\\S{6,12}$", message = "密码必须包含大小写字母、数字和特殊字符，长度6-12位")
    @JsonProperty("password")
    @Schema(description = "密码", example = "Password123!")
    private String passwordHash;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13812345678")
    private String phone;
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "john@example.com")
    private String email;
    
}
