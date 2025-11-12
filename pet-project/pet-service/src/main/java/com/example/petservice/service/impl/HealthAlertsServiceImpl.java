package com.example.petservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petpojo.dto.HealthDto;
import com.example.petpojo.entity.HealthAlerts;
import com.example.petpojo.vo.HealthAlertsVo;
import com.example.petservice.mapper.HealthAlertsMapper;
import com.example.petservice.service.HealthAlertsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 健康提醒服务实现类
 * 实现健康提醒相关的业务逻辑
 */
@Slf4j
@Service
public class HealthAlertsServiceImpl extends ServiceImpl<HealthAlertsMapper, HealthAlerts> implements HealthAlertsService {
    
    /**
     * 将HealthAlerts实体转换为HealthAlertsVo
     */
    private HealthAlertsVo convertToVo(HealthAlerts healthAlerts) {
        HealthAlertsVo vo = new HealthAlertsVo();
        vo.setHealthId(healthAlerts.getHealthId());
        vo.setPid(healthAlerts.getPid());
        vo.setCheckDate(healthAlerts.getCheckDate());
        vo.setHealthType(healthAlerts.getHealthType());
        vo.setDescription(healthAlerts.getDescription());
        vo.setReminderTime(healthAlerts.getReminderTime());
        vo.setStatus(healthAlerts.getStatus());
        vo.setCreatedAt(healthAlerts.getCreatedAt());
        vo.setUpdatedAt(healthAlerts.getUpdatedAt());
        return vo;
    }
    
    /**
     * 获取用户的健康提醒列表
     * @param userId 用户ID
     * @return 健康提醒VO列表
     */
    @Override
    public List<HealthAlertsVo> getUserHealthAlerts(Integer userId) {
        log.info("获取用户健康提醒列表，用户ID: {}", userId);
        // 根据用户ID关联宠物表来获取用户的宠物健康提醒
        // 这里需要实现根据用户ID过滤的逻辑
        // 暂时返回所有健康提醒，后续需要完善用户数据隔离逻辑
        List<HealthAlerts> healthAlerts = lambdaQuery()
                .orderByDesc(HealthAlerts::getCheckDate)
                .list();
        
        // 转换为VO对象
        return healthAlerts.stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据宠物ID获取健康提醒
     * @param pid 宠物ID
     * @return 健康提醒VO列表
     */
    @Override
    public List<HealthAlertsVo> getHealthAlertsByPetId(Integer pid) {
        log.info("获取宠物健康提醒列表，宠物ID: {}", pid);
        List<HealthAlerts> healthAlerts = lambdaQuery()
                .eq(HealthAlerts::getPid, pid)
                .orderByDesc(HealthAlerts::getCheckDate)
                .list();
        
        // 转换为VO对象
        return healthAlerts.stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());
    }
    
    /**
     * 更新健康提醒状态
     * @param healthId 健康提醒ID
     * @param status 状态
     * @return 是否更新成功
     */
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
    


    /**
     * 创建健康提醒
     * @param healthAlert 健康提醒实体
     * @return 健康提醒VO对象
     */
    @Override
    public HealthAlertsVo createHealthAlert(HealthAlerts healthAlert) {
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
        return convertToVo(healthAlert);
    }
    
    /**
     * 更新健康提醒
     * @param healthAlert 健康提醒实体
     * @return 健康提醒VO对象
     */
    @Override
    public HealthAlertsVo updateHealthAlert(HealthAlerts healthAlert) {
        log.info("更新健康提醒: {}", healthAlert);
        if (healthAlert.getHealthId() == null) {
            throw new IllegalArgumentException("健康检查ID不能为空");
        }
        
        // 检查健康提醒是否存在
        HealthAlerts existingHealthAlert = getById(healthAlert.getHealthId());
        if (existingHealthAlert == null) {
            throw new IllegalArgumentException("健康提醒不存在");
        }
        
        if (!updateById(healthAlert)) {
            throw new IllegalArgumentException("健康提醒更新失败");
        }
        return convertToVo(healthAlert);
    }
    
    /**
     * 删除健康提醒
     * @param healthId 健康提醒ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteHealthAlert(Integer healthId) {
        log.info("删除健康提醒，检查ID: {}", healthId);
        HealthAlerts healthAlert = getById(healthId);
        if (healthAlert == null) {
            throw new IllegalArgumentException("健康提醒不存在");
        }
        return removeById(healthId);
    }
    
    /**
     * 通过DTO创建健康提醒
     * @param healthDto 健康提醒DTO
     * @return 健康提醒VO对象
     */
    @Override
    public HealthAlertsVo createHealthAlert(HealthDto healthDto) {
        log.info("通过DTO创建健康提醒: {}", healthDto);
        
        // 将DTO转换为实体
        HealthAlerts healthAlert = new HealthAlerts();
        healthAlert.setPid(healthDto.getPid());
        healthAlert.setCheckDate(healthDto.getCheckDate());
        healthAlert.setHealthType(healthDto.getHealthType());
        healthAlert.setDescription(healthDto.getDescription());
        healthAlert.setReminderTime(healthDto.getReminderTime());
        healthAlert.setStatus(healthDto.getStatus());
        
        return createHealthAlert(healthAlert);
    }
    
    /**
     * 通过DTO更新健康提醒
     * @param healthId 健康提醒ID
     * @param healthDto 健康提醒DTO
     * @return 健康提醒VO对象
     */
    @Override
    public HealthAlertsVo updateHealthAlert(Integer healthId, HealthDto healthDto) {
        log.info("通过DTO更新健康提醒，ID: {}, 数据: {}", healthId, healthDto);
        
        // 将DTO转换为实体
        HealthAlerts healthAlert = new HealthAlerts();
        healthAlert.setHealthId(healthId);
        healthAlert.setPid(healthDto.getPid());
        healthAlert.setCheckDate(healthDto.getCheckDate());
        healthAlert.setHealthType(healthDto.getHealthType());
        healthAlert.setDescription(healthDto.getDescription());
        healthAlert.setReminderTime(healthDto.getReminderTime());
        healthAlert.setStatus(healthDto.getStatus());
        
        return updateHealthAlert(healthAlert);
    }
}