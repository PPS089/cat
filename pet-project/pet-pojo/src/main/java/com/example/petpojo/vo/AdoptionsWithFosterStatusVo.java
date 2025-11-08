package com.example.petpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 带寄养状态的领养信息VO类
 * 用于封装领养记录的详细信息，包括宠物信息、收容所信息和寄养状态
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdoptionsWithFosterStatusVo {
    /**
     * 领养记录ID
     */
    private Integer id;
    
    /**
     * 领养日期
     */
    private LocalDateTime adoptDate;
    
    /**
     * 宠物信息
     */
    private PetInfo pet;
    
    /**
     * 收容所信息
     */
    private ShelterInfo shelter;
    
    /**
     * 寄养状态 (active/available/ended)
     */
    private String fosterStatus;
    
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
        private Integer id;
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
        private Integer id;
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