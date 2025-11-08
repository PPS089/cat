package com.example.petpojo.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@TableName("pet_health")
public class HealthAlerts {
    
    @TableId(type = IdType.AUTO)
    private Integer healthId;
    
    @NotNull(message = "宠物ID不能为空")
    private Integer pid;
    
    @NotNull(message = "检查日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkDate;
    
    @NotBlank(message = "健康类型不能为空")
    private String healthType;
    
    @NotBlank(message = "描述不能为空")
    private String description;
    
    @NotNull(message = "提醒时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reminderTime;
    
    @NotBlank(message = "状态不能为空")
    private String status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}