package com.example.petservice.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.petpojo.entity.Users;

@Mapper
public interface UsersMapper  extends BaseMapper<Users>{

}