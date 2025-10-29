package com.example.petpojo.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 事件记录值对象
 * 用于适配前端期望的事件记录数据格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventVo {

    private Integer eid;
    
    private Integer pid;
    
    private Integer uid;
    
    private String eventType;
    
    private LocalDateTime eventTime;
    
    private String mood;
    
    private String description;
    
    private String location;
    
    private String mediaUrl;
    
    private String mediaType;
    
    private List<MediaFileVo> mediaList;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}