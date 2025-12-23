package com.example.petpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 寄养信息VO类
 * 用于封装寄养记录的详细信息，包括宠物信息和收容所信息
 * @author 33185
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "寄养信息VO")
public class FostersVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 寄养记录ID
     */
    @Schema(description = "寄养记录ID")
    private Integer id;

    @Schema(description = "申请人用户ID")
    private Integer applicantId;

    @Schema(description = "申请人用户名")
    private String applicantName;

    @Schema(description = "申请人手机号")
    private String applicantPhone;

    /**
     * 记录创建时间（寄养开始）
     */
    @Schema(description = "记录创建时间（寄养开始）")
    private LocalDateTime createTime;

    /**
     * 记录更新时间（寄养结束）
     */
    @Schema(description = "记录更新时间（寄养结束）")
    private LocalDateTime updateTime;

    /**
     * 寄养开始时间（业务字段）
     */
    @Schema(description = "寄养开始时间")
    private LocalDateTime startDate;

    /**
     * 寄养结束时间（业务字段）
     */
    @Schema(description = "寄养结束时间")
    private LocalDateTime endDate;

    /**
     * 寄养状态 (active/completed/archived)
     */
    @Schema(description = "寄养状态 (active/completed/archived)")
    private String status;

    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;

    @Schema(description = "审核备注")
    private String reviewNote;

    /**
     * 宠物信息 - 匹配前端期望的嵌套结构
     */
    @Schema(description = "宠物信息")
    private PetInfo pet;
    
    /**
     * 收容所信息
     */
    @Schema(description = "收容所信息")
    private ShelterInfo shelter;

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
        private Integer pid;
        /**
         * 宠物名称
         */
        @Schema(description = "宠物名称")
        private String name;
        /**
         * 宠物品类（猫/狗）
         */
        @Schema(description = "宠物品类")
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
         * 宠物当前状态
         */
        @Schema(description = "宠物当前状态")
        private String status;
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
        private Integer sid;
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
