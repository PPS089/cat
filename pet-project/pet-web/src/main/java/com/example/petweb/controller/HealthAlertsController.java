package com.example.petweb.controller;

import com.example.petcommon.context.UserContext;
import com.example.petcommon.result.Result;
import com.example.petcommon.exception.BizException;
import com.example.petcommon.error.ErrorCode;
import com.example.petpojo.dto.HealthDto;
import com.example.petpojo.vo.HealthAlertsVo;
import com.example.petservice.service.HealthAlertsService;

import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
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
    public Result<List<HealthAlertsVo>> getHealthAlerts() {
        Long userId = UserContext.getCurrentUserId();
        log.info("获取用户健康提醒列表，用户ID: {}", userId);
        List<HealthAlertsVo> alerts = healthAlertsService.getUserHealthAlerts(userId.intValue());
        return Result.success(alerts);
    }

    /**
     * 创建健康提醒
     */
    @PostMapping
    @Operation(summary = "创建健康提醒", description = "创建一个新的健康提醒")
    public Result<HealthAlertsVo> createHealthAlert(
            @Valid @RequestBody HealthDto healthDto) {
        log.info("创建健康提醒: {}", healthDto);
        HealthAlertsVo created = healthAlertsService.createHealthAlert(healthDto);
        return Result.success(created);
    }

    /**
     * 更新健康提醒
     */
    @PutMapping("/{healthId}")
    @Operation(summary = "更新健康提醒", description = "更新指定的健康提醒信息")
    public Result<HealthAlertsVo> updateHealthAlert(
            @PathVariable Integer healthId,
            @Valid @RequestBody HealthDto healthDto) {
        log.info("更新健康提醒，提醒ID: {}, 数据: {}", healthId, healthDto);
        HealthAlertsVo updated = healthAlertsService.updateHealthAlert(healthId, healthDto);
        return Result.success(updated);
    }

    /**
     * 删除健康提醒
     */
    @DeleteMapping("/{healthId}")
    @Operation(summary = "删除健康提醒", description = "删除指定的健康提醒")
    public Result<String> deleteHealthAlert(
            @PathVariable Integer healthId) {
        log.info("删除健康提醒，提醒ID: {}", healthId);
        boolean deleted = healthAlertsService.deleteHealthAlert(healthId);
        if (!deleted) {
            throw new BizException(ErrorCode.BAD_REQUEST, "删除失败");
        }
        return Result.success("删除成功");
    }
}
