package com.example.petpojo.dto;

import lombok.Data;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 记录宠物事件
 */
@Data
@Schema(description = "宠物记录DTO")
public class RecordDto {
    @Schema(description = "宠物ID")
    private Integer pid;
    
    @Schema(description = "事件类型")
    private String eventType;
    
    @Schema(description = "情绪状态")
    private String mood;
    
    @Schema(description = "描述")
    private String description;
    
    @Schema(description = "位置")
    private String location;
    
    @Schema(description = "记录时间")
    private LocalDateTime recordTime;
}