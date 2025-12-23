package com.example.petpojo.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 通用枚举类 - 所有业务枚举落库值均为英文，统一大小写规范
 * @author 33185
 */
public class CommonEnum {

    /* ===================== 宠物状态 ===================== */
    @Getter
    @Schema(description = "宠物状态枚举")
    public enum PetStatusEnum {
        // 可领养状态
        @Schema(description = "可领养")
        AVAILABLE("AVAILABLE"),
        // 已被领养状态
        @Schema(description = "已被领养")
        ADOPTED("ADOPTED"),
        // 寄养状态
        @Schema(description = "寄养中")
        FOSTERING("FOSTERING");

        @EnumValue
        @Schema(description = "枚举值")
        private final String code;

        PetStatusEnum(String code) {
            this.code = code;
        }
    }

    /* ===================== 健康类型 ===================== */
    @Getter
    @Schema(description = "健康类型枚举")
    public enum HealthTypeEnum {
        // 疫苗类型
        @Schema(description = "疫苗")
        VACCINE("VACCINE"),
        // 体检类型
        @Schema(description = "体检")
        CHECKUP("CHECKUP"),
        // 手术类型
        @Schema(description = "手术")
        SURGERY("SURGERY"),
        // 疾病类型
        @Schema(description = "疾病")
        DISEASE("DISEASE");

        @EnumValue
        @Schema(description = "枚举值")
        private final String code;

    /**
     * 枚举构造函数
     * @param code 枚举对应的代码值
     */
        HealthTypeEnum(String code) {
            this.code = code;
        }
    }

    /* ===================== 登录状态 ===================== */
    @Getter
    @Schema(description = "登录状态枚举")
    public enum LoginStatusEnum {
    // 定义枚举常量，表示登录成功的状态
        @Schema(description = "登录成功")
        SUCCESS("SUCCESS"),
    // 定义枚举常量，表示登录失败的状态
        @Schema(description = "登录失败")
        FAILED("FAILED");

        @EnumValue
        @Schema(description = "枚举值")
        private final String code;

        LoginStatusEnum(String code) {
            this.code = code;
        }
    }

    /* ===================== 文章状态 ===================== */
    @Getter
    @Schema(description = "文章状态枚举")
    public enum ArticleStatusEnum {
        @Schema(description = "已发布")
        PUBLISHED("PUBLISHED"),
        @Schema(description = "草稿")
        DRAFT("DRAFT");

        @EnumValue
        @Schema(description = "枚举值")
        private final String code;

        ArticleStatusEnum(String code) {
            this.code = code;
        }
    }

    /* ===================== 寄养删除结果 ===================== */
    @Getter
    @Schema(description = "寄养删除结果枚举")
    public enum FosterDeleteResultEnum {
        // 删除成功
        @Schema(description = "删除成功")
        SUCCESS("SUCCESS"),
        // 宠物正在寄养中，无法删除
        @Schema(description = "宠物寄养中，无法删除")
        PET_IS_FOSTERING("PET_IS_FOSTERING"),
        // 数据库删除失败
        @Schema(description = "删除失败")
        DELETE_FAILED("DELETE_FAILED");

        @EnumValue
        @Schema(description = "枚举值")
        private final String code;

        FosterDeleteResultEnum(String code) {
            this.code = code;
        }
    }

    /* ===================== 寄养状态 ===================== */
    /**
     * 寄养状态枚举类
     * 使用@Getter注解为枚举常量自动生成getter方法
     * 使用@EnumValue注解标记该字段用于数据库存储的值
     */
    @Getter
    @Schema(description = "寄养状态枚举")
    public enum FosterStatusEnum {
        // 待审核状态
        @Schema(description = "待审核")
        PENDING("PENDING"),
        // 进行中状态
        @Schema(description = "进行中")
        ONGOING("ONGOING"),
        // 已完成状态
        @Schema(description = "已完成")
        COMPLETED("COMPLETED"),
        // 已拒绝状态
        @Schema(description = "已拒绝")
        REJECTED("REJECTED");

        @EnumValue
        // 用于存储状态码的私有字段
        @Schema(description = "枚举值")
        private final String code;

        /**
         * 枚举构造函数
         * @param code 状态码
         */
        FosterStatusEnum(String code) {
            this.code = code;
        }
    }

    /* ===================== 领养状态 ===================== */
    @Getter
    @Schema(description = "领养状态枚举")
    public enum AdoptionStatusEnum {
    /**
     * 待审核状态
     * 表示领养申请已提交但尚未被审核
     */
        @Schema(description = "待审核")
        PENDING("PENDING"),
    /**
     * 已批准状态
     * 表示领养申请已通过审核
     */
        @Schema(description = "已批准")
        APPROVED("APPROVED"),
    /**
     * 已拒绝状态
     * 表示领养申请未通过审核
     */
        @Schema(description = "已拒绝")
        REJECTED("REJECTED");

        @EnumValue
        @Schema(description = "枚举值")
        private final String code;

    /**
     * 枚举构造函数
     * @param code 枚举状态对应的编码值
     */
        AdoptionStatusEnum(String code) {
            this.code = code;
        }
    }
}
