package com.example.petpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 寄养信息VO类
 * 用于封装寄养记录的详细信息，包括宠物信息和收容所信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FostersVo {
    /**
     * 寄养记录ID
     */
    private Integer id;

    /**
     * 寄养开始日期
     */
    private LocalDateTime startDate;

    /**
     * 寄养结束日期
     */
    private LocalDateTime endDate;

    /**
     * 寄养状态 (ongoing/ended)
     */
    private String status;

    /**
     * 宠物信息 - 匹配前端期望的嵌套结构
     */
    private PetInfo pet;
    
    /**
     * 收容所信息
     */
    private ShelterInfo shelter;

    /**
     * 宠物信息内部类
     * 用于封装宠物的详细信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PetInfo {
        /**
         * 宠物ID
         */
        private Integer pid;
        /**
         * 宠物名称
         */
        private String name;
        /**
         * 宠物品种
         */
        private String breed;
        /**
         * 宠物年龄
         */
        private Integer age;
        /**
         * 宠物性别
         */
        private String gender;
        /**
         * 宠物图片URL
         */
        private String image;
    }
    
    /**
     * 收容所信息内部类
     * 用于封装收容所的详细信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShelterInfo {
        /**
         * 收容所ID
         */
        private Integer sid;
        /**
         * 收容所名称
         */
        private String name;
        /**
         * 收容所位置
         */
        private String location;
    }
}