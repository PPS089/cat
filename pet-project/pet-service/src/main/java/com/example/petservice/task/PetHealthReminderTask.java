package com.example.petservice.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.petpojo.vo.HealthAlertsVo;
import com.example.petservice.service.AdoptionsService;
import com.example.petservice.service.HealthAlertsService;
import com.example.petservice.websocket.WebSocketServer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 33185
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PetHealthReminderTask {

    private final HealthAlertsService healthAlertsService;
    private final AdoptionsService adoptionsService;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // 每30秒检查一次需要发送的定时提醒
    @Scheduled(cron = "0/30 * * * * ?")
    public void checkScheduledHealthReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusSeconds(30);
        LocalDateTime end = now.plusSeconds(30);

        try {
            // 直接查数据库：找出【该发送的提醒】，一次搞定！（最重要优化）
            List<HealthAlertsVo> needRemindList = healthAlertsService
                .listNeedRemindAlerts(start, end, "attention");

            if (needRemindList.isEmpty()) {
                return;
            }


            for (HealthAlertsVo alert : needRemindList) {
                Long userId = alert.getUserId();
                String petName = Optional.ofNullable(
                    adoptionsService.getPetNameById(alert.getPid().longValue())
                ).orElse("宠物");

                // 1. WebSocket 实时推送（只推给在线用户）
                String payload;
                try {
                    payload = buildReminderPayload(alert, petName);
                } catch (JsonProcessingException e) {
                    log.error("[健康提醒] 构建推送消息失败, healthId={}", alert.getHealthId(), e);
                    continue;
                }
                WebSocketServer.send(String.valueOf(userId), payload);

                // 2. 更新状态为已提醒
                healthAlertsService.markAsReminded(alert.getHealthId());
            }

        } catch (Exception e) {
            log.error("[健康提醒] 执行异常", e);
        }
    }

    private String buildReminderPayload(HealthAlertsVo alert, String petName) throws JsonProcessingException {
        HealthReminderMessage message = new HealthReminderMessage();
        message.setType("health_reminder");
        message.setHealthId(alert.getHealthId());
        message.setPetId(alert.getPid());
        message.setPetName(petName);
        message.setHealthType(alert.getHealthType());
        message.setDescription(alert.getDescription());
        message.setReminderTime(alert.getReminderTime());
        message.setStatus(alert.getStatus());
        message.setTimestamp(LocalDateTime.now());
        message.setContent(String.format("【健康提醒】%s：%s（%s）",
                petName,
                alert.getDescription(),
                alert.getHealthType()));
        return OBJECT_MAPPER.writeValueAsString(message);
    }


//    @Scheduled(cron = "0 30 2 * * ?")
//    public void cleanupExpiredHealthReminders() {
//        log.info("[清理任务] 开始执行健康提醒状态流转");
//
//        LocalDateTime now = LocalDateTime.now();
//
//        int expired = 0, archived = 0, deleted = 0;
//
//        // 1. 一次性批量处理「已提醒 → 过期」
//        expired += healthAlertsService.batchExpireReminded(now);
//
//        // 2. 一次性批量处理「过期 → 归档」
//        archived += healthAlertsService.batchArchiveExpired(now);
//
//        // 3. 一次性批量处理「归档 → 物理删除」（可选）
//        if (cleanupProperties.isEnableDeletion()) {
//            deleted += healthAlertsService.batchDeleteArchived(now);
//        }
//
//        log.info("[清理任务] 完成：过期 {} 条，归档 {} 条，删除 {} 条", expired, archived, deleted);
//    }
}

@lombok.Data
class HealthReminderMessage {
    private String type;
    private Integer healthId;
    private Integer petId;
    private String petName;
    private String healthType;
    private String description;
    private LocalDateTime reminderTime;
    private String status;
    private LocalDateTime timestamp;
    private String content;
}
