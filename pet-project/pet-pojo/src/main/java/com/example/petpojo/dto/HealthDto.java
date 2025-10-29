package com.example.petpojo.dto;

import com.example.petpojo.entity.enums.CommonEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HealthDto {

    @NotBlank(message = "宠物ID不能为空")
    private Integer pid;
    @NotBlank(message = "检查日期不能为空")
    private LocalDateTime checkDate;
    @NotBlank(message = "健康类型不能为空")
    private CommonEnum.HealthTypeEnum healthType;
    @NotBlank(message = "描述不能为空")
    private String description;
    @NotBlank(message = "提醒时间不能为空")
    private LocalDateTime reminderTime;
    @NotBlank(message = "状态不能为空")
    private String status;
}
