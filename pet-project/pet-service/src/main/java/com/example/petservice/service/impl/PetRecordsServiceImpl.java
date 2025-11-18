package com.example.petservice.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petcommon.context.UserContext;
import com.example.petcommon.error.ErrorCode;
import com.example.petcommon.exception.BizException;
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
        return baseMapper.getEventVosByUserId(uid);
    }
    
    /**
     * 获取宠物的事件记录列表
     */
    @Override
    public List<EventVo> getPetRecords(Integer pid) {
        return baseMapper.getEventVosByPetId(pid);
    }
    
    /**
     * 获取用户的事件记录列表，直接返回EventVo
     */
    @Override
    public List<EventVo> getUserEventVos(Integer uid) {
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
            throw new BizException(ErrorCode.INTERNAL_ERROR, "检查宠物所有权时发生错误");
        }
    }

    /**
     * 创建事件记录
     */
    @Override
    public EventVo createRecord(RecordDto recordDto) {
        log.info("创建事件记录，用户ID: {}, 宠物ID: {}",recordDto.getPid());
        
        Long userId = UserContext.getCurrentUserId();
        Integer petId = recordDto.getPid();
        
        // 检查宠物是否存在且属于该用户
        if (!isPetOwnedByUser(petId, userId.intValue())) {
            log.warn("用户 {} 无权限为宠物 {} 创建事件记录", userId, petId);
            throw new BizException(ErrorCode.FORBIDDEN, "宠物不存在或不属于当前用户");
        }
        
        PetRecords petRecords = new PetRecords();
        BeanUtils.copyProperties(recordDto, petRecords);
        
        // 设置用户ID
        petRecords.setUid(userId.intValue());
        
        // 设置记录时间为当前时间（如果未提供）
        if (petRecords.getRecordTime() == null) {
            petRecords.setRecordTime(LocalDateTime.now());
        }
        
        // 尝试保存记录
        boolean saved = this.save(petRecords);
        if (!saved) {
            log.error("事件记录创建失败，用户ID: {}, 宠物ID: {}", userId, petId);
            throw new BizException(ErrorCode.INTERNAL_ERROR, "事件记录创建失败");
        }
        
        log.info("事件记录创建成功，记录ID: {}", petRecords.getRecordId());
        
        // 返回EventVo对象
        return getEventVoById(petRecords.getRecordId());
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
            throw new BizException(ErrorCode.NOT_FOUND, "事件记录不存在，ID: " + recordId);
        }
        
        // 更新所有字段
        BeanUtils.copyProperties(recordDto, petRecords);
        
        // 手动设置更新时间，确保updatedAt字段被更新
        petRecords.setUpdatedAt(LocalDateTime.now());
        
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
            log.warn("事件记录不存在，记录ID: {}", recordId);
            throw new BizException(ErrorCode.BAD_REQUEST, "删除失败，事件记录不存在");
        }
        
        boolean result = this.removeById(recordId);
        if (result) {
            log.info("事件记录删除成功，记录ID: {}", recordId);
        } else {
            log.error("事件记录删除失败，记录ID: {}", recordId);
            throw new BizException(ErrorCode.INTERNAL_ERROR, "删除事件记录失败");
        }
        return result;
    }
}