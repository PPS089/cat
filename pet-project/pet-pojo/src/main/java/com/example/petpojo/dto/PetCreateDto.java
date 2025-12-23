package com.example.petpojo.dto;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author 33185
 */
@Data
@Schema(description = "宠物创建DTO")
public class PetCreateDto {
    @NotBlank
    @Size(max = 10, message = "名称长度不能超过10字符")
    @Schema(description = "宠物名称", example = "旺财")
    private String name;

    @NotNull
    @Positive(message = "物种ID必须为正数")
    @Schema(description = "物种ID", example = "1")
    private Integer speciesId;

    @NotNull
    @Positive(message = "品种ID必须为正数")
    @Schema(description = "品种ID（必须属于物种）", example = "1")
    private Integer breedId;
    @NotNull
    @Positive(message = "年龄必须大于0")
    @Max(value = 30, message = "年龄不能超过30岁")
    @Schema(description = "年龄", example = "3")
    private Integer age;
    @NotBlank
    @Pattern(regexp = "[雄雌]", message = "性别只能为雄或雌")
    @Schema(description = "性别（雄/雌）", example = "雄")
    private String gender;
    @Size(max = 512, message = "图片URL长度不能超过512字符")
    @Schema(description = "图片URL", example = "https://example.com/pet.jpg")
    private String imageUrl;
    @NotNull
    @Schema(description = "收容所ID", example = "1")
    private Integer shelterId;
    @Schema(description = "宠物状态", example = "AVAILABLE")
    private String status;
}
