package com.example.petpojo.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

@Data
public class FosterRequest {
    private Long shelterId;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
}