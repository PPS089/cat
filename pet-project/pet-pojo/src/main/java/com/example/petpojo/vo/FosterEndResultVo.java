package com.example.petpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 结束寄养操作结果VO类
 * 用于封装结束寄养操作的结果信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "结束寄养操作结果VO")
public class FosterEndResultVo {
    /**
     * 宠物ID
     */
    @Schema(description = "宠物ID")
    private Long petId;
    
    /**
     * 结束寄养日期
     */
    @Schema(description = "结束寄养日期")
    private LocalDateTime endDate;
}