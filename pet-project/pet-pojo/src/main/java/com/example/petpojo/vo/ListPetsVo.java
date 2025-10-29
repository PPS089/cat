package com.example.petpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListPetsVo {
    private Integer pid; // 宠物ID字段，前端期望pid而不是id
    private String name;
    private String species; // 物种字段，前端需要
    private String breed;
    private Integer age;
    private String gender; // 需要确保是"公"/"母"格式
    private String imageUrl;
    private String shelterName;
    private String shelterAddress;
    private String status; // 宠物状态字段，需要是UNADOPTED等值
}
