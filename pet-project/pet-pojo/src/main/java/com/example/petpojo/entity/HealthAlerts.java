package com.example.petpojo.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
@TableName("pet_health")
public class HealthAlerts {
    
    @TableId(type = IdType.AUTO)
    private Integer healthId;
    
    private Integer pid;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime checkDate;
    
    private String healthType;
    
    private String description;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime reminderTime;
    
    private String status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}