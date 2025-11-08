package com.example.petweb.controller;

import com.example.petcommon.context.UserContext;
import com.example.petcommon.result.Result;
import com.example.petservice.service.LoginHistoryService;
import com.example.petpojo.vo.LoginHistoryVo;

import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 登录历史记录控制器
 * 提供登录历史记录相关的 REST API 接口
 */
@RestController
@RequestMapping("/login-history")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "登录历史记录", description = "登录历史记录相关的 API 接口")
@SecurityRequirement(name = "bearer-key")
public class LoginHistoryController {

    private final LoginHistoryService loginHistoryService;
    /**
     * 获取登录历史记录
     * @return 登录历史记录列表
     */
    @GetMapping
    @Operation(summary = "获取登录历史记录", description = "获取当前用户最近七天的登录历史记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    public Result<List<LoginHistoryVo>> getLoginHistory() {
        List<LoginHistoryVo> loginHistoryList = loginHistoryService.getLoginHistory();
        return Result.success(loginHistoryList);
    }

    /**
     * 清除登录历史记录
     */
    @DeleteMapping("/clear")
    @Operation(summary = "清除登录历史记录", description = "清除当前用户的所有登录历史记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "清除成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    public Result<Void> clearLoginHistory() {
        loginHistoryService.clearLoginHistory();
        return Result.success();
    }
}