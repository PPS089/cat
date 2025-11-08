package com.example.petweb.controller;

import com.example.petcommon.context.UserContext;
import com.example.petcommon.result.Result;
import com.example.petpojo.entity.HealthAlerts;
import com.example.petpojo.dto.HealthDto;
import com.example.petpojo.vo.HealthAlertsVo;
import com.example.petservice.service.HealthAlertsService;

import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

/**
 * 健康提醒控制器
 * 提供健康提醒相关的 REST API 接口
 */
@RestController
@RequestMapping("/user/health-alerts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "健康提醒", description = "健康提醒相关的 API 接口")
@SecurityRequirement(name = "bearer-key")
public class HealthAlertsController {

    private final HealthAlertsService healthAlertsService;

    /**
     * 获取用户的健康提醒列表
     */
    @GetMapping
    @Operation(summary = "获取用户健康提醒列表", description = "获取当前用户的所有健康提醒列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    public Result<Map<String, List<HealthAlertsVo>>> getHealthAlerts() {
        Long userId = UserContext.getCurrentUserId();
        log.info("获取用户健康提醒列表，用户ID: {}", userId);
        
        List<HealthAlertsVo> alerts = healthAlertsService.getUserHealthAlerts(userId.intValue());
        
        return Result.success(Map.of("alerts", alerts));
    }

    /**
     * 更新健康提醒状态
     */
    @PutMapping("/{healthId}/status")
    @Operation(summary = "更新健康提醒状态", description = "更新指定健康提醒的状态")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "更新失败"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    public Result<String> updateAlertStatus(
            @Parameter(description = "健康提醒ID", required = true) @PathVariable Integer healthId,
            @Parameter(description = "状态值", required = true) @RequestParam String status) {
        log.info("更新健康提醒状态，提醒ID: {}, 状态: {}", healthId, status);
        
        boolean updated = healthAlertsService.updateAlertStatus(healthId, status);
        if (updated) {
            return Result.success("状态更新成功");
        } else {
            return Result.error("状态更新失败");
        }
    }

    /**
     * 创建健康提醒
     */
    @PostMapping
    @Operation(summary = "创建健康提醒", description = "创建一个新的健康提醒")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功"),
        @ApiResponse(responseCode = "400", description = "创建失败"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    public Result<HealthAlertsVo> createHealthAlert(
            @Parameter(description = "健康提醒信息", required = true) @Valid @RequestBody HealthDto healthDto) {
        log.info("创建健康提醒: {}", healthDto);
        
        try {
            HealthAlertsVo created = healthAlertsService.createHealthAlert(healthDto);
            return Result.success(created);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新健康提醒
     */
    @PutMapping("/{healthId}")
    @Operation(summary = "更新健康提醒", description = "更新指定的健康提醒信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "更新失败"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    public Result<HealthAlertsVo> updateHealthAlert(
            @Parameter(description = "健康提醒ID", required = true) @PathVariable Integer healthId,
            @Parameter(description = "健康提醒信息", required = true) @Valid @RequestBody HealthDto healthDto) {
        log.info("更新健康提醒，提醒ID: {}, 数据: {}", healthId, healthDto);
        
        try {
            HealthAlertsVo updated = healthAlertsService.updateHealthAlert(healthId, healthDto);
            return Result.success(updated);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除健康提醒
     */
    @DeleteMapping("/{healthId}")
    @Operation(summary = "删除健康提醒", description = "删除指定的健康提醒")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "400", description = "删除失败"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    public Result<String> deleteHealthAlert(
            @Parameter(description = "健康提醒ID", required = true) @PathVariable Integer healthId) {
        log.info("删除健康提醒，提醒ID: {}", healthId);
        
        boolean deleted = healthAlertsService.deleteHealthAlert(healthId);
        if (deleted) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }
}