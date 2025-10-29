package com.example.petservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.petpojo.entity.Pets;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PetsMapper extends BaseMapper<Pets> {
    /**
     * 获取已领养宠物名字列表
     */
    List<String> getAdoptionPetNames();
}