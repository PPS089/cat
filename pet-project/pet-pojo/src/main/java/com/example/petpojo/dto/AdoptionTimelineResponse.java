package com.example.petpojo.dto;

import com.example.petpojo.vo.AdoptionTimelineVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 领养时间线响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdoptionTimelineResponse {
    
    private List<AdoptionTimelineVo> timeline;
    
    private Integer total;
    
    private String petName;
    
    private String petBreed;
}