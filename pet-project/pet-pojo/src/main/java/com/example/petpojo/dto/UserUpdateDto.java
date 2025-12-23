package com.example.petpojo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 用户更新DTO
 * @author 33185
 */
@Data
@Schema(description = "用户更新DTO")
public class UserUpdateDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户名")
    @Size(min = 1, max = 10, message = "用户名长度需在1-10字符内")
    private String userName;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "个人简介")
    @Size(max = 200, message = "个人简介长度不能超过200字符")
    private String introduce;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱")
    private String email;

}
