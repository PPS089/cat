package com.example.petpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "宠物详细信息VO")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PetsDetailsVo {

     @Schema(description = "ID")
     private Integer id;
    @Schema(description = "宠物ID")
    private Integer pid;
    @Schema(description = "宠物名称")
    private String name;
    @Schema(description = "宠物物种")
    private String species;
    @Schema(description = "宠物品种")
    private String breed;
    @Schema(description = "宠物年龄")
    private Integer age;
    @Schema(description = "宠物性别")
    private String gender;
    @Schema(description = "宠物状态")
    private String status;
    @Schema(description = "宠物描述")
    private String description;
    @Schema(description = "宠物图片URL")
    private String image;
    @Schema(description = "健康状态")
    private String healthStatus;
    @Schema(description = "是否已接种疫苗")
    private Boolean vaccinated;
    @Schema(description = "是否已绝育")
    private Boolean spayed;
    @Schema(description = "领养费用")
    private Integer adoptionFee;
    @Schema(description = "寄养费用")
    private Integer fosterFee;
    @Schema(description = "收容所名称")
    private String shelterName;
    @Schema(description = "收容所地址")
    private String shelterAddress;
    @Schema(description = "收容所ID")
    private Long shelterId;

    
}