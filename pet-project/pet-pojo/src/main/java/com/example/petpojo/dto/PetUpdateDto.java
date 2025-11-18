package com.example.petpojo.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "宠物信息更新DTO")
public class PetUpdateDto {
    @NotBlank(message = "宠物ID不能为空")
    @Schema(description = "宠物ID", example = "1")
    private Long pid;
    @NotBlank(message = "名字不能为空")
    @Schema(description = "宠物名称", example = "旺财")
    private String name;
    @NotBlank(message = "物种不能为空")
    @Schema(description = "宠物物种", example = "狗")
    private String species;
    @NotBlank(message = "品种不能为空")
    @Schema(description = "宠物品种", example = "金毛")
    private String breed;
    @NotBlank(message = "年龄不能为空")
    @Schema(description = "宠物年龄", example = "3")
    private Integer age;
    @NotBlank(message = "性别不能为空")
    @Schema(description = "宠物性别", example = "公")
    private String gender;

}