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
@Schema(description = "宠物列表VO")
public class ListPetsVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
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
    @Schema(description = "图片URL")
    private String image;
    @Schema(description = "收容所名称")
    private String shelterName;
    @Schema(description = "收容所地址")
    private String shelterAddress;
    // 宠物状态字段，取值 AVAILABLE/ADOPTED/FOSTERING
    @Schema(description = "宠物状态")
    private String status;
}
