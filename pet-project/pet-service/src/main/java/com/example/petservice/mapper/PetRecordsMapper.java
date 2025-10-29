package com.example.petservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.petpojo.entity.PetRecords;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PetRecordsMapper extends BaseMapper<PetRecords> {
    /**
     * 根据用户ID获取事件记录列表
     */
    List<PetRecords> getRecordsByUserId(Integer uid);
    
    /**
     * 根据宠物ID获取事件记录列表
     */
    List<PetRecords> getRecordsByPetId(Integer pid);
}