package com.example.petpojo.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("pet_records")
@Schema(description = "宠物事件记录实体")
public class PetRecords  implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "记录ID")
    private Integer recordId;

    @Schema(description = "宠物ID")
    private Integer pid;
    @Schema(description = "用户ID")
    private Integer uid;
    @Schema(description = "事件类型")
    private String eventType;
    @Schema(description = "情绪")
    private String mood;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "位置")
    private String location;
    @TableField("record_time")
    @Schema(description = "记录时间")
    private LocalDateTime recordTime;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
