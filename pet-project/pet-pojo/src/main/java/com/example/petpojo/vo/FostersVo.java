package com.example.petpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PetInfo {
        private Integer pid;
        private String name;
        private String breed;
        private Integer age;
        private String gender;
        private String image;
    }
}