package com.example.petservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.petpojo.entity.Pets;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ListPetsMapper extends BaseMapper<Pets> {
    IPage<Pets> selectByPage(Page<Pets> page);
    Pets selectPetById(@Param("petId") Integer petId);
//    IPage<ListPetsVo> listPets(@Param("current_page") Integer currentPage, @Param("per_page") Integer pageSize);
}
