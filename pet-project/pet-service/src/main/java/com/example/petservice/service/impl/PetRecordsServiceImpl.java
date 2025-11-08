package com.example.petservice.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petcommon.context.UserContext;
import com.example.petpojo.dto.RecordDto;
import com.example.petpojo.entity.PetRecords;
import com.example.petpojo.vo.EventVo;
import com.example.petservice.mapper.PetRecordsMapper;
import com.example.petservice.service.MediaFilesService;
import com.example.petservice.service.PetRecordsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 宠物事件记录服务实现类
 * 实现宠物事件记录相关的业务逻辑
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PetRecordsServiceImpl extends ServiceImpl<PetRecordsMapper, PetRecords> implements PetRecordsService {

    private final MediaFilesService mediaFilesService;

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
     * 创建事件记录
     */
    @Override
    public EventVo createRecord(RecordDto recordDto) {
        log.info("创建事件记录，用户ID: {}, 宠物ID: {}", recordDto.getUid(), recordDto.getPid());
        
        // 如果用户ID为空，使用当前登录用户ID
        if (recordDto.getUid() == null) {
            Long userId = UserContext.getCurrentUserId();
            recordDto.setUid(userId.intValue());
        }
        
        PetRecords petRecords = new PetRecords();
        BeanUtils.copyProperties(recordDto, petRecords);
        
        // 设置记录时间为当前时间（如果未提供）
        if (petRecords.getRecordTime() == null) {
            petRecords.setRecordTime(LocalDateTime.now());
        }
        
        this.save(petRecords);
        log.info("事件记录创建成功，记录ID: {}", petRecords.getRecordId());
        
        // 返回EventVo对象
        return getEventVoById(petRecords.getRecordId());
    }
    
    /**
     * 更新事件记录
     */
    @Override
    public EventVo updateRecord(Integer recordId, RecordDto recordDto) {
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