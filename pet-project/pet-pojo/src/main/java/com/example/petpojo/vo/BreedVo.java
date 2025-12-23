package com.example.petpojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "品种VO")
public class BreedVo {

    @Schema(description = "品种ID")
    private Integer id;

    @Schema(description = "所属物种ID")
    private Integer speciesId;

    @Schema(description = "品种名称")
    private String name;

    @Schema(description = "描述")
    private String description;
}

