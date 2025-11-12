package com.example.petpojo.vo;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录历史VO")
public class LoginHistoryVo {

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "用户ID")
    private Integer userId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "登录时间")
    private LocalDateTime loginTime;

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "设备信息")
    private String device;

    @Schema(description = "位置信息")
    private String location;

    @Schema(description = "状态")
    private String status;
}