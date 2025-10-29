package com.example.petservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.petpojo.entity.HealthAlerts;

import java.util.List;

public interface HealthAlertsService extends IService<HealthAlerts> {
    
    /**
     * 获取用户的健康提醒列表
     */
    List<HealthAlerts> getUserHealthAlerts(Integer userId);
    
    /**
     * 根据宠物ID获取健康提醒
     */
    List<HealthAlerts> getHealthAlertsByPetId(Integer pid);
    
    /**
     * 更新健康提醒状态
     */
    boolean updateAlertStatus(Integer healthId, String status);
    
    /**
     * 创建健康提醒
     */
    HealthAlerts createHealthAlert(HealthAlerts healthAlert);
    
    /**
     * 更新健康提醒
     */
    HealthAlerts updateHealthAlert(HealthAlerts healthAlert);
    
    /**
     * 删除健康提醒
     */
    boolean deleteHealthAlert(Integer healthId);
}