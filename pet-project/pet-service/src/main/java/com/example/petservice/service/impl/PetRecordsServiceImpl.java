package com.example.petservice.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petcommon.context.UserContext;
import com.example.petpojo.dto.RecordDto;
import com.example.petpojo.entity.Adoptions;
import com.example.petpojo.entity.Fosters;
import com.example.petpojo.entity.PetRecords;
import com.example.petpojo.entity.Pets;
import com.example.petpojo.vo.EventVo;
import com.example.petservice.mapper.PetRecordsMapper;
import com.example.petservice.service.AdoptionsService;
import com.example.petservice.service.FosterService;
import com.example.petservice.service.MediaFilesService;
import com.example.petservice.service.PetsService;
import com.example.petservice.service.PetRecordsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

/**
 * 宠物事件记录服务实现类
 * 实现宠物事件记录相关的业务逻辑
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PetRecordsServiceImpl extends ServiceImpl<PetRecordsMapper, PetRecords> implements PetRecordsService {

    private final MediaFilesService mediaFilesService;
    private final AdoptionsService adoptionsService;
    private final FosterService fosterService;
    private final PetsService petsService;

    /**
     * 获取用户的事件记录列表
     */
    @Override
    public List<EventVo> getUserRecords(Integer uid) {
        log.info("获取用户事件记录列表，用户ID: {}", uid);
        // 使用Mapper直接查询EventVo列表
        return baseMapper.getEventVosByUserId(uid);
    }
    
    /**
     * 获取宠物的事件记录列表
     */
    @Override
    public List<EventVo> getPetRecords(Integer pid) {
        log.info("获取宠物事件记录列表，宠物ID: {}", pid);
        // 使用Mapper直接查询EventVo列表
        return baseMapper.getEventVosByPetId(pid);
    }
    
    /**
     * 获取用户的事件记录列表，直接返回EventVo
     */
    @Override
    public List<EventVo> getUserEventVos(Integer uid) {
        log.info("获取用户事件记录列表，用户ID: {}", uid);
        List<EventVo> eventVos = baseMapper.getEventVosByUserId(uid);
        
        // 为每个事件加载媒体文件
        for (EventVo eventVo : eventVos) {
            List<com.example.petpojo.vo.MediaFileVo> mediaList = mediaFilesService.getMediaByRecordId(eventVo.getEid());
            eventVo.setMediaList(mediaList);
        }
        
        return eventVos;
    }
    
    /**
     * 根据记录ID获取事件记录，直接返回EventVo
     */
    @Override
    public EventVo getEventVoById(Integer recordId) {
        log.info("获取事件记录，记录ID: {}", recordId);
        EventVo eventVo = baseMapper.getEventVoById(recordId);
        
        if (eventVo != null) {
            // 加载媒体文件
            List<com.example.petpojo.vo.MediaFileVo> mediaList = mediaFilesService.getMediaByRecordId(recordId);
            eventVo.setMediaList(mediaList);
        }
        
        return eventVo;
    }
    
    /**
     * 检查宠物是否属于用户（通过领养或寄养关系）
     * @param petId 宠物ID
     * @param userId 用户ID
     * @return 是否属于用户
     */
    private boolean isPetOwnedByUser(Integer petId, Integer userId) {
        try {
            // 检查宠物是否存在
            Pets pet = petsService.getById(petId);
            if (pet == null) {
                log.warn("宠物不存在，宠物ID: {}", petId);
                return false;
            }
            
            // 检查是否是领养关系
            LambdaQueryWrapper<Adoptions> adoptionQuery = new LambdaQueryWrapper<>();
            adoptionQuery.eq(Adoptions::getPid, petId)
                         .eq(Adoptions::getUid, userId);
            Adoptions adoption = adoptionsService.getOne(adoptionQuery);
            if (adoption != null) {
                return true;
            }
            
            // 检查是否是寄养关系
            LambdaQueryWrapper<Fosters> fosterQuery = new LambdaQueryWrapper<>();
            fosterQuery.eq(Fosters::getPid, petId)
                       .eq(Fosters::getUid, userId);
            Fosters foster = fosterService.getOne(fosterQuery);
            if (foster != null) {
                return true;
            }
            
            log.warn("宠物不属于用户，宠物ID: {}，用户ID: {}", petId, userId);
            return false;
        } catch (Exception e) {
            log.error("检查宠物所有权时发生错误，宠物ID: {}，用户ID: {}", petId, userId, e);
            // 如果检查过程中出现异常，我们不能确定用户是否有权限，抛出带有具体信息的运行时异常
            throw new RuntimeException("检查宠物所有权时发生错误: " + e.getMessage());
        }
    }

    /**
     * 创建事件记录
     */
    @Override
    public EventVo createRecord(RecordDto recordDto) {
        log.info("创建事件记录，用户ID: {}, 宠物ID: {}", recordDto.getUid(), recordDto.getPid());
        
        try {
            // 如果用户ID为空，使用当前登录用户ID
            if (recordDto.getUid() == null) {
                Long userId = UserContext.getCurrentUserId();
                recordDto.setUid(userId.intValue());
            }
            
            // 验证用户是否有权限为该宠物创建记录
            Integer userId = recordDto.getUid();
            Integer petId = recordDto.getPid();
            
            // 检查宠物是否存在且属于该用户
            if (!isPetOwnedByUser(petId, userId)) {
                log.warn("用户 {} 无权限为宠物 {} 创建事件记录", userId, petId);
                throw new IllegalArgumentException("宠物不存在或不属于当前用户");
            }
            
            PetRecords petRecords = new PetRecords();
            BeanUtils.copyProperties(recordDto, petRecords);
            
            // 设置记录时间为当前时间（如果未提供）
            if (petRecords.getRecordTime() == null) {
                petRecords.setRecordTime(LocalDateTime.now());
            }
            
            // 尝试保存记录
            boolean saved = this.save(petRecords);
            if (!saved) {
                log.error("事件记录创建失败，用户ID: {}, 宠物ID: {}", userId, petId);
                throw new RuntimeException("事件记录创建失败");
            }
            
            log.info("事件记录创建成功，记录ID: {}", petRecords.getRecordId());
            
            // 返回EventVo对象
            return getEventVoById(petRecords.getRecordId());
        } catch (IllegalArgumentException e) {
            // 重新抛出参数异常，让控制器处理
            throw e;
        } catch (RuntimeException e) {
            // 重新抛出运行时异常，让控制器处理
            throw e;
        } catch (Exception e) {
            log.error("创建事件记录时发生未预期的错误: {}", e.getMessage(), e);
            throw new RuntimeException("创建事件记录时发生未预期的错误");
        }
    }
    
    /**
     * 更新事件记录
     */
    @Override
    public EventVo updateRecord(@NonNull Integer recordId, @NonNull RecordDto recordDto) {
        log.info("更新事件记录，记录ID: {}", recordId);
        
        PetRecords petRecords = this.getById(recordId);
        if (petRecords == null) {
            log.error("事件记录不存在，记录ID: {}", recordId);
            throw new IllegalArgumentException("事件记录不存在，ID: " + recordId);
        }
        
        // 更新所有字段
        BeanUtils.copyProperties(recordDto, petRecords);
        
        // 手动设置更新时间，确保updatedAt字段被更新
        petRecords.setUpdatedAt(LocalDateTime.now());
        
        // 直接调用updateById，MyBatis Plus会自动更新updatedAt字段（使用非严格模式的fillStrategy）
        boolean updated = this.updateById(petRecords);
        if (updated) {
            log.info("事件记录更新成功，记录ID: {}，更新时间: {}", recordId, petRecords.getUpdatedAt());
        } else {
            log.warn("事件记录更新失败，记录ID: {}", recordId);
        }
        
        // 返回EventVo对象
        return getEventVoById(recordId);
    }
    
    /**
     * 删除事件记录
     */
    @Override
    public boolean deleteRecord(Integer recordId) {
        log.info("删除事件记录，记录ID: {}", recordId);
        
        PetRecords petRecords = this.getById(recordId);
        if (petRecords == null) {
            log.error("事件记录不存在，记录ID: {}", recordId);
            return false;
        }
        
        boolean result = this.removeById(recordId);
        if (result) {
            log.info("事件记录删除成功，记录ID: {}", recordId);
        } else {
            log.error("事件记录删除失败，记录ID: {}", recordId);
        }
        return result;
    }
}