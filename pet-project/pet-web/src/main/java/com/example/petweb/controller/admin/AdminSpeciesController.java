package com.example.petweb.controller.admin;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.petcommon.result.Result;
import com.example.petpojo.dto.BreedCreateDto;
import com.example.petpojo.dto.BreedUpdateDto;
import com.example.petpojo.dto.SpeciesCreateDto;
import com.example.petpojo.dto.SpeciesUpdateDto;
import com.example.petpojo.vo.BreedVo;
import com.example.petpojo.vo.SpeciesVo;
import com.example.petservice.service.BreedService;
import com.example.petservice.service.SpeciesService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/species")
@Tag(name = "管理员-物种品种管理")
@Validated
@RequiredArgsConstructor
public class AdminSpeciesController {

    private final SpeciesService speciesService;
    private final BreedService breedService;

    @GetMapping
    public Result<List<SpeciesVo>> listSpecies() {
        return Result.success(speciesService.listAllSpecies());
    }

    @PostMapping
    public Result<SpeciesVo> createSpecies(@Valid @RequestBody SpeciesCreateDto dto) {
        return Result.success(speciesService.createSpecies(dto));
    }

    @PutMapping("/{id}")
    public Result<SpeciesVo> updateSpecies(
            @PathVariable @Positive(message = "物种ID必须为正数") Integer id,
            @Valid @RequestBody SpeciesUpdateDto dto) {
        return Result.success(speciesService.updateSpecies(id, dto));
    }

    @GetMapping("/{speciesId}/breeds")
    public Result<List<BreedVo>> listBreeds(@PathVariable @Positive(message = "物种ID必须为正数") Integer speciesId) {
        return Result.success(breedService.listBreedsBySpecies(speciesId));
    }

    @PostMapping("/{speciesId}/breeds")
    public Result<BreedVo> createBreed(
            @PathVariable @Positive(message = "物种ID必须为正数") Integer speciesId,
            @Valid @RequestBody BreedCreateDto dto) {
        return Result.success(breedService.createBreed(speciesId, dto));
    }

    @PutMapping("/{speciesId}/breeds/{breedId}")
    public Result<BreedVo> updateBreed(
            @PathVariable @Positive(message = "物种ID必须为正数") Integer speciesId,
            @PathVariable @Positive(message = "品种ID必须为正数") Integer breedId,
            @Valid @RequestBody BreedUpdateDto dto) {
        return Result.success(breedService.updateBreed(speciesId, breedId, dto));
    }
}

