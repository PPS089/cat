package com.example.petpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "收容所VO")
public class ShelterVo {
    @Schema(description = "收容所ID")
    private Integer sid;
    @Schema(description = "收容所名称")
    private String shelterName;
    @Schema(description = "收容所地址")
    private String shelterAddress;
}