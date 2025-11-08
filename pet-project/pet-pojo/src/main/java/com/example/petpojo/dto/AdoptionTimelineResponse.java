package com.example.petpojo.dto;

import com.example.petpojo.vo.AdoptionTimelineVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 领养时间线响应DTO
 * 用于封装领养时间线的响应数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdoptionTimelineResponse {
    
    /**
     * 时间线列表
     */
    private List<AdoptionTimelineVo> timeline;
    
    /**
     * 总记录数
     */
    private Integer total;
    
    /**
     * 宠物名称
     */
    private String petName;
    
    /**
     * 宠物品种
     */
    private String petBreed;
}