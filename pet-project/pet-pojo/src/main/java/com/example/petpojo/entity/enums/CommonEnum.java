package com.example.petpojo.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 通用枚举类 - 所有业务枚举落库值均为英文，统一大小写规范
 */
public class CommonEnum {

    /* ===================== 宠物状态 ===================== */
    @Getter
    public enum PetStatusEnum {
        UNADOPTED("UNADOPTED"),
        ADOPTED("ADOPTED"),
        FOSTERING("FOSTERING"),
        DEAD("DEAD"),
        FOSTER_END("FOSTER_END");

        @EnumValue
        private final String code;

        PetStatusEnum(String code) {
            this.code = code;
        }
    }

    /* ===================== 健康类型 ===================== */
    @Getter
    public enum HealthTypeEnum {
        VACCINE("VACCINE"),
        CHECKUP("CHECKUP"),
        SURGERY("SURGERY"),
        DISEASE("DISEASE");

        @EnumValue
        private final String code;

        HealthTypeEnum(String code) {
            this.code = code;
        }
    }

    /* ===================== 登录状态 ===================== */
    @Getter
    public enum LoginStatusEnum {
        SUCCESS("SUCCESS"),
        FAILED("FAILED");

        @EnumValue
        private final String code;

        LoginStatusEnum(String code) {
            this.code = code;
        }
    }

    /* ===================== 文章状态 ===================== */
    @Getter
    public enum ArticleStatusEnum {
        PUBLISHED("PUBLISHED"),
        DRAFT("DRAFT");

        @EnumValue
        private final String code;

        ArticleStatusEnum(String code) {
            this.code = code;
        }
    }

    /* ===================== 寄养删除结果 ===================== */
    @Getter
    public enum FosterDeleteResultEnum {
        SUCCESS("SUCCESS"),                    // 删除成功
        PET_IS_FOSTERING("PET_IS_FOSTERING"),  // 宠物正在寄养中，无法删除
        DELETE_FAILED("DELETE_FAILED");        // 数据库删除失败

        @EnumValue
        private final String code;

        FosterDeleteResultEnum(String code) {
            this.code = code;
        }
    }
}