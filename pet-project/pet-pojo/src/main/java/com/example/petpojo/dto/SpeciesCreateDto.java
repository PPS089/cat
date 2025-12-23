package com.example.petpojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "物种创建DTO")
public class SpeciesCreateDto {

    @NotBlank(message = "物种名称不能为空")
    @Size(max = 50, message = "物种名称长度不能超过50字符")
    @Schema(description = "物种名称", example = "狗")
    private String name;

    @Size(max = 255, message = "描述长度不能超过255字符")
    @Schema(description = "描述")
    private String description;
}

