package com.example.petservice.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.petcommon.properties.HealthReminderCleanupProperties;
import com.example.petpojo.dto.HealthDto;
import com.example.petpojo.entity.Users;
import com.example.petpojo.vo.AdoptionsVo;
import com.example.petpojo.vo.HealthAlertsVo;
import com.example.petservice.service.AdoptionsService;
import com.example.petservice.service.HealthAlertsService;
import com.example.petservice.service.UsersService;
import com.example.petservice.service.VoiceAlertService;
import com.example.petservice.websocket.WebSocketServer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PetHealthReminderTask {

    private final HealthAlertsService healthAlertsService;
    private final AdoptionsService adoptionsService;
    private final UsersService usersService;
    private final VoiceAlertService voiceAlertService;
    private final HealthReminderCleanupProperties cleanupConfig;
    private final WebSocketServer webSocketServer;

    // 每30秒检查一次需要发送的定时提醒
    @Scheduled(cron = "0/30 * * * * ?")
    public void checkScheduledHealthReminders() {
        try {
            log.info("开始检查定时健康提醒...");
            
            // 获取当前时间，用于匹配提醒时间
            LocalDateTime now = LocalDateTime.now();
            
            // 获取所有用户
            List<Users> users = usersService.list();
            
            int remindersSent = 0;
            
            for (Users user : users) {
                log.debug("处理用户ID: {}", user.getId());
                // 获取用户的宠物领养记录
                List<AdoptionsVo> userAdoptions = adoptionsService.getUserAdoptions(
                    user.getId().longValue(), 1, 1000
                );
                
                if (userAdoptions.isEmpty()) {
                    continue;
                }
                
                // 获取用户的健康提醒
                List<HealthAlertsVo> healthAlerts = healthAlertsService.getUserHealthAlerts(user.getId());
                
                for (HealthAlertsVo alert : healthAlerts) {
                    // 检查提醒是否符合条件 - 状态为需要关注
                    if (alert.getReminderTime() != null && "attention".equals(alert.getStatus())) {
                        
                        // 检查提醒时间是否在当前时间范围内（前后30秒内）
                        LocalDateTime alertTime = alert.getReminderTime();
                        LocalDateTime nowMinus30Sec = now.minusSeconds(30);
                        LocalDateTime nowPlus30Sec = now.plusSeconds(30);
                        
                        // 如果提醒时间在当前时间的前后30秒内，就发送提醒
                        if (alertTime.isAfter(nowMinus30Sec) && alertTime.isBefore(nowPlus30Sec)) {
                            // 获取宠物名称
                            String petName = "宠物";
                            for (AdoptionsVo adoption : userAdoptions) {
                                if (adoption.getPid().equals(alert.getPid())) {
                                    petName = adoption.getName();
                                    break;
                                }
                            }
                            
                            // 发送WebSocket提醒
                            String message = String.format(
                                "宠物健康提醒：%s - %s (提醒时间: %s)",
                                alert.getHealthType(),
                                alert.getDescription(),
                                alert.getReminderTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                            );
                            
                            webSocketServer.sendToUsersClient(user.getId().toString(), message);
                            log.info("发送健康提醒给用户 {} - 宠物ID: {}, 类型: {}", 
                                user.getId(), alert.getPid(), alert.getHealthType());
                            
                            // 发送语音提醒
                            voiceAlertService.speakPetHealthReminder(petName, alert.getDescription());
                            
                            // 更新提醒状态为已提醒
                            HealthDto healthDto = new HealthDto();
                            healthDto.setPid(alert.getPid());
                            healthDto.setCheckDate(alert.getCheckDate());
                            healthDto.setHealthType(alert.getHealthType());
                            healthDto.setDescription(alert.getDescription());
                            healthDto.setReminderTime(alert.getReminderTime());
                            healthDto.setStatus("reminded");
                            healthAlertsService.updateHealthAlert(alert.getHealthId(), healthDto);
                            
                            remindersSent++;
                        }
                    }
                }
            }
            
            log.info("定时健康提醒检查完成 - 发送了 {} 个提醒", remindersSent);
            
        } catch (Exception e) {
            log.error("检查定时健康提醒时发生错误", e);
        }
    }

    // 每天清理过期的健康提醒
    @Scheduled(cron = "0 04 11 * * ?")
    public void cleanupExpiredHealthReminders() {
        try {
            log.info("开始清理过期的健康提醒...");
            
            LocalDateTime now = LocalDateTime.now();
            
            // 获取所有用户
            List<Users> users = usersService.list();
            int expiredCount = 0;
            int archivedCount = 0;
            int deletedCount = 0;
            
            for (Users user : users) {
                List<HealthAlertsVo> healthAlerts = healthAlertsService.getAllUserHealthAlerts(user.getId());
                log.debug("用户 {} 共有 {} 条健康提醒", user.getId(), healthAlerts.size());
                
                for (HealthAlertsVo alert : healthAlerts) {
                    LocalDateTime effectiveTime = alert.getReminderTime() != null ? alert.getReminderTime() : alert.getCheckDate();
                    log.debug("处理提醒ID: {}, 状态: {}, reminderTime: {}, checkDate: {}, effectiveTime: {}",
                        alert.getHealthId(), alert.getStatus(), alert.getReminderTime(), alert.getCheckDate(), effectiveTime);
                    
                    // 计算过期天数阈值（根据健康类型）
                    int expiryDays = cleanupConfig.getExpiryDaysByHealthType(alert.getHealthType());
                    LocalDateTime expiryThreshold = now.minusDays(expiryDays);
                    
                    // 计算归档天数阈值
                    int archiveDays = expiryDays + cleanupConfig.getArchiveThresholdDays();
                    LocalDateTime archiveThreshold = now.minusDays(archiveDays);
                    log.debug("提醒ID: {}, expiryDays: {}, expiryThreshold: {}, archiveDays: {}, archiveThreshold: {}",
                        alert.getHealthId(), expiryDays, expiryThreshold, archiveDays, archiveThreshold);
                    
                    // 需要注意状态的提醒
                    if ("attention".equals(alert.getStatus()) &&
                        effectiveTime.isBefore(expiryThreshold)) {
                        log.debug("提醒ID: {} 满足 attention -> expired 条件", alert.getHealthId());
                        
                        // 更新状态为过期
                        HealthDto healthDto = createHealthDto(alert);
                        healthDto.setStatus("expired");
                        healthAlertsService.updateHealthAlert(alert.getHealthId(), healthDto);
                        expiredCount++;
                        log.debug("健康提醒已过期，ID: {}, 类型: {}, 过期天数: {}", 
                            alert.getHealthId(), alert.getHealthType(), expiryDays);
                    }
                    // 处理已过期但需要归档的提醒
                    else if ("expired".equals(alert.getStatus()) && 
                             effectiveTime.isBefore(archiveThreshold)) {
                        log.debug("提醒ID: {} 满足 expired -> archived 条件", alert.getHealthId());
                        
                        // 更新状态为已归档
                        HealthDto healthDto = createHealthDto(alert);
                        healthDto.setStatus("archived");
                        healthAlertsService.updateHealthAlert(alert.getHealthId(), healthDto);
                        archivedCount++;
                        log.debug("健康提醒已归档，ID: {}, 类型: {}, 归档天数: {}", 
                            alert.getHealthId(), alert.getHealthType(), archiveDays);
                    }
                    // 处理已提醒但过期的记录
                    else if ("reminded".equals(alert.getStatus()) && 
                             effectiveTime.isBefore(expiryThreshold)) {
                        log.debug("提醒ID: {} 满足 reminded -> expired 条件", alert.getHealthId());
                        
                        // 更新状态为过期
                        HealthDto healthDto = createHealthDto(alert);
                        healthDto.setStatus("expired");
                        healthAlertsService.updateHealthAlert(alert.getHealthId(), healthDto);
                        expiredCount++;
                        log.debug("已提醒的健康提醒已过期，ID: {}, 类型: {}, 过期天数: {}", 
                            alert.getHealthId(), alert.getHealthType(), expiryDays);
                    }
                    // 处理需要删除的归档提醒
                    else if ("archived".equals(alert.getStatus()) && cleanupConfig.isEnableDeletion()) {
                        // 检查是否达到删除阈值
                        int deleteDays = expiryDays + cleanupConfig.getArchiveThresholdDays() + cleanupConfig.getDeletionThresholdDays();
                        LocalDateTime deleteThreshold = now.minusDays(deleteDays);
                        log.debug("提醒ID: {} 满足 archived 且 enableDeletion 条件，deleteDays: {}, deleteThreshold: {}",
                            alert.getHealthId(), deleteDays, deleteThreshold);
                        if (effectiveTime.isBefore(deleteThreshold) || effectiveTime.isEqual(deleteThreshold)) {
                            log.debug("提醒ID: {} 满足 archived -> deleted 条件", alert.getHealthId());
                            healthAlertsService.removeById(alert.getHealthId());
                            deletedCount++;
                            log.debug("已删除超长期归档提醒 {}，健康类型: {}", alert.getHealthId(), alert.getHealthType());
                        }
                    }
                }
            }
            
            log.info("过期健康提醒清理完成 - 更新了 {} 个过期提醒, {} 个归档提醒, {} 个删除提醒", expiredCount, archivedCount, deletedCount);
        } catch (Exception e) {
            log.error("清理过期健康提醒时发生错误", e);
        }
    }
    
    /**
     * 从HealthAlertsVo创建HealthDto对象
     * @param alert 健康提醒VO对象
     * @return 健康提醒DTO对象
     */
    private HealthDto createHealthDto(HealthAlertsVo alert) {
        HealthDto healthDto = new HealthDto();
        healthDto.setPid(alert.getPid());
        healthDto.setCheckDate(alert.getCheckDate());
        healthDto.setHealthType(alert.getHealthType());
        healthDto.setDescription(alert.getDescription());
        healthDto.setReminderTime(alert.getReminderTime());
        return healthDto;
    }

}