package com.example.petpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author 33185
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文章信息VO")
/*
  文章信息VO类
  用于封装文章的详细信息
 */
public class ArticlesVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文章ID
     */
    @Schema(description = "文章ID")
    private Integer id;

    /**
     * 文章标题
     */
    @Schema(description = "文章标题")
    private String title;

    /**
     * 文章内容
     */
    @Schema(description = "文章内容")
    private String content;

    /**
     * 文章作者
     */
    @Schema(description = "文章作者")
    private String author;

    /**
     * 封面图片URL
     */
    @Schema(description = "封面图片URL")
    private String coverImage;

    /**
     * 文章状态（PUBLISHED/DRAFT）
     */
    @Schema(description = "文章状态")
    private String status;

    /**
     * 浏览次数
     */
    @Schema(description = "浏览次数")
    private Integer viewCount;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
