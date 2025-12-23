package com.example.petpojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "物种VO")
public class SpeciesVo {

    @Schema(description = "物种ID")
    private Integer id;

    @Schema(description = "物种名称")
    private String name;

    @Schema(description = "描述")
    private String description;
}

