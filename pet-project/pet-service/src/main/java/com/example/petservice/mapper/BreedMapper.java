package com.example.petservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.petpojo.entity.Breed;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BreedMapper extends BaseMapper<Breed> {
}

