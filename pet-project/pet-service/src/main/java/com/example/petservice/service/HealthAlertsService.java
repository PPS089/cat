package com.example.petservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.petpojo.dto.HealthDto;
import com.example.petpojo.entity.HealthAlerts;
import com.example.petpojo.vo.HealthAlertsVo;

import java.util.List;

public interface HealthAlertsService extends IService<HealthAlerts> {
    
    /**
     * 获取用户的健康提醒列表（包含所有状态，用于清理任务）
     */
    List<HealthAlertsVo> getAllUserHealthAlerts(Integer userId);
    
    /**
     * 获取用户的健康提醒列表
     */
    List<HealthAlertsVo> getUserHealthAlerts(Integer userId);
    
    /**
     * 根据宠物ID获取健康提醒
     */
    List<HealthAlertsVo> getHealthAlertsByPetId(Integer pid);
    
    /**
     * 创建健康提醒
     */
    HealthAlertsVo createHealthAlert(HealthDto healthDto);
    
    /**
     * 更新健康提醒
     */
    HealthAlertsVo updateHealthAlert(Integer healthId, HealthDto healthDto);
    
    /**
     * 删除健康提醒
     */
    boolean deleteHealthAlert(Integer healthId);
}