package com.example.petservice.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.petpojo.dto.BreedCreateDto;
import com.example.petpojo.dto.BreedUpdateDto;
import com.example.petpojo.entity.Breed;
import com.example.petpojo.vo.BreedVo;

public interface BreedService extends IService<Breed> {

    List<BreedVo> listBreedsBySpecies(Integer speciesId);

    BreedVo createBreed(Integer speciesId, BreedCreateDto dto);

    BreedVo updateBreed(Integer speciesId, Integer breedId, BreedUpdateDto dto);
}

