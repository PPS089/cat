package com.example.petpojo.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 事件记录值对象
 * 用于适配前端期望的事件记录数据格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "事件记录VO")
public class EventVo {

    @Schema(description = "事件ID")
    private Integer eid;
    
    @Schema(description = "宠物ID")
    private Integer pid;
    
    @Schema(description = "用户ID")
    private Integer uid;
    
    @Schema(description = "事件类型")
    private String eventType;
    
    @Schema(description = "事件时间")
    private LocalDateTime eventTime;
    
    @Schema(description = "情绪状态")
    private String mood;
    
    @Schema(description = "描述")
    private String description;
    
    @Schema(description = "位置")
    private String location;
    
    @Schema(description = "媒体文件列表")
    private List<MediaFileVo> mediaList;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}