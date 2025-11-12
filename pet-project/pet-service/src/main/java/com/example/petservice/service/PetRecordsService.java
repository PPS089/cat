package com.example.petservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.petpojo.dto.RecordDto;
import com.example.petpojo.entity.PetRecords;
import com.example.petpojo.vo.EventVo;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * 宠物事件记录服务接口
 * 定义宠物事件记录相关的业务逻辑接口
 */
public interface PetRecordsService extends IService<PetRecords> {

    /**
     * 获取用户的事件记录列表
     */
    List<EventVo> getUserRecords(Integer uid);
    
    /**
     * 获取宠物的事件记录列表
     */
    List<EventVo> getPetRecords(Integer pid);
    
    /**
     * 获取用户的事件记录列表，直接返回EventVo
     */
    List<EventVo> getUserEventVos(Integer uid);
    
    /**
     * 根据记录ID获取事件记录，直接返回EventVo
     */
    EventVo getEventVoById(Integer recordId);
    
    /**
     * 创建事件记录
     */
    EventVo createRecord(RecordDto recordDto);
    
    /**
     * 更新事件记录
     */
    EventVo updateRecord(@NonNull Integer recordId, @NonNull RecordDto recordDto);
    
    /**
     * 删除事件记录
     */
    boolean deleteRecord(Integer recordId);
}