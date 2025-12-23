package com.example.petpojo.vo;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 宠物领养时间线响应
 * 用于封装领养时间线列表及相关信息（总数、宠物信息等）
 * @author 33185
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "领养时间线响应")
public class AdoptionTimelineResponse {

    @Schema(description = "时间线列表")
    @Valid
    @NotNull(message = "时间线列表不能为空")
    private List<AdoptionTimelineVo> timeline;

    @Schema(description = "时间线总数")
    @NotNull(message = "时间线总数不能为空")
    @PositiveOrZero(message = "时间线总数不能为负数")
    private Integer total;

    @Schema(description = "宠物名称")
    @Size(max = 50, message = "宠物名称长度不能超过50字符")
    private String petName;

    @Schema(description = "宠物品种")
    @Size(max = 50, message = "宠物品种长度不能超过50字符")
    private String petBreed;
}
