package com.example.petpojo.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "寄养请求DTO")
public class FosterRequest {
    @Schema(description = "收容所ID")
    private Long shelterId;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "开始日期")
    private LocalDate startDate;
}