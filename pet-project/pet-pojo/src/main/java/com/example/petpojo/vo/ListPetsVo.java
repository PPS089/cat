package com.example.petpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "宠物列表VO")
public class ListPetsVo {
    @Schema(description = "宠物ID")
    private Integer pid; // 宠物ID字段，前端期望pid而不是id
    @Schema(description = "宠物名称")
    private String name;
    @Schema(description = "宠物物种")
    private String species; // 物种字段，前端需要
    @Schema(description = "宠物品种")
    private String breed;
    @Schema(description = "宠物年龄")
    private Integer age;
    @Schema(description = "宠物性别")
    private String gender; // 需要确保是"公"/"母"格式
    @Schema(description = "图片URL")
    private String imgUrl; // 图片URL字段，前端需要
    @Schema(description = "收容所名称")
    private String shelterName;
    @Schema(description = "收容所地址")
    private String shelterAddress;
    @Schema(description = "宠物状态")
    private String status; // 宠物状态字段，需要是UNADOPTED等值
}