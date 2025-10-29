package com.example.petservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petpojo.dto.HealthDto;
import com.example.petpojo.entity.PetHealth;
import com.example.petservice.mapper.PetHealthMapper;
import com.example.petservice.service.HealthService;
import com.example.petcommon.utils.UserContext;
import com.example.petpojo.vo.HealthVo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HealthServiceImpl extends ServiceImpl<PetHealthMapper, PetHealth> implements HealthService {

    private final PetHealthMapper petHealthMapper;

    /**
     * 创建宠物健康信息
     */
    @Override
    public boolean createHealth(HealthDto healthDto) {
        PetHealth petHealth = new PetHealth();
        BeanUtils.copyProperties(healthDto, petHealth);
        return save(petHealth);
    }

    /**
     * 根据用户ID查询宠物健康信息
     * @return 宠物健康信息
     */
    @Override
    public HealthVo getHealth(){
        Integer userId = UserContext.getCurrentUserId().intValue();
        HealthVo petHealth = petHealthMapper.getHealth(userId);
        return petHealth;
    }
}