package com.example.petservice.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.petpojo.vo.ListPetsVo;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petpojo.entity.Pets;
import com.example.petservice.mapper.ListPetsMapper;
import com.example.petservice.service.ListPetsService;


import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
@RequiredArgsConstructor
public class ListPetsServiceImpl extends ServiceImpl<ListPetsMapper, Pets> implements ListPetsService {
   
    private final ListPetsMapper listPetsMapper;


    private ListPetsVo convertToVo(Pets pet) {
        ListPetsVo vo = new ListPetsVo();
        vo.setPid(pet.getPid()); // 前端期望pid而不是id
        vo.setName(pet.getName());
        vo.setSpecies(pet.getSpecies() != null ? pet.getSpecies() : "未知物种"); // 添加物种字段
        vo.setBreed(pet.getBreed());
        vo.setAge(pet.getAge());
        vo.setGender(pet.getGender()); // 确保是"公"/"母"格式
        vo.setImageUrl(pet.getImageUrl());
        vo.setStatus(pet.getStatus() != null ? pet.getStatus().name() : "UNADOPTED"); // 状态值
        
        // 直接使用查询结果中的收容所信息
        if (pet.getShelterName() != null) {
            vo.setShelterName(pet.getShelterName());
            vo.setShelterAddress(pet.getShelterLocation());
        } else {
            vo.setShelterName("未知收容所");
            vo.setShelterAddress("未知地址");
        }
        
        return vo;
    }


    @SneakyThrows
    @Override
    public IPage<ListPetsVo> listPets(@Param("current_page") Integer currentPage, @Param("per_page") Integer pageSize) {
        // 确保分页参数有效
        int current = (currentPage != null && currentPage > 0) ? currentPage : 1;
        int size = (pageSize != null && pageSize > 0) ? pageSize : 10;
        
        Page<Pets> page = new Page<>(current, size);
        
        try {
            // 使用自定义的JOIN查询
            IPage<Pets> result = listPetsMapper.selectByPage(page);
            return result.convert(this::convertToVo);
        } catch (Exception e) {
            log.error("查询未领养宠物列表失败", e);
            throw new RuntimeException("查询未领养宠物列表失败", e);
        }
    }

    @Override
    public ListPetsVo getPetById(Integer petId) {
        log.info("根据ID查询宠物详情，宠物ID: {}", petId);
        
        try {
            // 使用自定义查询直接根据ID获取宠物
            Pets pet = listPetsMapper.selectPetById(petId);
            
            log.info("查询结果: pet = {}", pet);
            
            if (pet != null) {
                ListPetsVo petVo = convertToVo(pet);
                log.info("成功查询到宠物详情: {}", petVo);
                return petVo;
            }
            
            log.warn("未找到ID为{}的宠物", petId);
            return null;
            
        } catch (Exception e) {
            log.error("查询宠物详情失败，宠物ID: {}", petId, e);
            throw new RuntimeException("查询宠物详情失败", e);
        }
    }


}

