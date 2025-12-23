package com.example.petpojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "品种创建DTO（基于物种添加）")
public class BreedCreateDto {

    @NotBlank(message = "品种名称不能为空")
    @Size(max = 50, message = "品种名称长度不能超过50字符")
    @Schema(description = "品种名称", example = "金毛寻回犬")
    private String name;

    @Size(max = 255, message = "描述长度不能超过255字符")
    @Schema(description = "描述")
    private String description;
}

