package com.example.petpojo.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.petpojo.entity.enums.CommonEnum.ArticleStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



/**
 * @author 33185
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("articles")
@Schema(description = "文章实体")
public class Articles  implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "文章ID")
    private Integer id;

    @Schema(description = "标题")
    private String title;
    @Schema(description = "内容")
    private String content;
    @Schema(description = "作者")
    private String author;
    @Schema(description = "封面图片URL")
    private String coverImage;
    @Schema(description = "浏览次数")
    private Integer viewCount;
    @Schema(description = "文章状态")
    private ArticleStatusEnum status;

    @TableField(value = "shelter_id")
    @Schema(description = "收容所ID，平台文章可为null")
    private Integer shelterId;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
