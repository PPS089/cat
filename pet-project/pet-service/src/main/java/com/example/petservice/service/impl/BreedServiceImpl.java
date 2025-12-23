package com.example.petservice.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petcommon.error.ErrorCode;
import com.example.petcommon.exception.BizException;
import com.example.petpojo.dto.BreedCreateDto;
import com.example.petpojo.dto.BreedUpdateDto;
import com.example.petpojo.entity.Breed;
import com.example.petpojo.entity.Pets;
import com.example.petpojo.entity.Species;
import com.example.petpojo.vo.BreedVo;
import com.example.petservice.mapper.BreedMapper;
import com.example.petservice.mapper.PetsMapper;
import com.example.petservice.service.BreedService;
import com.example.petservice.service.SpeciesService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BreedServiceImpl extends ServiceImpl<BreedMapper, Breed> implements BreedService {

    private final SpeciesService speciesService;
    private final PetsMapper petsMapper;

    @Override
    @Transactional(readOnly = true)
    public List<BreedVo> listBreedsBySpecies(Integer speciesId) {
        ensureSpeciesExists(speciesId);
        return lambdaQuery()
                .eq(Breed::getSpeciesId, speciesId)
                .orderByAsc(Breed::getId)
                .list()
                .stream()
                .map(b -> new BreedVo(b.getId(), b.getSpeciesId(), b.getName(), b.getDescription()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BreedVo createBreed(Integer speciesId, BreedCreateDto dto) {
        ensureSpeciesExists(speciesId);
        String name = normalizeName(dto.getName());
        if (existsBySpeciesAndName(speciesId, name, null)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "该物种下已存在同名品种");
        }
        Breed entity = Breed.builder()
                .speciesId(speciesId)
                .name(name)
                .description(normalizeDesc(dto.getDescription()))
                .build();
        if (!this.save(entity)) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "创建品种失败");
        }
        return new BreedVo(entity.getId(), entity.getSpeciesId(), entity.getName(), entity.getDescription());
    }

    @Override
    @Transactional
    public BreedVo updateBreed(Integer speciesId, Integer breedId, BreedUpdateDto dto) {
        ensureSpeciesExists(speciesId);
        Breed existing = this.getById(breedId);
        if (existing == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "品种不存在");
        }
        if (!speciesId.equals(existing.getSpeciesId())) {
            ensureBreedNotInUse(breedId);
            existing.setSpeciesId(speciesId);
        }
        String name = normalizeName(dto.getName());
        if (existsBySpeciesAndName(speciesId, name, breedId)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "该物种下已存在同名品种");
        }
        existing.setName(name);
        existing.setDescription(normalizeDesc(dto.getDescription()));
        if (!this.updateById(existing)) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "更新品种失败");
        }
        return new BreedVo(existing.getId(), existing.getSpeciesId(), existing.getName(), existing.getDescription());
    }

    private void ensureBreedNotInUse(Integer breedId) {
        Long count = petsMapper.selectCount(new LambdaQueryWrapper<Pets>().eq(Pets::getBreedId, breedId));
        if (count != null && count > 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "该品种已被宠物使用，无法修改所属物种");
        }
    }

    private void ensureSpeciesExists(Integer speciesId) {
        if (speciesId == null || speciesId <= 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "物种ID不能为空");
        }
        Species species = speciesService.getById(speciesId);
        if (species == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "物种不存在");
        }
    }

    private boolean existsBySpeciesAndName(Integer speciesId, String name, Integer excludeId) {
        LambdaQueryWrapper<Breed> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Breed::getSpeciesId, speciesId)
                .eq(Breed::getName, name);
        if (excludeId != null) {
            wrapper.ne(Breed::getId, excludeId);
        }
        return this.count(wrapper) > 0;
    }

    private String normalizeName(String name) {
        String value = name == null ? "" : name.trim();
        if (value.isEmpty()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "品种名称不能为空");
        }
        return value;
    }

    private String normalizeDesc(String desc) {
        if (desc == null) {
            return null;
        }
        String value = desc.trim();
        return value.isEmpty() ? null : value;
    }
}
