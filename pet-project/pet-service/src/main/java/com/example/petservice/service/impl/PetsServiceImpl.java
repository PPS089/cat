package com.example.petservice.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petcommon.context.UserContext;
import com.example.petcommon.error.ErrorCode;
import com.example.petcommon.exception.BizException;
import com.example.petpojo.dto.PetUpdateDto;
import com.example.petpojo.entity.Adoptions;
import com.example.petpojo.entity.Pets;
import com.example.petpojo.entity.enums.CommonEnum;
import com.example.petpojo.vo.AdoptionResultVo;
import com.example.petpojo.vo.AdoptionsVo;
import com.example.petpojo.vo.PetListVo;
import com.example.petservice.mapper.PetsMapper;
import com.example.petservice.service.AdoptionsService;
import com.example.petservice.service.PetsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 宠物服务实现类
 * 实现宠物相关的业务逻辑
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PetsServiceImpl extends ServiceImpl<PetsMapper, Pets> implements PetsService {

    private final AdoptionsService adoptionsService;


    /**
     * 开始领养，修改宠物领养状态
     */
    @Override
    @Transactional
    public AdoptionResultVo adop(Long petId) {
        // 参数校验
        validatePetId(petId);

        Long userId = UserContext.getCurrentUserId();
        log.info("用户ID: {} 尝试领养宠物ID: {}", userId, petId);

        // 查询宠物信息
        Pets pet = this.getById(petId);
        if (pet == null) {
            throw new BizException(ErrorCode.PET_NOT_FOUND, "宠物不存在，ID: " + petId);
        }

        // 检查宠物状态
        validatePetForAdoption(pet, petId);

        // 更新宠物状态
        pet.setStatus(CommonEnum.PetStatusEnum.ADOPTED);
        this.updateById(pet);

        // 创建领养记录
        AdoptionsVo adoptionsVo = adoptionsService.createAdoption(petId.intValue());
        if (adoptionsVo == null) {
            throw new RuntimeException("创建领养记录失败");
        }
        
        log.info("宠物领养成功，宠物ID: {}，用户ID: {}", petId, userId);
        
        // 构建并返回结果
        AdoptionResultVo result = new AdoptionResultVo();
        result.setPetId(pet.getPid().longValue());
        result.setPetName(pet.getName());
        result.setSpecies(pet.getSpecies());
        result.setBreed(pet.getBreed());
        result.setStatus(pet.getStatus() != null ? pet.getStatus().getCode() : null);
        result.setAdoptionDate(LocalDateTime.now());
        result.setMessage("领养成功");
        return result;
    }
    
    /**
     * 验证宠物是否可被领养
     */
    private void validatePetForAdoption(Pets pet, Long petId) {
        // 检查宠物是否已被领养
        if (CommonEnum.PetStatusEnum.ADOPTED.equals(pet.getStatus())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "该宠物已被领养，无法重复领养");
        }

        // 检查是否已有领养记录
        LambdaQueryWrapper<Adoptions> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Adoptions::getPid, petId.intValue());
        Adoptions existingAdoption = adoptionsService.getOne(queryWrapper);
        if (existingAdoption != null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "该宠物已有领养记录，无法重复领养");
       }
    }
    
    /**
     *验证宠物ID
     * @param petId 宠物ID
     */
    private void validatePetId(Long petId) {
        if (petId == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "宠物ID不能为空");
        }
    }


    /**
     *更新宠物信息
     */
    @Override
    public PetListVo update(PetUpdateDto petUpdateDto) {
        Pets pet = lambdaQuery()
                .eq(Pets::getPid, petUpdateDto.getPid())
                .one();          // 取不到返回 null
                
        if (pet == null) {
            log.warn("宠物不存在，pid: {}", petUpdateDto.getPid());
            throw new BizException(ErrorCode.PET_NOT_FOUND, "宠物不存在，ID: " + petUpdateDto.getPid());
        }

        // 直接复制属性
        BeanUtils.copyProperties(petUpdateDto, pet);
        this.updateById(pet);
        
        // 转换为PetListVo
        PetListVo petListVo = new PetListVo();
        petListVo.setPid(pet.getPid());
        petListVo.setName(pet.getName());
        petListVo.setSpecies(pet.getSpecies());
        petListVo.setBreed(pet.getBreed());
        petListVo.setAge(pet.getAge());
        petListVo.setGender(pet.getGender());
        petListVo.setImgUrl(pet.getImageUrl());
        petListVo.setStatus(pet.getStatus() != null ? pet.getStatus().getCode() : null);
        
        return petListVo;
    }


}