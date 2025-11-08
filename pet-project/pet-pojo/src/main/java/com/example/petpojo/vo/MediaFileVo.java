package com.example.petpojo.vo;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 媒体文件值对象
 * 用于前端展示媒体文件信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaFileVo {

    private Integer mid;
    
    private Integer recordId;
    
    private Integer uid;
    
    private String fileName;
    
    private String filePath;
    
    private String mediaType;
    
    private Long fileSize;
    
    private LocalDateTime createdAt;
}