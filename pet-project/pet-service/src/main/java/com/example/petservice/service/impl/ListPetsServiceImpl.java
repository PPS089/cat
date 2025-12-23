package com.example.petservice.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.petpojo.vo.PetListVo;
import com.example.petpojo.vo.PetsDetailsVo;
import com.example.petservice.cache.CacheNames;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petcommon.error.ErrorCode;
import com.example.petcommon.exception.BizException;
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
     * @param species 物种筛选
     * @param breed 品种筛选
     * @param gender 性别筛选
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     * @return 宠物列表分页对象
     */
    @Override
    @Transactional(readOnly = true)
    public IPage<PetListVo> listPets(Integer currentPage, Integer pageSize, String species, String breed, String gender, Integer minAge, Integer maxAge) {
        // 确保分页参数有效
        int current = (currentPage != null && currentPage > 0) ? currentPage : 1;
        int size = (pageSize != null && pageSize > 0) ? pageSize : 10;
        
        Page<PetListVo> page = new Page<>(current, size);
        
        try {
            // 直接使用Mapper查询返回VO对象，无需转换
            return listPetsMapper.selectPetListByPage(page, species, breed, gender, minAge, maxAge);
        } catch (Exception e) {
            log.error("查询宠物列表失败", e);
            throw new BizException(ErrorCode.INTERNAL_ERROR, "查询宠物列表失败");
        }
    }

    /**
     * 根据ID查询宠物详情
     * @param petId 宠物ID
     * @return 宠物详情VO对象
     */
    @Override
    @Cacheable(
            cacheNames = CacheNames.PET_DETAIL,
            key = "T(com.example.petservice.cache.CacheKeys).petId(#petId)",
            sync = true
    )
    @Transactional(readOnly = true)
    public PetsDetailsVo getPetById(Integer petId) {
        log.info("根据ID查询宠物详情，宠物ID: {}", petId);
        
        PetsDetailsVo petDetailsVo = listPetsMapper.selectPetDetailsById(petId);
        
        // 检查宠物是否存在
        if (petDetailsVo == null) {
            log.warn("宠物不存在，宠物ID: {}", petId);
            throw new BizException(ErrorCode.PET_NOT_FOUND);
        }
        
        return petDetailsVo;
    }
}