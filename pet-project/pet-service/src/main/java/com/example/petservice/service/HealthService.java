package com.example.petservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.petpojo.entity.PetHealth;
import com.example.petpojo.dto.HealthDto;
import com.example.petpojo.vo.HealthVo;

/**
 * 健康服务接口
 * 定义宠物健康信息相关的业务方法
 */
public interface HealthService extends IService<PetHealth> {




    /**
     * 创建宠物健康信息
     * @param healthDto 健康信息DTO
     * @return 是否创建成功
     */
    boolean createHealth(HealthDto healthDto);

    /**
     * 获取宠物健康信息
     * @return 宠物健康信息VO对象
     */
    HealthVo getHealth();

}