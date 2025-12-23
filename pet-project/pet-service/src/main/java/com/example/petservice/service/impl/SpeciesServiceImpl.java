package com.example.petservice.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petcommon.error.ErrorCode;
import com.example.petcommon.exception.BizException;
import com.example.petpojo.dto.SpeciesCreateDto;
import com.example.petpojo.dto.SpeciesUpdateDto;
import com.example.petpojo.entity.Species;
import com.example.petpojo.vo.SpeciesVo;
import com.example.petservice.mapper.SpeciesMapper;
import com.example.petservice.service.SpeciesService;

@Service
public class SpeciesServiceImpl extends ServiceImpl<SpeciesMapper, Species> implements SpeciesService {

    @Override
    @Transactional(readOnly = true)
    public List<SpeciesVo> listAllSpecies() {
        return lambdaQuery()
                .orderByAsc(Species::getId)
                .list()
                .stream()
                .map(s -> new SpeciesVo(s.getId(), s.getName(), s.getDescription()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SpeciesVo createSpecies(SpeciesCreateDto dto) {
        String name = normalizeName(dto.getName());
        if (existsByName(name, null)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "物种已存在");
        }
        Species entity = Species.builder()
                .name(name)
                .description(normalizeDesc(dto.getDescription()))
                .build();
        if (!this.save(entity)) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "创建物种失败");
        }
        return new SpeciesVo(entity.getId(), entity.getName(), entity.getDescription());
    }

    @Override
    @Transactional
    public SpeciesVo updateSpecies(Integer id, SpeciesUpdateDto dto) {
        Species existing = this.getById(id);
        if (existing == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "物种不存在");
        }
        String name = normalizeName(dto.getName());
        if (existsByName(name, id)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "物种名称已被占用");
        }
        existing.setName(name);
        existing.setDescription(normalizeDesc(dto.getDescription()));
        if (!this.updateById(existing)) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "更新物种失败");
        }
        return new SpeciesVo(existing.getId(), existing.getName(), existing.getDescription());
    }

    private boolean existsByName(String name, Integer excludeId) {
        LambdaQueryWrapper<Species> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Species::getName, name);
        if (excludeId != null) {
            wrapper.ne(Species::getId, excludeId);
        }
        return this.count(wrapper) > 0;
    }

    private String normalizeName(String name) {
        String value = name == null ? "" : name.trim();
        if (value.isEmpty()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "物种名称不能为空");
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

