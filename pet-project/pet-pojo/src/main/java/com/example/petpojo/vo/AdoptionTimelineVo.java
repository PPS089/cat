package com.example.petpojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 领养时间线项值对象
 * 用于封装领养过程中的各个时间点事件
 * @author 33185
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "领养时间线项VO")
public class AdoptionTimelineVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @Schema(description = "时间线项ID")
    private Integer id;

    @Schema(description = "宠物ID")
    @NotNull(message = "宠物ID不能为空")
    @Positive(message = "宠物ID必须为正数")
    private Integer petId;

    @Schema(description = "操作类型")
    @NotBlank(message = "操作类型不能为空")
    @Size(max = 64, message = "操作类型长度不能超过64字符")
    private String action;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "操作时间")
    @NotNull(message = "操作时间不能为空")
    private LocalDateTime actionTime;

    @Schema(description = "操作用户ID")
    @NotNull(message = "操作用户ID不能为空")
    @Positive(message = "操作用户ID必须为正数")
    private Integer operatorUserId;

    @Schema(description = "操作描述")
    @NotBlank(message = "操作描述不能为空")
    @Size(max = 255, message = "操作描述长度不能超过255字符")
    private String description;

    @Schema(description = "状态标签")
    @NotBlank(message = "状态标签不能为空")
    @Size(max = 64, message = "状态标签长度不能超过64字符")
    private String statusLabel;

    @Schema(description = "收容所信息")
    @Valid
    private ShelterInfo shelter;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "时间线收容所信息")
    public static class ShelterInfo {
        @Schema(description = "收容所ID")
        @NotNull(message = "收容所ID不能为空")
        @Positive(message = "收容所ID必须为正数")
        private Integer sid;
        @Schema(description = "收容所名称")
        @NotBlank(message = "收容所名称不能为空")
        @Size(max = 100, message = "收容所名称长度不能超过100字符")
        private String name;
        @Schema(description = "收容所位置")
        @Size(max = 255, message = "收容所位置长度不能超过255字符")
        private String location;
    }
}
