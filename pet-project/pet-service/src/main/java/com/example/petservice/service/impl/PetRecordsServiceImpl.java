package com.example.petservice.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petpojo.dto.RecordDto;
import com.example.petpojo.entity.PetRecords;
import com.example.petservice.mapper.PetRecordsMapper;
import com.example.petservice.service.PetRecordsService;
import com.example.petcommon.utils.UserContext;

import lombok.extern.slf4j.Slf4j;

/**
 * 宠物事件记录服务实现类
 * 实现宠物事件记录相关的业务逻辑
 */
@Service
@Slf4j
public class PetRecordsServiceImpl extends ServiceImpl<PetRecordsMapper, PetRecords> implements PetRecordsService {

    /**
     * 获取用户的事件记录列表
     */
    @Override
    public List<PetRecords> getUserRecords(Integer uid) {
        log.info("获取用户事件记录列表，用户ID: {}", uid);
        LambdaQueryWrapper<PetRecords> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PetRecords::getUid, uid)
                   .orderByDesc(PetRecords::getRecordTime);
        return this.list(queryWrapper);
    }
    
    /**
     * 获取宠物的事件记录列表
     */
    @Override
    public List<PetRecords> getPetRecords(Integer pid) {
        log.info("获取宠物事件记录列表，宠物ID: {}", pid);
        LambdaQueryWrapper<PetRecords> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PetRecords::getPid, pid)
                   .orderByDesc(PetRecords::getRecordTime);
        return this.list(queryWrapper);
    }
    
    /**
     * 创建事件记录
     */
    @Override
    public PetRecords createRecord(RecordDto recordDto) {
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
        return petRecords;
    }
    
    /**
     * 更新事件记录
     */
    @Override
    public PetRecords updateRecord(Integer recordId, RecordDto recordDto) {
        log.info("更新事件记录，记录ID: {}", recordId);
        
        PetRecords petRecords = this.getById(recordId);
        if (petRecords == null) {
            log.error("事件记录不存在，记录ID: {}", recordId);
            throw new IllegalArgumentException("事件记录不存在，ID: " + recordId);
        }
        
        // 只允许更新部分字段
        petRecords.setEventType(recordDto.getEventType());
        petRecords.setMood(recordDto.getMood());
        petRecords.setDescription(recordDto.getDescription());
        petRecords.setLocation(recordDto.getLocation());
        petRecords.setRecordTime(recordDto.getRecordTime());
        petRecords.setMediaUrl(recordDto.getMediaUrl());
        petRecords.setMediaType(recordDto.getMediaType());
        
        this.updateById(petRecords);
        log.info("事件记录更新成功，记录ID: {}", recordId);
        return petRecords;
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