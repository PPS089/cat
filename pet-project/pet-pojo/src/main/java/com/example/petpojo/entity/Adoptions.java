package com.example.petpojo.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.petpojo.entity.enums.CommonEnum.AdoptionStatusEnum;
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
@TableName("adoptions")
@Schema(description = "领养记录实体")
public class Adoptions implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "领养记录ID")
    private Integer aid;

    @Schema(description = "宠物ID")
    private Integer pid;
    @Schema(description = "用户ID")
    private Integer uid;

    @TableField(value = "status")
    @EnumValue
    @Schema(description = "领养状态")
    private AdoptionStatusEnum status;

    @TableField(value = "reviewer_id")
    @Schema(description = "审核人ID")
    private Integer reviewerId;

    @TableField(value = "review_time")
    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;

    @TableField(value = "review_note")
    @Schema(description = "审核备注")
    private String reviewNote;

    @TableField(value = "adopt_date", fill = FieldFill.INSERT)
    @Schema(description = "领养日期")
    private LocalDateTime adoptDate;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
