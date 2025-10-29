package com.example.petservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.petpojo.entity.PetHealth;
import com.example.petpojo.dto.HealthDto;
import com.example.petpojo.vo.HealthVo;

public interface HealthService extends IService<PetHealth> {




    /**
     * 创建宠物健康信息
     */
    boolean createHealth(HealthDto healthDto);

    /**
     * 获取宠物健康信息
     * @return 宠物健康信息
     */
    HealthVo getHealth();

}