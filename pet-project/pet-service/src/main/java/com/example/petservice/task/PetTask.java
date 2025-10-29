package com.example.petservice.task;


import com.example.petpojo.entity.Adoptions;
import com.example.petpojo.entity.PetHealth;
import com.example.petpojo.entity.Users;
import com.example.petservice.service.AdoptionsService;
import com.example.petservice.service.HealthService;
import com.example.petservice.service.UsersService;
import com.example.petservice.websocket.WebSocketServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Component
@Slf4j
@Transactional
@RequiredArgsConstructor
public class PetTask {

    private final UsersService usersService;
    private final AdoptionsService adoptionsService;
    private final HealthService healthService;
    private final WebSocketServer webSocketServer;


    /**
     * 检查是否需要发送提醒消息,提取pids
     */
   private List<Integer> getPids() {
        return healthService.lambdaQuery()
                .select(PetHealth::getPid)
                .gt(PetHealth::getReminderTime, LocalDateTime.now())
                .list()
                .stream()
                .map(PetHealth::getPid)
                .toList();
    }


    @Scheduled(cron = "0 * * * * ?")
    public void sendMessageToClient() {
        log.info("开始执行宠物健康提醒任务");
        
        try {
            // 获取需要提醒的宠物ID列表
            List<Integer> pids = getPids();
            log.info("找到 {} 个需要提醒的宠物", pids.size());
            
            // 如果没有需要提醒的宠物，直接返回
            if (pids.isEmpty()) {
                log.debug("没有宠物需要提醒");
                return;
            }
            
            // 提取关联的用户ID
            List<Integer> userIdList = adoptionsService.lambdaQuery()
                    .select(Adoptions::getUid)
                    .in(Adoptions::getPid, pids)
                    .list()
                    .stream()
                    .map(Adoptions::getUid)
                    .distinct()
                    .toList();
            
            log.info("找到 {} 个需要通知的用户", userIdList.size());
            
            // 发送提醒消息给每个用户
            userIdList.forEach(userId -> {
                try {
                    Users user = usersService.getById(userId);
                    if (user != null) {
                        // 获取该用户关联的所有需要提醒的宠物
                        List<Integer> userPets = adoptionsService.lambdaQuery()
                                .select(Adoptions::getPid)
                                .eq(Adoptions::getUid, userId)
                                .in(Adoptions::getPid, pids)
                                .list()
                                .stream()
                                .map(Adoptions::getPid)
                                .toList();
                        
                        if (!userPets.isEmpty()) {
                            String message = "您有 " + userPets.size() + " 个宠物需要检查健康状态";
                            log.info("发送提醒消息给用户 {}: {}", user.getUserName(), message);
                            webSocketServer.sendToUsersClient(userId.toString(), message);
                        }
                    }
                } catch (Exception e) {
                    log.error("发送提醒消息给用户 {} 失败", userId, e);
                }
            });
            
            // 更新已过期的宠物健康状态
            try {
                boolean updated = healthService.lambdaUpdate()
                        .lt(PetHealth::getCheckDate, LocalDateTime.now())
                        .set(PetHealth::getStatus, "critical")
                        .update();
                if (updated) {
                    log.info("更新过期宠物健康状态成功");
                }
            } catch (Exception e) {
                log.error("更新宠物健康状态失败", e);
            }
        } catch (Exception e) {
            log.error("执行宠物健康提醒任务失败", e);
        }
    }
}
