package com.example.petpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 健康提醒信息VO类
 * 用于封装健康提醒的详细信息
 * @author 33185
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "健康提醒信息VO")
public class HealthAlertsVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 健康检查ID
     */
    @Schema(description = "健康检查ID")
    private Integer healthId;
    
    /**
     * 宠物ID
     */
    @Schema(description = "宠物ID")
    private Integer pid;
    
    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;
    
    /**
     * 检查日期
     */
    @Schema(description = "检查日期")
    private LocalDateTime checkDate;
    
    /**
     * 健康类型
     */
    @Schema(description = "健康类型")
    private String healthType;
    
    /**
     * 健康描述
     */
    @Schema(description = "健康描述")
    private String description;
    
    /**
     * 提醒时间
     */
    @Schema(description = "提醒时间")
    private LocalDateTime reminderTime;
    
    /**
     * 状态
     */
    @Schema(description = "状态")
    private String status;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
