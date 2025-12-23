package com.example.petservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.petpojo.entity.Pets;
import com.example.petpojo.vo.PetListVo; // 添加新的VO导入
import com.example.petpojo.vo.PetsDetailsVo; // 添加详情VO导入

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ListPetsMapper extends BaseMapper<Pets> {
    IPage<Pets> selectByPage(Page<Pets> page);
    Pets selectPetById(@Param("petId") Integer petId);
    
    // 添加新的方法用于直接查询VO
    IPage<PetListVo> selectPetListByPage(Page<PetListVo> page, String species, String breed, String gender, Integer minAge, Integer maxAge);
    PetListVo selectPetListById(@Param("petId") Integer petId);
    
    // 添加查询宠物详情的方法
    PetsDetailsVo selectPetDetailsById(@Param("petId") Integer petId);
}