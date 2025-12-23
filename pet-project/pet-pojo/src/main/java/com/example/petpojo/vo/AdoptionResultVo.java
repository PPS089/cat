package com.example.petpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 领养操作结果VO类
 * 用于封装领养操作的结果信息
 * @author 33185
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "领养操作结果VO")
public class AdoptionResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 宠物ID
     */
    @Schema(description = "宠物ID")
    private Long petId;
    
    /**
     * 宠物名称
     */
    @Schema(description = "宠物名称")
    private String petName;
    
    /**
     * 宠物物种
     */
    @Schema(description = "宠物物种")
    private String species;
    
    /**
     * 宠物品种
     */
    @Schema(description = "宠物品种")
    private String breed;
    
    /**
     * 宠物状态
     */
    @Schema(description = "宠物状态")
    private String status;
    
    /**
     * 领养日期
     */
    @Schema(description = "领养日期")
    private LocalDateTime adoptionDate;
    
    /**
     * 操作结果消息
     */
    @Schema(description = "操作结果消息")
    private String message;
}