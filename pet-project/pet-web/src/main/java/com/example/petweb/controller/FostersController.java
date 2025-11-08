package com.example.petweb.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.petcommon.result.Result;
import com.example.petpojo.entity.enums.CommonEnum;
import com.example.petservice.service.FosterService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 寄养管理控制器
 * 提供寄养相关的 REST API 接口
 */
@RestController
@RequestMapping("/fosters")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "寄养管理", description = "寄养相关的 API 接口")
@SecurityRequirement(name = "bearer-key")
public class FostersController {

    private final FosterService fosterService;

    /**
     * 删除寄养记录
     */
    @DeleteMapping("delete/{id}")
    @Operation(summary = "删除寄养记录", description = "根据ID删除寄养记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "400", description = "删除失败"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    public Result<String> deleteFoster(
            @Parameter(description = "寄养记录ID", required = true) @PathVariable Integer id) {
        log.info("删除寄养记录ID: {}", id);
        CommonEnum.FosterDeleteResultEnum result = fosterService.deleteFoster(id);
        
        switch (result) {
            case SUCCESS:
                return Result.success("寄养记录删除成功");
            case PET_IS_FOSTERING:
                return Result.error("宠物正在寄养中，无法删除记录");
            case DELETE_FAILED:
                return Result.error("寄养记录删除失败");
            default:
                return Result.error("未知错误");
        }
    }

}