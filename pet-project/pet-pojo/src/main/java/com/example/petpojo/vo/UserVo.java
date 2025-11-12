package com.example.petpojo.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户信息VO")
public class UserVo {

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

}