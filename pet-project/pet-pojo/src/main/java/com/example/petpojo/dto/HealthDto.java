package com.example.petpojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "健康信息DTO")
public class HealthDto {

    @NotNull(message = "宠物ID不能为空")
    @Schema(description = "宠物ID")
    private Integer pid;
    
    @NotNull(message = "检查日期不能为空")
    @Schema(description = "检查日期")
    private LocalDateTime checkDate;
    
    @Schema(description = "健康类型")
    private String healthType;
    
    @Schema(description = "健康描述")
    private String description;
    
    @Schema(description = "提醒时间")
    private LocalDateTime reminderTime;
    
    @Schema(description = "状态")
    private String status;
}