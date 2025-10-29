package com.example.petpojo.vo;

import lombok.Data;


import java.time.LocalDateTime;

@Data
public class HealthVo {
    /**
     * 宠物健康信息
     */
    private Integer healthId;
    private String healthType;
    private String description;
    private LocalDateTime checkDate;
    private LocalDateTime reminderTime;
    private String healthStatus;



     private Integer petId;
     private String petName;




}

