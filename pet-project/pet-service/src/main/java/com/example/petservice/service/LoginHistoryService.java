package com.example.petservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.petpojo.entity.LoginHistory;
import com.example.petpojo.vo.LoginHistoryVo;

import java.util.List;

/**
 * 登录历史服务接口
 * 定义登录历史相关的业务方法
 */
public interface LoginHistoryService extends IService<LoginHistory> {
    /**
     * 获取最近七天的登录历史记录
     * @return 登录历史记录VO列表
     */
    List<LoginHistoryVo> getLoginHistory();

    /**
     * 清除当前用户的登录历史记录
     */
    void clearLoginHistory();

}