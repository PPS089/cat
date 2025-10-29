package com.example.petservice.task;

import com.example.petpojo.entity.HealthAlerts;
import com.example.petservice.service.HealthAlertsService;
import com.example.petservice.websocket.WebSocketServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 健康提醒定时任务
 * 定时检查提醒时间并发送WebSocket通知
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HealthAlertTask {
    
    private final HealthAlertsService healthAlertsService;
    private final WebSocketServer webSocketServer;
    
    /**
     * 每分钟检查一次即将到期的健康提醒
     * 提前15分钟提醒
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkUpcomingHealthAlerts() {
        log.info("开始检查健康提醒任务");
        
        try {
            // 获取当前时间
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime fifteenMinutesLater = now.plusMinutes(15);
            
            // 查询即将到期的提醒（优先使用reminderTime，如果没有则使用time字段）
            List<HealthAlerts> upcomingAlerts = healthAlertsService.lambdaQuery()
                    .eq(HealthAlerts::getStatus, "normal")
                    .and(wrapper -> wrapper
                        .between(HealthAlerts::getReminderTime, now, fifteenMinutesLater)
                        .or()
                        .between(HealthAlerts::getCheckDate, now, fifteenMinutesLater)
                            .isNull(HealthAlerts::getReminderTime))
                    .list();
            
            log.info("找到 {} 个即将到期的健康提醒", upcomingAlerts.size());
            
            // 发送WebSocket通知
            for (HealthAlerts alert : upcomingAlerts) {
                sendHealthAlertNotification(alert);
            }
            
        } catch (Exception e) {
            log.error("检查健康提醒任务失败", e);
        }
    }
    
    /**
     * 每天凌晨2点检查过期的健康提醒
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkExpiredHealthAlerts() {
        log.info("开始检查过期健康提醒任务");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // 更新已过期的提醒状态
            List<HealthAlerts> expiredAlerts = healthAlertsService.lambdaQuery()
                    .eq(HealthAlerts::getStatus, "normal")
                    .lt(HealthAlerts::getCheckDate, now)
                    .list();
            
            log.info("找到 {} 个已过期的健康提醒", expiredAlerts.size());
            
            for (HealthAlerts alert : expiredAlerts) {
                alert.setStatus("critical");
                healthAlertsService.updateById(alert);
                
                // 发送过期通知
                sendExpiredAlertNotification(alert);
            }
            
        } catch (Exception e) {
            log.error("检查过期健康提醒任务失败", e);
        }
    }
    
    /**
     * 发送健康提醒通知
     */
    private void sendHealthAlertNotification(HealthAlerts alert) {
        try {
            // 确定提醒时间（优先使用reminderTime，如果没有则使用checkDate）
            LocalDateTime reminderTime = alert.getReminderTime() != null ? alert.getReminderTime() : alert.getCheckDate();
            
            String message = String.format(
                "健康提醒通知：宠物 %d 的 %s 提醒将在 %s 到期，请及时处理。提醒内容：%s",
                alert.getPid(),
                alert.getHealthType(),
                reminderTime.format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
                alert.getDescription() != null ? alert.getDescription() : "无"
            );
            
            // 发送给所有在线用户（可以根据需要改为发送给特定用户）
            webSocketServer.sendToAllClient(message);
            
            log.info("发送健康提醒通知：宠物 {} 的 {} 提醒", alert.getPid(), alert.getHealthType());
            
        } catch (Exception e) {
            log.error("发送健康提醒通知失败", e);
        }
    }
    
    /**
     * 发送过期提醒通知
     */
    private void sendExpiredAlertNotification(HealthAlerts alert) {
        try {
            String message = String.format(
                "健康提醒已过期：宠物 %d 的 %s 提醒已于 %s 过期，请尽快处理。",
                alert.getPid(),
                alert.getHealthType(),
                alert.getCheckDate().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
            );
            
            // 发送给所有在线用户
            webSocketServer.sendToAllClient(message);
            
            log.info("发送过期提醒通知：宠物 {} 的 {} 提醒", alert.getPid(), alert.getHealthType());
            
        } catch (Exception e) {
            log.error("发送过期提醒通知失败", e);
        }
    }
}