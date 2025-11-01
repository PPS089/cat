package com.example.petweb.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.petcommon.result.Result;
import com.example.petservice.service.FosterService;

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
public class FostersController {

    private final FosterService fosterService;

    /**
     * 删除寄养记录
     * @param id 寄养记录ID
     * @return 删除结果
     */
    @DeleteMapping("delete/{id}")
    public Result<String> deleteFoster(
            @PathVariable Long id) {
        log.info("删除寄养记录ID: {}", id);
        boolean deleted = fosterService.deleteFoster(id);
        if (deleted) {
            return Result.success("寄养记录删除成功");
        } else {
            return Result.error("寄养记录删除失败");
        }
    }

}