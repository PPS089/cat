package com.example.petpojo.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 媒体文件实体
 * 存储用户上传的图片、视频等媒体文件信息
 * @author 33185
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("media_files")
@Schema(description = "媒体文件实体")
public class MediaFiles implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "媒体文件ID")
    private Integer mid;

    @NotNull(message = "记录ID不能为空")
    @Positive(message = "记录ID必须为正数")
    @Schema(description = "记录ID")
    private Integer recordId;
    
    @NotNull(message = "用户ID不能为空")
    @Positive(message = "用户ID必须为正数")
    @Schema(description = "用户ID")
    private Integer uid;
    
    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名长度不能超过255字符")
    @Schema(description = "文件名")
    private String fileName;
    
    @NotBlank(message = "文件路径不能为空")
    @Size(max = 512, message = "文件路径长度不能超过512字符")
    @Schema(description = "文件路径")
    private String filePath;
    
    @NotBlank(message = "媒体类型不能为空")
    @Pattern(regexp = "image|video", message = "媒体类型必须为 image 或 video")
    @Schema(description = "媒体类型（image/video）")
    private String mediaType;
    
    @NotNull(message = "文件大小不能为空")
    @PositiveOrZero(message = "文件大小不能为负数")
    @Schema(description = "文件大小（字节）")
    private Long fileSize;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @PastOrPresent(message = "创建时间不能晚于当前时间")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @PastOrPresent(message = "更新时间不能晚于当前时间")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
