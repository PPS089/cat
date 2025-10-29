package com.example.petservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petpojo.entity.LoginHistory;
import com.example.petservice.mapper.LoginHistoryMapper;
import com.example.petservice.service.LoginHistoryService;
import com.example.petcommon.utils.UserContext;
import com.example.petpojo.vo.LoginHistoryVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoginHistoryServiceImpl extends ServiceImpl<LoginHistoryMapper, LoginHistory> implements LoginHistoryService {
    /**
     * 最近七天登录记录
     */


    private LoginHistoryVo convertToVo(LoginHistory loginHistory) {
        LoginHistoryVo vo = new LoginHistoryVo();
        try {
            // 复制基本属性
            vo.setId(loginHistory.getId());
            vo.setUserId(loginHistory.getUserId());
            vo.setLoginTime(loginHistory.getLoginTime());
            vo.setIpAddress(loginHistory.getIpAddress());
            vo.setDevice(loginHistory.getDevice());
            vo.setLocation(loginHistory.getLocation());
            // 将枚举转换为字符串
            vo.setStatus(loginHistory.getStatus() != null ? loginHistory.getStatus().name() : "SUCCESS");
        } catch (Exception e) {
            // 捕获属性复制异常，包装为运行时异常抛出
            throw new RuntimeException("登录记录转换为VO失败", e);
        }
        return vo;
    }
    @Override
    public List<LoginHistoryVo> getLoginHistory() {
        Long currentUserId = UserContext.getCurrentUserId();
        
        // 添加调试日志
        System.out.println("DEBUG: getLoginHistory called with userId: " + currentUserId);
        
        // 检查用户ID是否有效
        if (currentUserId == null || currentUserId <= 0) {
            System.out.println("DEBUG: Invalid userId: " + currentUserId + ", returning empty list");
            return new ArrayList<>();
        }

        List<LoginHistory> loginHistories = lambdaQuery()
                .eq(LoginHistory::getUserId, currentUserId.longValue())
                .ge(LoginHistory::getLoginTime, LocalDateTime.now().minusDays(7))
                .orderByDesc(LoginHistory::getLoginTime)
                .list();
        
        System.out.println("DEBUG: Found " + loginHistories.size() + " login history records for userId: " + currentUserId);
        if (!loginHistories.isEmpty()) {
            System.out.println("DEBUG: First record userId: " + loginHistories.get(0).getUserId() + ", loginTime: " + loginHistories.get(0).getLoginTime());
        } else {
            System.out.println("DEBUG: No login history found for userId: " + currentUserId + " in the last 7 days");
            // 检查是否有任何历史记录（不限时间）
            List<LoginHistory> allHistory = lambdaQuery()
                    .eq(LoginHistory::getUserId, currentUserId.longValue())
                    .orderByDesc(LoginHistory::getLoginTime)
                    .list();
            System.out.println("DEBUG: Total login history records for userId " + currentUserId + ": " + allHistory.size());
        }

        List<LoginHistoryVo> loginHistoryVos=loginHistories
                .stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());

        return loginHistoryVos;
    }

    @Override
    public void clearLoginHistory() {
        Long currentUserId = UserContext.getCurrentUserId();
        
        // 删除当前用户的所有登录历史记录
        lambdaUpdate()
                .eq(LoginHistory::getUserId, currentUserId.longValue())
                .remove();
    }

}