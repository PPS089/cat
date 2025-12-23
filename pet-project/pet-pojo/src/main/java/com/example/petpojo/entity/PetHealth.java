package com.example.petpojo.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.petpojo.entity.enums.CommonEnum.HealthTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 33185
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("pet_health")
@Schema(description = "宠物健康记录实体")
public class PetHealth  implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "健康记录ID")
    private Integer healthId;

    @Schema(description = "宠物ID")
    private Integer pid;
    @TableField("check_date")
    @Schema(description = "检查日期")
    private LocalDateTime checkDate;
    @Schema(description = "健康类型")
    private HealthTypeEnum healthType;
    @Schema(description = "描述")
    private String description;
    @TableField(value="reminder_time")
    @Schema(description = "提醒时间")
    private LocalDateTime reminderTime;
    @Schema(description = "状态")
    private String status;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
