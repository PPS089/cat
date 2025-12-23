package com.example.petpojo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 记录宠物事件
 * @author 33185
 */
@Data
@Schema(description = "宠物记录DTO")
public class RecordDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @NotNull(message = "宠物ID不能为空")
    @Positive(message = "宠物ID必须为正数")
    @Schema(description = "宠物ID")
    private Integer pid;

    @Size(min = 1, max = 10, message = "事件类型需在2-10字符内")
    @Schema(description = "事件类型")
    private String eventType;

    @Size(min = 1, max = 10, message = "情绪类型需在2-10字符内")
    @Schema(description = "情绪状态")
    private String mood;

    @Size(min = 1, max = 200, message = "描述需在2-200字符内")
    @Schema(description = "描述")
    private String description;

    @Size(min = 1, max = 100, message = "位置需在2-100字符内")
    @Schema(description = "位置")
    private String location;
    
    @Schema(description = "记录时间")
    private LocalDateTime recordTime;
}
