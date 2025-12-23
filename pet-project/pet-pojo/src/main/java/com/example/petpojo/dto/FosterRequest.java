package com.example.petpojo.dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * @author 33185
 */
@Data
@Schema(description = "寄养请求DTO")
public class FosterRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @NotNull(message = "收容所ID不能为空")
    @Positive(message = "收容所ID必须为正数")
    @Schema(description = "收容所ID")
    private Long shelterId;
    
    @NotNull(message = "寄养开始日期不能为空")
    @FutureOrPresent(message = "寄养开始日期不能早于今天")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "开始日期")
    private LocalDate startDate;
}
