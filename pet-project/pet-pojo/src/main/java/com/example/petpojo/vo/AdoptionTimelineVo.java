package com.example.petpojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 领养时间线项值对象
 * 用于封装领养过程中的各个时间点事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdoptionTimelineVo {
    
    /**
     * 时间线项ID
     */
    private Integer id;
    
    /**
     * 宠物ID
     */
    private Integer petId;
    
    /**
     * 宠物名称
     */
    private String petName;
    
    /**
     * 宠物品种
     */
    private String petBreed;
    
    /**
     * 操作类型
     */
    private String action;
    
    /**
     * 操作日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actionDate;
    
    /**
     * 操作描述
     */
    private String description;
    
    /**
     * 收容所名称
     */
    private String shelterName;
    
    /**
     * 状态
     */
    private String status;
}