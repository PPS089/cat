package com.example.petpojo.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author 33185
 */
@Data
@Schema(description = "健康信息DTO")
public class HealthDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "宠物ID不能为空")
    @Schema(description = "宠物ID")
    private Integer pid;
    
    @NotNull(message = "检查日期不能为空")
    @Schema(description = "检查日期")
    @FutureOrPresent(message = "检查日期不能早于当前时间")
    private LocalDateTime checkDate;
    
    @Schema(description = "健康类型")
    @NotBlank(message = "健康类型不能为空")
    @Size(min = 1, max = 10, message = "健康类型长度不能超过10字符")
    private String healthType;
    
    @Schema(description = "健康描述")
    @NotBlank(message = "健康描述不能为空")
    @Size(min = 1, max = 50, message = "健康描述应在1-50字符")
    private String description;

    @FutureOrPresent(message = "提醒开始日期不能早于当前时间")
    @Schema(description = "提醒时间")
    private LocalDateTime reminderTime;
    
    @Schema(description = "状态")
    private String status;
}