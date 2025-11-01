package com.example.petweb.controller;

import com.example.petcommon.context.UserContext;
import com.example.petcommon.result.Result;
import com.example.petservice.service.LoginHistoryService;
import com.example.petpojo.vo.LoginHistoryVo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/login-history")
@Slf4j
@RequiredArgsConstructor
public class LoginHistoryController {

    private final LoginHistoryService loginHistoryService;
    /**
     * 分页查询登录记录
     */
    @GetMapping
    public Result<List<LoginHistoryVo>> getLoginHistory() {
        // 添加调试日志
        Long currentUserId = UserContext.getCurrentUserId();
        System.out.println("DEBUG: LoginHistoryController.getLoginHistory called with userId: " + currentUserId);

        List<LoginHistoryVo> loginHistoryList = loginHistoryService.getLoginHistory();
        System.out.println("DEBUG: Returning " + loginHistoryList.size() + " login history records");

        return Result.success(loginHistoryList);
    }

    /**
     * 清除登录历史记录
     */
    @DeleteMapping("/clear")
    public Result<Void> clearLoginHistory() {
        loginHistoryService.clearLoginHistory();
        return Result.success();
    }
}