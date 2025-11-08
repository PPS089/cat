package com.example.petweb.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.petcommon.result.Result;
import com.example.petservice.service.ShelterService;
import com.example.petpojo.vo.ShelterVo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "收容所管理", description = "收容所相关的 API 接口")
@SecurityRequirement(name = "bearer-key")
public class ShelterController {

    private final ShelterService shelterService;

    /**
     * 获取所有收容所列表
     */
    @GetMapping
    @Operation(summary = "获取所有收容所列表", description = "获取系统中所有收容所的列表信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    public Result<List<ShelterVo>> getAllShelters() {
        log.info("获取收容所列表");
        List<ShelterVo> shelters = shelterService.getShelterNames();
        if(!shelters.isEmpty()) return Result.success(shelters);
        return Result.success(shelters);
    }
}