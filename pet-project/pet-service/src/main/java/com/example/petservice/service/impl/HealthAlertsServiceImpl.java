package com.example.petservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petpojo.dto.HealthDto;
import com.example.petpojo.entity.HealthAlerts;
import com.example.petpojo.vo.AdoptionsVo;
import com.example.petcommon.exception.BizException;
import com.example.petcommon.error.ErrorCode;
import com.example.petpojo.vo.HealthAlertsVo;
import com.example.petservice.mapper.HealthAlertsMapper;
import com.example.petservice.service.AdoptionsService;
import com.example.petservice.service.HealthAlertsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 健康提醒服务实现类
 * 实现健康提醒相关的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthAlertsServiceImpl extends ServiceImpl<HealthAlertsMapper, HealthAlerts> implements HealthAlertsService {
    
    private final AdoptionsService adoptionsService;
    
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
     * 获取用户的健康提醒列表（包含所有状态，用于清理任务）
     * @param userId 用户ID
     * @return 健康提醒VO列表
     */
    @Override
    public List<HealthAlertsVo> getAllUserHealthAlerts(Integer userId) {
        log.info("获取用户所有健康提醒列表，用户ID: {}", userId);
        
        // 获取用户的领养记录
        List<AdoptionsVo> userAdoptions = adoptionsService.getUserAdoptions(userId.longValue(), 1, 1000);
        
        if (userAdoptions.isEmpty()) {
            log.info("用户没有领养任何宠物，用户ID: {}", userId);
            return List.of(); // 返回空列表
        }
        
        // 提取用户领养的宠物ID
        Set<Integer> userPetIds = userAdoptions.stream()
                .map(AdoptionsVo::getPid)
                .collect(Collectors.toSet());
        
        log.info("用户领养的宠物ID列表: {}", userPetIds);
        
        // 查询这些宠物的所有健康提醒（不限制状态）
        List<HealthAlerts> healthAlerts = lambdaQuery()
                .in(HealthAlerts::getPid, userPetIds)
                .orderByDesc(HealthAlerts::getCheckDate)
                .list();
        
        log.info("查询到用户宠物的所有健康提醒数量: {}", healthAlerts.size());
        
        // 转换为VO对象
        return healthAlerts.stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户的健康提醒列表
     * @param userId 用户ID
     * @return 健康提醒VO列表
     */
    @Override
    public List<HealthAlertsVo> getUserHealthAlerts(Integer userId) {
        log.info("获取用户健康提醒列表，用户ID: {}", userId);
        
        // 获取用户的领养记录
        List<AdoptionsVo> userAdoptions = adoptionsService.getUserAdoptions(userId.longValue(), 1, 1000);
        
        if (userAdoptions.isEmpty()) {
            log.info("用户没有领养任何宠物，用户ID: {}", userId);
            return List.of(); // 返回空列表
        }
        
        // 提取用户领养的宠物ID
        Set<Integer> userPetIds = userAdoptions.stream()
                .map(AdoptionsVo::getPid)
                .collect(Collectors.toSet());
        
        log.info("用户领养的宠物ID列表: {}", userPetIds);
        
        // 查询这些宠物的健康提醒，只返回attention、expired和reminded状态的数据
        List<HealthAlerts> healthAlerts = lambdaQuery()
                .in(HealthAlerts::getPid, userPetIds)
                .in(HealthAlerts::getStatus, Arrays.asList("attention", "expired", "reminded"))
                .orderByDesc(HealthAlerts::getCheckDate)
                .list();
        
        log.info("查询到用户宠物的健康提醒数量: {}", healthAlerts.size());
        
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
     * 创建健康提醒
     * @param healthDto 健康提醒DTO
     * @return 健康提醒VO对象
     */
    @Override
    public HealthAlertsVo createHealthAlert(HealthDto healthDto) {
        log.info("创建健康提醒: {}", healthDto);
        
        // 将DTO转换为实体
        HealthAlerts healthAlert = new HealthAlerts();
        healthAlert.setPid(healthDto.getPid());
        healthAlert.setCheckDate(healthDto.getCheckDate());
        healthAlert.setHealthType(healthDto.getHealthType());
        healthAlert.setDescription(healthDto.getDescription());
        
        if (healthDto.getReminderTime() != null) {
            healthAlert.setReminderTime(healthDto.getReminderTime());
        }
        
        healthAlert.setStatus(healthDto.getStatus());
        
        // 参数校验和默认值设置
        if (healthAlert.getPid() == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "宠物ID不能为空");
        }
        if (healthAlert.getCheckDate() == null) {
            healthAlert.setCheckDate(LocalDateTime.now());
        }
        if (healthAlert.getStatus() == null || healthAlert.getStatus().trim().isEmpty()) {
            healthAlert.setStatus("pending");
        }
        
        save(healthAlert);
        return convertToVo(healthAlert);
    }
    
    /**
     * 更新健康提醒
     * @param healthId 健康提醒ID
     * @param healthDto 健康提醒DTO
     * @return 健康提醒VO对象
     */
    @Override
    public HealthAlertsVo updateHealthAlert(Integer healthId, HealthDto healthDto) {
        log.info("更新健康提醒，ID: {}, 数据: {}", healthId, healthDto);
        
        if (healthId == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "健康检查ID不能为空");
        }
        
        // 检查健康提醒是否存在
        HealthAlerts existingHealthAlert = getById(healthId);
        if (existingHealthAlert == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        
        // 更新字段
        existingHealthAlert.setPid(healthDto.getPid());
        existingHealthAlert.setCheckDate(healthDto.getCheckDate());
        existingHealthAlert.setHealthType(healthDto.getHealthType());
        existingHealthAlert.setDescription(healthDto.getDescription());
        
        if (healthDto.getReminderTime() != null) {
            existingHealthAlert.setReminderTime(healthDto.getReminderTime());
        }
        
        // 设置默认状态为pending
        if (healthDto.getStatus() == null || healthDto.getStatus().trim().isEmpty()) {
            existingHealthAlert.setStatus("pending");
        } else {
            existingHealthAlert.setStatus(healthDto.getStatus());
        }
        
        if (!updateById(existingHealthAlert)) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "健康提醒更新失败");
        }
        return convertToVo(existingHealthAlert);
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
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        return removeById(healthId);
    }
    

}