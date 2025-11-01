package com.example.petweb.controller;

import com.example.petcommon.context.UserContext;
import com.example.petcommon.result.Result;
import com.example.petpojo.entity.HealthAlerts;
import com.example.petservice.service.HealthAlertsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/health-alerts")
@RequiredArgsConstructor
@Slf4j
public class HealthAlertsController {

    private final HealthAlertsService healthAlertsService;

    /**
     * 获取用户的健康提醒列表
     */
    @GetMapping
    public Result<Map<String, List<HealthAlerts>>> getHealthAlerts() {
        Long userId = UserContext.getCurrentUserId();
        log.info("获取用户健康提醒列表，用户ID: {}", userId);
        
        List<HealthAlerts> alerts = healthAlertsService.getUserHealthAlerts(userId.intValue());
        
        return Result.success(Map.of("alerts", alerts));
    }

    /**
     * 更新健康提醒状态
     */
    @PutMapping("/{healthId}/status")
    public Result<String> updateAlertStatus(
            @PathVariable Integer healthId,
            @RequestParam String status) {
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
    public Result<HealthAlerts> createHealthAlert(@RequestBody HealthAlerts healthAlert) {
        log.info("创建健康提醒: {}", healthAlert);
        
        try {
            HealthAlerts created = healthAlertsService.createHealthAlert(healthAlert);
            return Result.success(created);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新健康提醒
     */
    @PutMapping("/{healthId}")
    public Result<HealthAlerts> updateHealthAlert(
            @PathVariable Integer healthId,
            @RequestBody HealthAlerts healthAlert) {
        log.info("更新健康提醒，提醒ID: {}, 数据: {}", healthId, healthAlert);
        
        healthAlert.setHealthId(healthId);
        try {
            HealthAlerts updated = healthAlertsService.updateHealthAlert(healthAlert);
            return Result.success(updated);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除健康提醒
     */
    @DeleteMapping("/{healthId}")
    public Result<String> deleteHealthAlert(
            @PathVariable Integer healthId) {
        log.info("删除健康提醒，提醒ID: {}", healthId);
        
        boolean deleted = healthAlertsService.deleteHealthAlert(healthId);
        if (deleted) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }
}