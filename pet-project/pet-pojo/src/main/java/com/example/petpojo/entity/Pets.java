package com.example.petpojo.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.*;
import com.example.petpojo.entity.enums.CommonEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("pets")
public class Pets implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer pid;

    private String name;
    private String species;
    private String breed;
    private Integer age;
    private String gender;
    @TableField(value = "img_url")
    private String imageUrl;
    @EnumValue      // 告诉 MP 用 code 存库
    private CommonEnum.PetStatusEnum status;
    private Integer shelterId;
    
    // 非数据库字段，用于存储JOIN查询的收容所信息
    @TableField(exist = false)
    private String shelterName;
    
    @TableField(exist = false)
    private String shelterAddress;
}