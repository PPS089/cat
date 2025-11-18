package com.example.petcommon.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 健康提醒清理配置类
 * 用于从配置文件中读取健康提醒清理相关的配置参数
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "health.reminder.cleanup")
public class HealthReminderCleanupProperties {
    
    /**
     * 不同健康类型的过期天数配置
     */
    private Map<String, Integer> expiryDays;
    
    /**
     * 归档阈值（过期后多少天归档）
     */
    private int archiveThresholdDays;
    
    /**
     * 是否启用自动删除超长期归档记录
     */
    private boolean enableDeletion;
    
    /**
     * 删除阈值（归档后多少天删除）
     */
    private int deletionThresholdDays;
    
    /**
     * 根据健康类型获取过期天数
     * @param healthType 健康类型
     * @return 过期天数
     */
    public int getExpiryDaysByHealthType(String healthType) {
        if (expiryDays == null) {
            return 1; // 默认值
        }
        
        // 尝试获取特定健康类型的过期天数
        Integer days = expiryDays.get(healthType);
        if (days != null) {
            return days;
        }
        
        // 尝试获取默认值
        days = expiryDays.get("default");
        return days != null ? days : 1; // 如果没有默认值，返回1
    }
}