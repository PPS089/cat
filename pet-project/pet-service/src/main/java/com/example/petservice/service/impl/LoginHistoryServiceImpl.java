package com.example.petservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petcommon.context.UserContext;
import com.example.petpojo.entity.LoginHistory;
import com.example.petservice.mapper.LoginHistoryMapper;
import com.example.petservice.service.LoginHistoryService;
import com.example.petpojo.vo.LoginHistoryVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录历史服务实现类
 * 实现登录历史相关的业务逻辑
 */
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
    /**
     * 获取最近七天的登录历史记录
     * @return 登录历史记录VO列表
     */
    @Override
    public List<LoginHistoryVo> getLoginHistory() {
        Long currentUserId = UserContext.getCurrentUserId();
        
        // 检查用户ID是否有效
        if (currentUserId == null || currentUserId <= 0) {
            return new ArrayList<>();
        }

        List<LoginHistory> loginHistories = lambdaQuery()
                .eq(LoginHistory::getUserId, currentUserId.longValue())
                .ge(LoginHistory::getLoginTime, LocalDateTime.now().minusDays(7))
                .orderByDesc(LoginHistory::getLoginTime)
                .list();

        return loginHistories.stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());
    }

    /**
     * 清除当前用户的登录历史记录
     */
    @Override
    public void clearLoginHistory() {
        Long currentUserId = UserContext.getCurrentUserId();
        
        // 删除当前用户的所有登录历史记录
        lambdaUpdate()
                .eq(LoginHistory::getUserId, currentUserId.longValue())
                .remove();
    }

}