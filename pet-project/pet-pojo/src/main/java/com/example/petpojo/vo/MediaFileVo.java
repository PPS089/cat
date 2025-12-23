package com.example.petpojo.vo;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 媒体文件值对象
 * 用于前端展示媒体文件信息
 * @author 33185
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "媒体文件VO")
public class MediaFileVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "媒体文件ID")
    private Integer mid;
    
    @Schema(description = "记录ID")
    private Integer recordId;
    
    @Schema(description = "用户ID")
    private Integer uid;
    
    @Schema(description = "文件名")
    private String fileName;
    
    @Schema(description = "文件路径")
    private String filePath;
    
    @Schema(description = "媒体类型")
    private String mediaType;
    
    @Schema(description = "文件大小")
    private Long fileSize;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
