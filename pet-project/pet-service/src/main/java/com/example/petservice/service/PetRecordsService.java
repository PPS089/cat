package com.example.petservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.petpojo.dto.RecordDto;
import com.example.petpojo.entity.PetRecords;

import java.util.List;

/**
 * 宠物事件记录服务接口
 * 定义宠物事件记录相关的业务逻辑接口
 */
public interface PetRecordsService extends IService<PetRecords> {

    /**
     * 获取用户的事件记录列表
     */
    List<PetRecords> getUserRecords(Integer uid);
    
    /**
     * 获取宠物的事件记录列表
     */
    List<PetRecords> getPetRecords(Integer pid);
    
    /**
     * 创建事件记录
     */
    PetRecords createRecord(RecordDto recordDto);
    
    /**
     * 更新事件记录
     */
    PetRecords updateRecord(Integer recordId, RecordDto recordDto);
    
    /**
     * 删除事件记录
     */
    boolean deleteRecord(Integer recordId);
}