package com.example.petservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.petpojo.entity.Pets;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PetTaskMapper extends BaseMapper<Pets> {

}
