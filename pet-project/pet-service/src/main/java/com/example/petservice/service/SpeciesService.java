package com.example.petservice.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.petpojo.dto.SpeciesCreateDto;
import com.example.petpojo.dto.SpeciesUpdateDto;
import com.example.petpojo.entity.Species;
import com.example.petpojo.vo.SpeciesVo;

public interface SpeciesService extends IService<Species> {

    List<SpeciesVo> listAllSpecies();

    SpeciesVo createSpecies(SpeciesCreateDto dto);

    SpeciesVo updateSpecies(Integer id, SpeciesUpdateDto dto);
}

