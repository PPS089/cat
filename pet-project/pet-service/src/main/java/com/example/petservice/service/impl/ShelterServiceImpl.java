package com.example.petservice.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petpojo.entity.Shelters;
import com.example.petservice.mapper.SheltersMapper;
import com.example.petservice.service.ShelterService;
import com.example.petpojo.vo.ShelterVo;

@Service
public class ShelterServiceImpl extends ServiceImpl<SheltersMapper, Shelters> implements ShelterService {

    @Override
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