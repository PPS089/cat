package com.example.petservice.mapper;

import com.example.petpojo.entity.PetHealth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.petpojo.vo.HealthVo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PetHealthMapper extends BaseMapper<PetHealth> {



    /**
     * 根据用户ID查询宠物健康信息
     * @param userId 用户ID
     * @return 宠物健康信息
     */
    HealthVo getHealth(Integer userId);

}