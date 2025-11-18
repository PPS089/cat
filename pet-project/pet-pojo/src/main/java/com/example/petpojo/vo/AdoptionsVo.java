package com.example.petpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 领养信息VO类
 * 用于封装领养记录的详细信息，包括宠物信息和收容所信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "领养信息VO")
public class AdoptionsVo {
    /**
     * 领养记录ID
     */
    @Schema(description = "领养记录ID")
    private Integer aid;
    /**
     * 领养日期
     */
    @Schema(description = "领养日期")
    private LocalDateTime adoptionDate;
    /**
     * 宠物ID
     */
    @Schema(description = "宠物ID")
    private Integer pid;
    /**
     * 宠物名称
     */
    @Schema(description = "宠物名称")
    private String name;
    /**
     * 宠物物种
     */
    @Schema(description = "宠物物种")
    private String species;
    /**
     * 宠物品种
     */
    @Schema(description = "宠物品种")
    private String breed;
    /**
     * 宠物年龄
     */
    @Schema(description = "宠物年龄")
    private Integer age;
    /**
     * 宠物性别
     */
    @Schema(description = "宠物性别")
    private String gender;
    /**
     * 宠物图片URL
     */
    @Schema(description = "宠物图片URL")
    private String image;
    /**
     * 收容所ID
     */
    @Schema(description = "收容所ID")
    private Integer sid;
    /**
     * 收容所名称
     */
    @Schema(description = "收容所名称")
    private String sname;
    /**
     * 收容所位置
     */
    @Schema(description = "收容所位置")
    private String location;
    /**
     * 是否正在寄养
     */
    @Schema(description = "是否正在寄养")
    private Boolean isFostering;
    
    /**
     * 宠物状态
     */
    @Schema(description = "宠物状态")
    private String status;

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