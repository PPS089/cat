package com.example.petservice.service.impl;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petcommon.context.UserContext;
import com.example.petpojo.dto.PetUpdateDto;
import com.example.petpojo.entity.Adoptions;
import com.example.petpojo.entity.Pets;
import com.example.petpojo.entity.enums.CommonEnum;
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
     * 获取用户已领养的宠物列表
     */
    @Override
    public List<PetListVo> getUserAdoptedPets() {
        Long userId = UserContext.getCurrentUserId();
        log.info("获取用户已领养的宠物列表，用户ID: {}", userId);
        
        // 查询用户领养记录
        LambdaQueryWrapper<Adoptions> adoptionQuery = new LambdaQueryWrapper<>();
        adoptionQuery.eq(Adoptions::getUid, userId.intValue());
        List<Adoptions> adoptions = adoptionsService.list(adoptionQuery);
        
        if (adoptions.isEmpty()) {
            log.info("用户没有领养记录，用户ID: {}", userId);
            return List.of();
        }
        
        // 获取领养的宠物ID列表
        List<Integer> petIds = adoptions.stream()
                .map(Adoptions::getPid)
                .collect(java.util.stream.Collectors.toList());
        
        // 查询对应的宠物信息
        LambdaQueryWrapper<Pets> petQuery = new LambdaQueryWrapper<>();
        petQuery.in(Pets::getPid, petIds);
        List<Pets> pets = this.list(petQuery);
        
        // 转换为PetListVo
        List<PetListVo> petListVos = pets.stream()
                .map(pet -> {
                    PetListVo vo = new PetListVo();
                    vo.setPid(pet.getPid());
                    vo.setName(pet.getName());
                    vo.setSpecies(pet.getSpecies());
                    vo.setBreed(pet.getBreed());
                    vo.setAge(pet.getAge());
                    vo.setGender(pet.getGender());
                    vo.setImgUrl(pet.getImageUrl());
                    vo.setStatus(pet.getStatus() != null ? pet.getStatus().getCode() : null);
                    // 注意：这里没有设置收容所信息，因为Pets实体不包含这些信息
                    return vo;
                })
                .collect(java.util.stream.Collectors.toList());
        
        log.info("获取到用户已领养宠物数量: {}，用户ID: {}", petListVos.size(), userId);
        return petListVos;
    }



    /**
     *  开始领养，修改宠物领养状态
     */

    @Override
    @Transactional
    public PetListVo adop(Long petId) {

        Long userId = UserContext.getCurrentUserId();

        if (petId == null) {
            log.error("宠物ID为空，无法执行领养操作");
            throw new RuntimeException("宠物ID为空，无法执行领养操作");
        }

        Pets pet = this.getById(petId);
        if (pet == null) {
            log.error("宠物不存在，ID: {}", petId);
            throw new IllegalArgumentException("宠物不存在，ID: " + petId);
        }

        // Check if pet is already adopted
        if (CommonEnum.PetStatusEnum.ADOPTED.equals(pet.getStatus())) {
            log.error("宠物已被领养，无法重复领养，宠物ID: {}", petId);
            throw new IllegalStateException("该宠物已被领养，无法重复领养");
        }

        // Check if adoption record already exists
        LambdaQueryWrapper<Adoptions> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Adoptions::getPid, petId.intValue());
        Adoptions existingAdoption = adoptionsService.getOne(queryWrapper);
        if (existingAdoption != null) {
            log.error("领养记录已存在，无法重复领养，宠物ID: {}", petId);
            throw new IllegalStateException("该宠物已有领养记录，无法重复领养");
        }

        pet.setStatus(CommonEnum.PetStatusEnum.ADOPTED);
        boolean petUpdated = this.updateById(pet);
        if (!petUpdated) {
            log.error("更新宠物状态失败，宠物ID: {}", petId);
            throw new RuntimeException("更新宠物状态失败");
        }
        
/**
 *  添加领养记录
 */

        AdoptionsVo adoptionsVo = adoptionsService.createAdoption(petId.intValue());
        // 我们只需要检查领养记录是否创建成功，不需要使用返回的VO对象
        // 如果adoptionsVo为null，说明创建失败
        if (adoptionsVo == null) {
            log.error("创建领养记录失败，宠物ID: {}", petId);
            throw new RuntimeException("创建领养记录失败");
        }
        
        log.info("宠物领养成功，宠物ID: {}，用户ID: {}", petId, userId);
        
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
        // 注意：这里没有设置收容所信息，因为Pets实体不包含这些信息
        
        return petListVo;
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
            throw new IllegalArgumentException("宠物不存在，ID: " + petUpdateDto.getPid());
        }

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
        // 注意：这里没有设置收容所信息，因为Pets实体不包含这些信息
        
        return petListVo;
    }


    /**
     * 获取已领养宠物的名称列表
     */
    @Override
    public List<String> getAdoptedPetsName() {
        Long userId = UserContext.getCurrentUserId();
        log.info("获取用户已领养宠物的名称列表，用户ID: {}", userId);
        
        // 查询用户领养记录
        LambdaQueryWrapper<Adoptions> adoptionQuery = new LambdaQueryWrapper<>();
        adoptionQuery.eq(Adoptions::getUid, userId.intValue());
        List<Adoptions> adoptions = adoptionsService.list(adoptionQuery);
        
        if (adoptions.isEmpty()) {
            log.info("用户没有领养记录，用户ID: {}", userId);
            return List.of();
        }
        
        // 获取领养的宠物ID列表
        List<Integer> petIds = adoptions.stream()
                .map(Adoptions::getPid)
                .collect(java.util.stream.Collectors.toList());
        
        // 查询对应的宠物名称
        LambdaQueryWrapper<Pets> petQuery = new LambdaQueryWrapper<>();
        petQuery.in(Pets::getPid, petIds);
        List<Pets> pets = this.list(petQuery);
        
        List<String> petNames = pets.stream()
                .map(Pets::getName)
                .collect(java.util.stream.Collectors.toList());
        
        log.info("获取到用户已领养宠物名称数量: {}，用户ID: {}", petNames.size(), userId);
        return petNames;
    }
}