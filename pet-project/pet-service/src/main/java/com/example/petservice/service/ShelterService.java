package com.example.petservice.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.example.petpojo.entity.Shelters;
import com.example.petpojo.vo.ShelterVo;

import java.util.List;

public interface ShelterService extends IService<Shelters> {

     List<ShelterVo> getShelterNames();
}
