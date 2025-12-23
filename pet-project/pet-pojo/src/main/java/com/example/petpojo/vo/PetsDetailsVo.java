package com.example.petpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author 33185
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "宠物详细信息VO")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PetsDetailsVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "宠物ID")
    private Integer pid;

    @Schema(description = "宠物名称")
    private String name;

    @Schema(description = "物种ID")
    private Integer speciesId;

    @Schema(description = "宠物物种")
    private String species;

    @Schema(description = "品种ID")
    private Integer breedId;

    @Schema(description = "宠物品种")
    private String breed;

    @Schema(description = "宠物年龄")
    private Integer age;

    @Schema(description = "宠物性别")
    private String gender;

    @Schema(description = "宠物状态")
    private String status;

    @Schema(description = "宠物图片URL")
    private String image;

    @Schema(description = "收容所名称")
    private String shelterName;

    @Schema(description = "收容所地址")
    private String shelterAddress;

    @Schema(description = "收容所ID")
    private Integer shelterId;
}
