package com.example.petservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.petpojo.entity.LoginHistory;
import com.example.petpojo.vo.LoginHistoryVo;

import java.util.List;

public interface LoginHistoryService extends IService<LoginHistory> {
    /**
     * 最近七天登录记录
     */
    List<LoginHistoryVo> getLoginHistory();

    /**
     * 清除当前用户的登录历史记录
     */
    void clearLoginHistory();

}