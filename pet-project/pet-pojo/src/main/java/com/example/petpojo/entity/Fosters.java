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
import com.example.petpojo.entity.enums.CommonEnum;
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
@TableName("fosters")
@Schema(description = "寄养记录实体")
public class Fosters  implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "寄养记录ID")
    private Integer fid;

    @Schema(description = "宠物ID")
    private Integer pid;
    @Schema(description = "用户ID")
    private Integer uid;
    @Schema(description = "收容所ID")
    private Integer sid;
    @TableField(value = "reviewer_id")
    @Schema(description = "审核人ID")
    private Integer reviewerId;

    @TableField(value = "review_time")
    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;

    @TableField(value = "review_note")
    @Schema(description = "审核备注")
    private String reviewNote;

    @TableField(value = "start_date")
    @Schema(description = "开始日期")
    private LocalDateTime startDate;

    @TableField(value = "end_date")
    @Schema(description = "结束日期")
    private LocalDateTime endDate;

    @TableField(value = "status")
    @EnumValue
    @Schema(description = "寄养状态")
    private CommonEnum.FosterStatusEnum status;

    @TableField(value = "deleted")
    @Schema(description = "是否删除")
    private Boolean deleted;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
