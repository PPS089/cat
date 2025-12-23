package com.example.petpojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author 33185
 */
@Data
@Schema(description = "文章请求DTO")
public class ArticleRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @NotBlank
    @Size(min = 1, max = 20, message = "标题长度应在2-20字符")
    @Schema(description = "标题", example = "宠物护理小技巧")
    private String title;
    @NotBlank
    @Size(min = 1, max = 500, message = "内容长度应在1-500字符")
    @Schema(description = "内容", example = "今天分享一些日常护理建议...")
    private String content;
    @NotBlank
    @Size(min = 1, max = 10, message = "作者长度应在1-10字符")
    @Schema(description = "作者", example = "admin")
    private String author;
    @Size(max = 512, message = "封面图片URL长度不能超过512字符")
    @Schema(description = "封面图片URL", example = "https://example.com/cover.jpg")
    private String coverImage;
    // PUBLISHED / DRAFT
    @Schema(description = "文章状态（PUBLISHED/DRAFT）", example = "PUBLISHED")
    private String status;
}
