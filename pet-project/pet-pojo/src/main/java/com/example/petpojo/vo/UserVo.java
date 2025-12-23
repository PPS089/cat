package com.example.petpojo.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author 33185
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户信息VO")
public class UserVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "个人简介")
    private String introduce;

    @Schema(description = "头像URL")
    private String headPic;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "角色")
    private String role;
}
