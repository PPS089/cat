package com.example.petweb.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.petcommon.result.Result;
import com.example.petpojo.vo.BreedVo;
import com.example.petpojo.vo.SpeciesVo;
import com.example.petservice.service.BreedService;
import com.example.petservice.service.ListPetsService;
import com.example.petservice.service.SpeciesService;
import com.example.petpojo.vo.PetListVo; 
import com.example.petpojo.vo.PetsDetailsVo;

import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;


/**
 * 宠物信息控制器
 * 提供宠物信息相关的 REST API 接口
 * @author 33185
 */
@RestController
@RequestMapping("/pets/info")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "宠物信息", description = "宠物信息相关的 API 接口")
@SecurityRequirement(name = "bearer-key")
@Validated
public class MyPetsController {

    private final ListPetsService listPetsService;
    private final SpeciesService speciesService;
    private final BreedService breedService;

    /**
     * 分页查询宠物列表
     * @param currentPage 当前页码
     * @param pageSize 每页数量
     * @return 宠物列表
     */
    @GetMapping("/available")
    @Operation(summary = "分页查询宠物列表", description = "分页查询可领养的宠物列表")
    public Result<IPage<PetListVo>> listPets(
            @RequestParam("current_page") @Min(value = 1, message = "current_page 必须>=1") Integer currentPage,
            @RequestParam("per_page") @Min(value = 1, message = "per_page 必须>=1") @Max(value = 100, message = "per_page 不能超过100") Integer pageSize,
            @RequestParam(value = "species", required = false) String species,
            @RequestParam(value = "breed", required = false) String breed,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "min_age", required = false) Integer minAge,
            @RequestParam(value = "max_age", required = false) Integer maxAge) {
        IPage<PetListVo> petsPage = listPetsService.listPets(currentPage, pageSize, species, breed, gender, minAge, maxAge);
        return Result.success(petsPage);

    }

    /**
     * 我的宠物界面根据ID获取宠物详情，用于编辑
     * @param petId 宠物ID
     * @return 宠物详情
     */
    @GetMapping("/available/{petId}")
    @Operation(summary = "根据ID获取宠物详情", description = "根据宠物ID获取可领养宠物的详细信息")
    public Result<PetsDetailsVo> getPetById(
            @PathVariable @Positive(message = "宠物ID必须为正数") Integer petId) {
        log.info("获取宠物详情，宠物ID: {}", petId);
        PetsDetailsVo pet = listPetsService.getPetById(petId);
        return Result.success(pet);
    }

    /**
     * 领养中心筛选：获取物种列表
     */
    @GetMapping("/species")
    @Operation(summary = "获取物种列表", description = "用于领养中心筛选下拉框")
    public Result<List<SpeciesVo>> listSpecies() {
        return Result.success(speciesService.listAllSpecies());
    }

    /**
     * 领养中心筛选：根据物种获取品种列表
     */
    @GetMapping("/species/{speciesId}/breeds")
    @Operation(summary = "获取品种列表", description = "用于领养中心筛选下拉框")
    public Result<List<BreedVo>> listBreedsBySpecies(
            @PathVariable @Positive(message = "物种ID必须为正数") Integer speciesId) {
        return Result.success(breedService.listBreedsBySpecies(speciesId));
    }

}
