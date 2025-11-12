package com.example.petpojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "健康信息VO")
public class HealthVo {
    /**
     * 宠物健康信息
     */
    @Schema(description = "健康ID")
    private Integer healthId;
    @Schema(description = "健康类型")
    private String healthType;
    @Schema(description = "健康描述")
    private String description;
    @Schema(description = "检查日期")
    private LocalDateTime checkDate;
    @Schema(description = "提醒时间")
    private LocalDateTime reminderTime;
    @Schema(description = "健康状态")
    private String healthStatus;


     @Schema(description = "宠物ID")
     private Integer petId;
     @Schema(description = "宠物名称")
     private String petName;




}