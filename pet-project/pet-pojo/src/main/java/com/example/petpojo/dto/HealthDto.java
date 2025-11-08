package com.example.petpojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HealthDto {

    @NotNull(message = "宠物ID不能为空")
    private Integer pid;
    
    @NotNull(message = "检查日期不能为空")
    private LocalDateTime checkDate;
    
    private String healthType;
    
    private String description;
    
    private LocalDateTime reminderTime;
    
    private String status;
}