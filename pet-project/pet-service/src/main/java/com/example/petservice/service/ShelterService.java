package com.example.petservice.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.example.petpojo.entity.Shelters;
import com.example.petpojo.vo.ShelterVo;

import java.util.List;

/**
 * 收容所服务接口
 * 定义收容所相关的业务方法
 */
public interface ShelterService extends IService<Shelters> {

    /**
     * 获取所有收容所名称列表
     * @return 收容所VO列表
     */
     List<ShelterVo> getShelterNames();
}
