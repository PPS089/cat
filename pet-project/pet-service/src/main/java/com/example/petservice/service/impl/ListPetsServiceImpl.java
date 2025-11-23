package com.example.petservice.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.petpojo.dto.PetQueryDto;
import com.example.petpojo.vo.PetListVo;
import com.example.petpojo.vo.PetsDetailsVo;

import lombok.RequiredArgsConstructor;
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
     * @param queryDto 查询参数
     * @return 宠物列表分页对象
     */
    @Override
    @Transactional(readOnly = true)
    public IPage<PetListVo> listPets(PetQueryDto queryDto) {
        // 确保分页参数有效
        int current = (queryDto.getCurrentPage() != null && queryDto.getCurrentPage() > 0) ? queryDto.getCurrentPage() : 1;
        int size = (queryDto.getPageSize() != null && queryDto.getPageSize() > 0) ? queryDto.getPageSize() : 10;
        
        Page<PetListVo> page = new Page<>(current, size);
        
        try {
            // 直接使用Mapper查询返回VO对象，无需转换
            return listPetsMapper.selectPetListByPage(page, queryDto);
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