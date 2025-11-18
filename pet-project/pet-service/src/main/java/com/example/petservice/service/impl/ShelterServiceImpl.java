package com.example.petservice.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petpojo.entity.Shelters;
import com.example.petservice.mapper.SheltersMapper;
import com.example.petservice.service.ShelterService;
import com.example.petpojo.vo.ShelterVo;

/**
 * 收容所服务实现类
 * 实现收容所相关的业务逻辑
 */
@Service
public class ShelterServiceImpl extends ServiceImpl<SheltersMapper, Shelters> implements ShelterService {

    /**
     * 获取所有收容所名称列表
     * @return 收容所VO列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<ShelterVo> getShelterNames() {
        return lambdaQuery()
                .list()
                .stream()
                .map(shelter -> new ShelterVo(
                        shelter.getSid(),
                        shelter.getName(),
                        shelter.getLocation()
                ))
                .collect(Collectors.toList());
    }

}