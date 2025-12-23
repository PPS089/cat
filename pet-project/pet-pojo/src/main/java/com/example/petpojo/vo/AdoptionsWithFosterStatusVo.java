package com.example.petpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 带寄养状态的领养信息VO类
 * 用于封装领养记录的详细信息，包括宠物信息、收容所信息和寄养状态
 * @author 33185
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "带寄养状态的领养信息VO")
public class AdoptionsWithFosterStatusVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 领养记录ID
     */
    @Schema(description = "领养记录ID")
    private Integer id;
    
    /**
     * 领养日期
     */
    @Schema(description = "领养日期")
    private LocalDateTime adoptDate;
    
    /**
     * 宠物信息
     */
    @Schema(description = "宠物信息")
    private PetInfo pet;
    
    /**
     * 收容所信息
     */
    @Schema(description = "收容所信息")
    private ShelterInfo shelter;
    
    /**
     * 寄养状态 (active/available/ended)
     */
    @Schema(description = "寄养状态 (active/available/ended)")
    private String fosterStatus;
    
    /**
     * 宠物信息内部类
     * 用于封装宠物的详细信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "宠物信息")
    public static class PetInfo {
        /**
         * 宠物ID
         */
        @Schema(description = "宠物ID")
        private Integer id;
        /**
         * 宠物名称
         */
        @Schema(description = "宠物名称")
        private String name;
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
    }
    
    /**
     * 收容所信息内部类
     * 用于封装收容所的详细信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "收容所信息")
    public static class ShelterInfo {
        /**
         * 收容所ID
         */
        @Schema(description = "收容所ID")
        private Integer id;
        /**
         * 收容所名称
         */
        @Schema(description = "收容所名称")
        private String name;
        /**
         * 收容所位置
         */
        @Schema(description = "收容所位置")
        private String location;
    }
}