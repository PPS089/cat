package com.example.petpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdoptionsVo {
    /**
     * 领养记录
     */
    private Integer aid;
    private LocalDateTime adoptionDate;
    /**
     * 宠物信息
     */
    private Integer pid;
    private String name;
    private String species;
    private String breed;
    private Integer age;
    private String gender;
    private String image;
    /**
     * 收容所信息
     */
    private Integer sid;
    private String sname;
    private String location;
    /**
     * 寄养状态
     */
    private Boolean isFostering;
    
    /**
     * 宠物状态（FOSTERING/ADOPTED等）
     */
    private String petStatus;

}


//adoptions: [{
//id: number,                    // 领养记录ID
//pet: {
//id: number,                  // 宠物ID
//name: string,                // 宠物名称 (显示用)
//breed: string,               // 品种 (显示用)
//age: number,                 // 年龄 (显示用)
//gender: string,              // 性别 (显示用)
//image: string                // 图片URL (显示用)
//  },
//shelter: {
//id: number,                  // 收容所ID
//name: string,                // 收容所名称 (显示用)
//location: string              // 收容所地址 (显示用)
//  },
//adoptDate: string,             // 领养日期 (格式化显示用)
//foster_status: string          // 寄养状态 (active/available/ended)
//}]