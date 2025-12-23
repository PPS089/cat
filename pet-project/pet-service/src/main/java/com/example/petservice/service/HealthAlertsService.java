package com.example.petservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.petpojo.dto.HealthDto;
import com.example.petpojo.entity.HealthAlerts;
import com.example.petpojo.vo.HealthAlertsVo;

import java.time.LocalDateTime;
import java.util.List;

public interface HealthAlertsService extends IService<HealthAlerts> {
    
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
    
    /**
     * 获取需要提醒的健康提醒列表
     * @param start 开始时间
     * @param end 结束时间
     * @param status 状态
     * @return 需要提醒的健康提醒列表
     */
    List<HealthAlertsVo> listNeedRemindAlerts(LocalDateTime start, LocalDateTime end, String status);
    
    /**
     * 通过宠物ID获取宠物名称
     * @param pid 宠物ID
     * @return 宠物名称
     */
    String getPetNameById(Long pid);
    
    /**
     * 标记为已提醒
     * @param healthId 健康提醒ID
     */
    void markAsReminded(Integer healthId);
    
    /**
     * 批量将已提醒的健康提醒标记为过期
     * @param now 当前时间
     * @return 更新的记录数
     */
    int batchExpireReminded(LocalDateTime now);
    
    /**
     * 批量将过期的健康提醒标记为归档
     * @param now 当前时间
     * @return 更新的记录数
     */
    int batchArchiveExpired(LocalDateTime now);
    
    /**
     * 批量删除已归档的健康提醒
     * @param now 当前时间
     * @return 删除的记录数
     */
    int batchDeleteArchived(LocalDateTime now);
}