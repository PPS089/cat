package com.example.petweb.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.petcommon.result.Result;
import com.example.petpojo.entity.Shelters;
import com.example.petservice.service.ShelterService;
import com.example.petpojo.vo.ShelterVo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 收容所管理控制器
 * 提供收容所相关的 REST API 接口
 */
@RestController
@RequestMapping("/shelters")
@RequiredArgsConstructor
@Slf4j
public class ShelterController {

    private final ShelterService shelterService;

    /**
     * 获取所有收容所列表
     */
    @GetMapping
    public Result<List<ShelterVo>> getAllShelters() {
        log.info("获取收容所列表");
        List<ShelterVo> shelters = shelterService.getShelterNames();
        if(!shelters.isEmpty()) return Result.success(shelters);
        return Result.success(shelters);
    }
}