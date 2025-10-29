package com.example.petservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petpojo.entity.HealthAlerts;
import com.example.petservice.mapper.HealthAlertsMapper;
import com.example.petservice.service.HealthAlertsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class HealthAlertsServiceImpl extends ServiceImpl<HealthAlertsMapper, HealthAlerts> implements HealthAlertsService {
    
    @Override
    public List<HealthAlerts> getUserHealthAlerts(Integer userId) {
        log.info("获取用户健康提醒列表，用户ID: {}", userId);
        // 根据用户ID关联宠物表来获取用户的宠物健康提醒
        return lambdaQuery()
                .orderByDesc(HealthAlerts::getCheckDate)
                .list();
    }
    
    @Override
    public List<HealthAlerts> getHealthAlertsByPetId(Integer pid) {
        log.info("获取宠物健康提醒列表，宠物ID: {}", pid);
        return lambdaQuery()
                .eq(HealthAlerts::getPid, pid)
                .orderByDesc(HealthAlerts::getCheckDate)
                .list();
    }
    
    @Override
    public boolean updateAlertStatus(Integer healthId, String status) {
        log.info("更新健康提醒状态，检查ID: {}, 状态: {}", healthId, status);
        HealthAlerts healthAlert = getById(healthId);
        if (healthAlert == null) {
            throw new IllegalArgumentException("健康提醒不存在");
        }
        healthAlert.setStatus(status);
        return updateById(healthAlert);
    }
    


    @Override
    public HealthAlerts createHealthAlert(HealthAlerts healthAlert) {
        log.info("创建健康提醒: {}", healthAlert);
        if (healthAlert.getPid() == null) {
            throw new IllegalArgumentException("宠物ID不能为空");
        }
        if (healthAlert.getCheckDate() == null) {
            healthAlert.setCheckDate(LocalDateTime.now());
        }
        if (healthAlert.getStatus() == null || healthAlert.getStatus().trim().isEmpty()) {
            healthAlert.setStatus("normal");
        }
        save(healthAlert);
        return healthAlert;
    }
    
    @Override
    public HealthAlerts updateHealthAlert(HealthAlerts healthAlert) {
        log.info("更新健康提醒: {}", healthAlert);
        if (healthAlert.getHealthId() == null) {
            throw new IllegalArgumentException("健康检查ID不能为空");
        }
        if (!updateById(healthAlert)) {
            throw new IllegalArgumentException("健康提醒更新失败");
        }
        return healthAlert;
    }
    
    @Override
    public boolean deleteHealthAlert(Integer healthId) {
        log.info("删除健康提醒，检查ID: {}", healthId);
        return removeById(healthId);
    }
}