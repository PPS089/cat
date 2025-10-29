package com.example.petpojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 领养时间线项值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdoptionTimelineVo {
    
    private Integer id;
    
    private Integer petId;
    
    private String petName;
    
    private String petBreed;
    
    private String action;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actionDate;
    
    private String description;
    
    private String shelterName;
    
    private String status;
}