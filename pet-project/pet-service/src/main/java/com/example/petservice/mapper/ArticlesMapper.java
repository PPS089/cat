package com.example.petservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.petpojo.entity.Articles;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticlesMapper extends BaseMapper<Articles> {
}