package com.example.petpojo.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author 33185
 */
@Data
@Schema(description = "宠物信息更新DTO")
public class PetUpdateDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @Schema(description = "宠物ID", example = "1", hidden = true)
    private Long pid;
    @NotBlank(message = "名字不能为空")
    @Size(max = 10, message = "名称长度不能超过10字符")
    @Schema(description = "宠物名称", example = "旺财")
    private String name;

    @NotNull(message = "物种ID不能为空")
    @Positive(message = "物种ID必须为正数")
    @Schema(description = "物种ID", example = "1")
    private Integer speciesId;

    @NotNull(message = "品种ID不能为空")
    @Positive(message = "品种ID必须为正数")
    @Schema(description = "品种ID（必须属于物种）", example = "1")
    private Integer breedId;
    @NotNull(message = "年龄不能为空")
    @Positive(message = "年龄必须大于0")
    @Max(value = 30, message = "年龄不能超过30岁")
    @Schema(description = "宠物年龄", example = "3")
    private Integer age;
    @NotBlank(message = "性别不能为空")
    @Pattern(regexp = "[雄雌]", message = "性别只能为雄或雌")
    @Schema(description = "宠物性别", example = "雄")
    private String gender;
    @Schema(description = "图片URL")
    @Size(max = 512, message = "图片URL长度不能超过512字符")
    private String imageUrl;
    @Schema(description = "收容所ID", example = "1")
    private Integer shelterId;
}
