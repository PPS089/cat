package com.example.petservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.petpojo.entity.HealthAlerts;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HealthAlertsMapper extends BaseMapper<HealthAlerts> {
}