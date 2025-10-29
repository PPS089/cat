package com.example.petpojo.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 记录宠物事件
 */
@Data
public class RecordDto {
    private Integer pid;
    
    private Integer uid;
    
    private String eventType;
    
    private String mood;
    
    private String description;
    
    private String location;
    
    private LocalDateTime recordTime;
    
    private String mediaUrl;
    
    private String mediaType;
}
