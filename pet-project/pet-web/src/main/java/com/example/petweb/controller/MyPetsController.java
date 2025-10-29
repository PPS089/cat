package com.example.petweb.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.petcommon.result.Result;
import com.example.petservice.service.ListPetsService;
import com.example.petpojo.vo.ListPetsVo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/pets/info")
@RequiredArgsConstructor
@Slf4j
public class MyPetsController {

    /* 1. 注入 Service，用 Lombok 的 @RequiredArgsConstructor 生成构造器 */
    private final ListPetsService listPetsService;

    /**
     * 分页查询宠物列表
     * @param currentPage 当前页码
     * @param pageSize 每页数量
     * @return 宠物列表
     */
    @GetMapping("/available")
    public Result<IPage<ListPetsVo>> listPets(
            @RequestParam("current_page") Integer currentPage,
            @RequestParam("per_page") Integer pageSize) {

        IPage<ListPetsVo> petsPage = listPetsService.listPets(currentPage, pageSize);
        return Result.success(petsPage);

    }


   

    /**
     * 我的宠物界面根据ID获取宠物详情，用于编辑
     * @param petId 宠物ID
     * @return 宠物详情
     */
    @GetMapping("/available/{petId}")
    public Result<ListPetsVo> getPetById(
            @PathVariable("petId") Integer petId) {
        log.info("获取宠物详情，宠物ID: {}", petId);
        
        // 调用service方法获取宠物详情
        ListPetsVo pet = listPetsService.getPetById(petId);
        
        if (pet == null) {
            return Result.error("未找到该宠物");
        }
        
        return Result.success(pet);
    }

}