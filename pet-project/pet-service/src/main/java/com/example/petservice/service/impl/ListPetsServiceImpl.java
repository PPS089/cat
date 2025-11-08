package com.example.petservice.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.petpojo.vo.PetListVo;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petpojo.entity.Pets;
import com.example.petservice.mapper.ListPetsMapper;
import com.example.petservice.service.ListPetsService;

import lombok.extern.slf4j.Slf4j;

/**
 * 宠物列表服务实现类
 * 实现宠物列表相关的业务逻辑
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ListPetsServiceImpl extends ServiceImpl<ListPetsMapper, Pets> implements ListPetsService {
   
    private final ListPetsMapper listPetsMapper;

    /**
     * 分页查询宠物列表
     * @param currentPage 当前页码
     * @param pageSize 每页数量
     * @return 宠物列表分页对象
     */
    @SneakyThrows
    @Override
    public IPage<PetListVo> listPets(@Param("current_page") Integer currentPage, @Param("per_page") Integer pageSize) {
        // 确保分页参数有效
        int current = (currentPage != null && currentPage > 0) ? currentPage : 1;
        int size = (pageSize != null && pageSize > 0) ? pageSize : 10;
        
        Page<PetListVo> page = new Page<>(current, size);
        
        try {
            // 直接使用Mapper查询返回VO对象，无需转换
            return listPetsMapper.selectPetListByPage(page);
        } catch (Exception e) {
            log.error("查询宠物列表失败", e);
            throw new RuntimeException("查询宠物列表失败", e);
        }
    }

    /**
     * 根据ID查询宠物详情
     * @param petId 宠物ID
     * @return 宠物详情VO对象
     */
    @Override
    public PetListVo getPetById(Integer petId) {
        log.info("根据ID查询宠物详情，宠物ID: {}", petId);
        
        try {
            // 直接使用Mapper查询返回VO对象
            PetListVo petVo = listPetsMapper.selectPetListById(petId);
            
            log.info("查询结果: petVo = {}", petVo);
            
            return petVo;
        } catch (Exception e) {
            log.error("查询宠物详情失败，宠物ID: {}", petId, e);
            throw new RuntimeException("查询宠物详情失败", e);
        }
    }
}