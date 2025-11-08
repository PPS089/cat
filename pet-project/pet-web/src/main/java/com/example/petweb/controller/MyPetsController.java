package com.example.petweb.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.petcommon.result.Result;
import com.example.petservice.service.ListPetsService;
import com.example.petpojo.vo.PetListVo; // 更改为新的VO类

import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;


/**
 * 宠物信息控制器
 * 提供宠物信息相关的 REST API 接口
 */
@RestController
@RequestMapping("/pets/info")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "宠物信息", description = "宠物信息相关的 API 接口")
@SecurityRequirement(name = "bearer-key")
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
    @Operation(summary = "分页查询宠物列表", description = "分页查询可领养的宠物列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    public Result<IPage<PetListVo>> listPets(
            @Parameter(description = "当前页码", required = true) @RequestParam("current_page") Integer currentPage,
            @Parameter(description = "每页数量", required = true) @RequestParam("per_page") Integer pageSize) {

        IPage<PetListVo> petsPage = listPetsService.listPets(currentPage, pageSize);
        return Result.success(petsPage);

    }


   

    /**
     * 我的宠物界面根据ID获取宠物详情，用于编辑
     * @param petId 宠物ID
     * @return 宠物详情
     */
    @GetMapping("/available/{petId}")
    @Operation(summary = "根据ID获取宠物详情", description = "根据宠物ID获取可领养宠物的详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "404", description = "宠物不存在")
    })
    public Result<PetListVo> getPetById(
            @Parameter(description = "宠物ID", required = true) @PathVariable("petId") Integer petId) {
        log.info("获取宠物详情，宠物ID: {}", petId);
        
        // 调用service方法获取宠物详情
        PetListVo pet = listPetsService.getPetById(petId);
        
        if (pet == null) {
            return Result.error("未找到该宠物");
        }
        
        return Result.success(pet);
    }

}