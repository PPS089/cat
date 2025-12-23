package com.example.petservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.petpojo.entity.HealthAlerts;
import com.example.petcommon.properties.HealthReminderCleanupProperties;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface HealthAlertsMapper extends BaseMapper<HealthAlerts> {
    /**
     * 批量将已提醒的健康提醒标记为过期
     * @param now 当前时间
     * @param cleanupProperties 清理配置
     * @return 更新的记录数
     */
    int batchExpireReminded(@Param("now") LocalDateTime now, @Param("cleanupProperties") HealthReminderCleanupProperties cleanupProperties);
    
    /**
     * 批量将过期的健康提醒标记为归档
     * @param now 当前时间
     * @param cleanupProperties 清理配置
     * @return 更新的记录数
     */
    int batchArchiveExpired(@Param("now") LocalDateTime now, @Param("cleanupProperties") HealthReminderCleanupProperties cleanupProperties);
    
    /**
     * 批量删除已归档的健康提醒
     * @param now 当前时间
     * @param cleanupProperties 清理配置
     * @return 删除的记录数
     */
    int batchDeleteArchived(@Param("now") LocalDateTime now, @Param("cleanupProperties") HealthReminderCleanupProperties cleanupProperties);
}