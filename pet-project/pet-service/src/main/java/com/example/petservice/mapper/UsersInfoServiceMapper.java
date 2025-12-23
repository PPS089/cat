package com.example.petservice.mapper;

import com.example.petpojo.entity.Users;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UsersInfoServiceMapper extends BaseMapper<Users> {
}
