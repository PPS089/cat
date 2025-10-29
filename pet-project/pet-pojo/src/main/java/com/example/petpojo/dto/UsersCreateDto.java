package com.example.petpojo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UsersCreateDto {
    @NotBlank(message = "用户名不能为空")
    @JsonProperty("username")
    private String userName;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?![a-zA-Z]+$)(?![A-Z0-9]+$)(?![a-z0-9]+$)\\S{6,12}$", message = "密码必须包含大小写字母、数字和特殊字符，长度6-12位")
    @JsonProperty("password")
    private String passwordHash;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
}
