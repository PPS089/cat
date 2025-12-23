package com.example.petpojo.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * @author 33185
 */
@Data
@TableName("pet_health")
@Schema(description = "健康提醒实体")
public class HealthAlerts implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "健康记录ID")
    private Integer healthId;
    
    @NotNull(message = "宠物ID不能为空")
    @Positive(message = "宠物ID必须为正数")
    @Schema(description = "宠物ID")
    private Integer pid;
    
    @NotNull(message = "检查日期不能为空")
    @TableField("check_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @FutureOrPresent(message = "检查日期不能早于当前时间")
    @Schema(description = "检查日期")
    private LocalDateTime checkDate;
    
    @NotBlank(message = "健康类型不能为空")
    @Schema(description = "健康类型")
    private String healthType;
    
    @NotBlank(message = "描述不能为空")
    @Schema(description = "描述")
    private String description;
    
    @TableField("reminder_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "提醒时间")
    private LocalDateTime reminderTime;
    
    @NotBlank(message = "状态不能为空")
    @Schema(description = "状态")
    private String status;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
