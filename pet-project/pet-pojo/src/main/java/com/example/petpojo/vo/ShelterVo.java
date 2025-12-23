package com.example.petpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author 33185
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "收容所VO")
public class ShelterVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @Schema(description = "收容所ID")
    private Integer sid;
    @Schema(description = "收容所名称")
    private String shelterName;
    @Schema(description = "收容所地址")
    private String shelterAddress;
}