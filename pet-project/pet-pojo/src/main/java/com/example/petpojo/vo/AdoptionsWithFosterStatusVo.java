package com.example.petpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PetInfo {
        private Integer id;
        private String name;
        private String breed;
        private Integer age;
        private String gender;
        private String image;
    }
    
    /**
     * 收容所信息内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShelterInfo {
        private Integer id;
        private String name;
        private String location;
    }
}