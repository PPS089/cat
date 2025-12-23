package com.example.petweb.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.petcommon.result.Result;
import com.example.petservice.service.FosterService;
import jakarta.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 寄养管理控制器
 * 提供寄养相关的 REST API 接口
 * @author 33185
 */
@RestController
@RequestMapping("/fosters")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "寄养管理", description = "寄养相关的 API 接口")
@SecurityRequirement(name = "bearer-key")
@Validated
public class FostersController {

    private final FosterService fosterService;

    /**
     * 删除寄养记录
     */
    @DeleteMapping("delete/{id}")
    @Operation(summary = "删除寄养记录", description = "根据ID删除寄养记录")
    public Result<String> deleteFoster(
            @PathVariable("id") @Positive(message = "寄养记录ID必须为正整数") Integer id) {
        log.info("删除寄养记录ID: {}", id);
        fosterService.deleteFoster(id);
        return Result.success("寄养记录删除成功");
    }

}
