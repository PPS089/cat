package com.example.petpojo.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.petpojo.entity.enums.CommonEnum;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



/**
 * @author 33185
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("pets")
@Schema(description = "宠物实体")
public class Pets implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "宠物ID")
    private Integer pid;

    @Schema(description = "宠物名称")
    private String name;

    @TableField(value = "species_id")
    @Schema(description = "物种ID")
    private Integer speciesId;

    @TableField(value = "breed_id")
    @Schema(description = "品种ID")
    private Integer breedId;

    // 非数据库字段，用于存储JOIN查询的物种/品种名称
    @TableField(exist = false)
    @Schema(description = "物种名称（联表字段）")
    private String species;

    @TableField(exist = false)
    @Schema(description = "品种名称（联表字段）")
    private String breed;
    @Schema(description = "年龄")
    private Integer age;
    @Schema(description = "性别")
    private String gender;
    @TableField(value = "img_url")
    @Schema(description = "图片URL")
    private String imageUrl;
    @EnumValue      // 告诉 MP 用 code 存库
    @Schema(description = "宠物状态")
    private CommonEnum.PetStatusEnum status;
    @Schema(description = "收容所ID")
    private Integer shelterId;
    
    // 非数据库字段，用于存储JOIN查询的收容所信息
    @TableField(exist = false)
    @Schema(description = "收容所名称（联表字段）")
    private String shelterName;
    
    @TableField(exist = false)
    @Schema(description = "收容所地址（联表字段）")
    private String shelterAddress;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
