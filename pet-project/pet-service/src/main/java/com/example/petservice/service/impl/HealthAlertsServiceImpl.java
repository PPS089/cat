package com.example.petservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petpojo.dto.HealthDto;
import com.example.petpojo.entity.HealthAlerts;
import com.example.petpojo.vo.AdoptionsVo;
import com.example.petcommon.exception.BizException;
import com.example.petcommon.error.ErrorCode;
import com.example.petpojo.vo.HealthAlertsVo;
import com.example.petservice.mapper.AdoptionsMapper;
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
    private final AdoptionsMapper adoptionsMapper;
    private final HealthAlertsMapper healthAlertsMapper;
    
    /**
     * 将HealthAlerts实体转换为HealthAlertsVo
     */
    private HealthAlertsVo convertToVo(HealthAlerts healthAlerts) {
        HealthAlertsVo vo = new HealthAlertsVo();
        vo.setHealthId(healthAlerts.getHealthId());
        vo.setPid(healthAlerts.getPid());
        // 获取宠物对应的用户ID
        vo.setUserId(getUserIdByPetId(healthAlerts.getPid()));
        vo.setCheckDate(healthAlerts.getCheckDate());
        vo.setHealthType(healthAlerts.getHealthType());
        vo.setDescription(healthAlerts.getDescription());
        vo.setReminderTime(healthAlerts.getReminderTime());
        vo.setStatus(healthAlerts.getStatus());
        vo.setCreateTime(healthAlerts.getCreateTime());
        vo.setUpdateTime(healthAlerts.getUpdateTime());
        return vo;
    }
    
    /**
     * 通过宠物ID获取用户ID
     * @param pid 宠物ID
     * @return 用户ID
     */
    private Long getUserIdByPetId(Integer pid) {
        try {
            // 直接查询数据库获取领养记录，从中获取用户ID
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.example.petpojo.entity.Adoptions> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(com.example.petpojo.entity.Adoptions::getPid, pid)
                    .eq(com.example.petpojo.entity.Adoptions::getStatus, com.example.petpojo.entity.enums.CommonEnum.AdoptionStatusEnum.APPROVED)
                    .orderByDesc(com.example.petpojo.entity.Adoptions::getAdoptDate)
                    .last("LIMIT 1");
            com.example.petpojo.entity.Adoptions adoption = adoptionsMapper.selectOne(wrapper);
            
            if (adoption != null) {
                return adoption.getUid().longValue();
            }
        } catch (Exception e) {
            log.warn("获取宠物对应的用户ID失败: pid={}, error={}", pid, e.getMessage());
        }
        return null;
    }
    
    /**
     * 获取用户的健康提醒列表
     * @param userId 用户ID
     * @return 健康提醒VO列表
     */
    @Override
    public List<HealthAlertsVo> getUserHealthAlerts(Integer userId) {
        log.info("获取用户健康提醒列表，用户ID: {}", userId);
        
        // 获取用户已领养成功的宠物（仅 APPROVED）
        List<AdoptionsVo> userAdoptions = adoptionsService.getUserApprovedAdoptions(userId.longValue(), 1, 1000);
        
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
    
    /**
     * 获取需要提醒的健康提醒列表
     * @param start 开始时间
     * @param end 结束时间
     * @param status 状态
     * @return 需要提醒的健康提醒列表
     */
    @Override
    public List<HealthAlertsVo> listNeedRemindAlerts(LocalDateTime start, LocalDateTime end, String status) {
        log.info("获取需要提醒的健康提醒列表: start={}, end={}, status={}", start, end, status);
        
        List<HealthAlerts> healthAlerts = lambdaQuery()
            .eq(HealthAlerts::getStatus, status)
            .ge(HealthAlerts::getReminderTime, start)
            .le(HealthAlerts::getReminderTime, end)
            .list();
        
        log.info("查询到需要提醒的健康提醒数量: {}", healthAlerts.size());
        
        // 转换为VO对象
        return healthAlerts.stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());
    }
    
    /**
     * 通过宠物ID获取宠物名称
     * @param pid 宠物ID
     * @return 宠物名称
     */
    @Override
    public String getPetNameById(Long pid) {
        try {
            String petName = adoptionsService.getPetNameById(pid);
            return petName != null ? petName : "宠物";
        } catch (Exception e) {
            log.warn("获取宠物名称失败: pid={}, error={}", pid, e.getMessage());
            return "宠物";
        }
    }
    
    /**
     * 标记为已提醒
     * @param healthId 健康提醒ID
     */
    @Override
    public void markAsReminded(Integer healthId) {
        HealthAlerts entity = new HealthAlerts();
        entity.setHealthId(healthId);
        entity.setStatus("reminded");
        updateById(entity);
    }
    
    /**
     * 批量将已提醒的健康提醒标记为过期
     * @param now 当前时间
     * @return 更新的记录数
     */
    @Override
    public int batchExpireReminded(LocalDateTime now) {
        return healthAlertsMapper.batchExpireReminded(now, null); // cleanupProperties会在Mapper中处理
    }
    
    /**
     * 批量将过期的健康提醒标记为归档
     * @param now 当前时间
     * @return 更新的记录数
     */
    @Override
    public int batchArchiveExpired(LocalDateTime now) {
        return healthAlertsMapper.batchArchiveExpired(now, null); // cleanupProperties会在Mapper中处理
    }
    
    /**
     * 批量删除已归档的健康提醒
     * @param now 当前时间
     * @return 删除的记录数
     */
    @Override
    public int batchDeleteArchived(LocalDateTime now) {
        return healthAlertsMapper.batchDeleteArchived(now, null); // cleanupProperties会在Mapper中处理
    }
}
