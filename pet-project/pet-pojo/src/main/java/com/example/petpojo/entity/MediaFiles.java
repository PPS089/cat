package com.example.petpojo.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 媒体文件实体
 * 存储用户上传的图片、视频等媒体文件信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("media_files")
public class MediaFiles implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer mid;

    private Integer recordId;
    
    private Integer uid;
    
    private String fileName;
    
    private String filePath;
    
    private String mediaType;
    
    private Long fileSize;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
