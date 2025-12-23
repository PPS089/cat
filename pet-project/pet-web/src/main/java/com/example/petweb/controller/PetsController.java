package com.example.petweb.controller;

import java.time.LocalDateTime;

import com.example.petpojo.vo.AdoptionResultVo;
import com.example.petpojo.vo.FosterEndResultVo;
import com.example.petpojo.vo.AdoptionTimelineResponse;
import com.example.petpojo.dto.FosterRequest;
import com.example.petpojo.dto.PetUpdateDto;
import com.example.petpojo.vo.FostersVo;
import com.example.petservice.service.AdoptionsService;
import com.example.petservice.service.FosterService;
import com.example.petservice.service.ListPetsService;
import com.example.petpojo.vo.PetListVo;
import com.example.petpojo.vo.PetsDetailsVo;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.petcommon.context.UserContext;
import com.example.petcommon.result.Result;

import com.example.petservice.service.PetsService;
import com.example.petcommon.exception.BizException;
import com.example.petcommon.error.ErrorCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 宠物管理控制器
 * 提供宠物相关的 REST API 接口
 */
@RestController
@RequestMapping("/pets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "宠物管理", description = "宠物相关的 API 接口")
@SecurityRequirement(name = "bearer-key")
@Validated
public class PetsController {

    private final PetsService petsService;
    private final FosterService fosterService;
    private final AdoptionsService adoptionsService;
    private final ListPetsService listPetsService;

    /**
     * 领养宠物
     */
    @PostMapping("/adopt")
    @Operation(summary = "领养宠物", description = "领养指定ID的宠物")
    public Result<AdoptionResultVo> adoptPet(
            @RequestParam("petId") @NotNull(message = "宠物ID不能为空") @Positive(message = "宠物ID必须为正数") Long petId) {
        log.info("领养宠物ID: {}", petId);
        AdoptionResultVo result = petsService.adop(petId);
        return Result.success(result);
    }

    /**
     * 寄养宠物
     */
    @PostMapping("/{petId}/foster")
    @Operation(summary = "寄养宠物", description = "将宠物寄养到指定的收容所")
    public Result<FostersVo> fosterPet(
            @PathVariable @NotNull(message = "宠物ID不能为空") @Positive(message = "宠物ID必须为正数") Long petId,
            @Valid @RequestBody FosterRequest request) {
        log.info("寄养宠物ID: {}, 收容所ID: {}, 开始日期: {}", petId, request.getShelterId(), request.getStartDate());
        FostersVo fostered = fosterService.createFoster(petId, request.getShelterId(), request.getStartDate());
        if (fostered == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "寄养失败");
        }
        return Result.success(fostered);
    }


    /**
     * 结束宠物寄养
     */
    @PostMapping("/{petId}/foster/end")
    @Operation(summary = "结束宠物寄养", description = "结束指定宠物的寄养状态")
    public Result<FosterEndResultVo> endPetFoster(@PathVariable @NotNull(message = "宠物ID不能为空") @Positive(message = "宠物ID必须为正数") Long petId) {
        log.info("结束宠物寄养ID: {}", petId);
        boolean ended = fosterService.endFosterByPetId(petId);
        if (!ended) {
            throw new BizException(ErrorCode.BAD_REQUEST, "结束寄养失败");
        }
        FosterEndResultVo result = new FosterEndResultVo();
        result.setPetId(petId);
        result.setEndDate(LocalDateTime.now());
        return Result.success(result);
    }

    /**
     * 获取宠物领养时间线
     */
    @GetMapping("/{petId}/adoption-timeline")
    @Operation(summary = "获取宠物领养时间线", description = "获取指定宠物的领养过程时间线")
    public Result<AdoptionTimelineResponse> getAdoptionTimeline(@PathVariable @NotNull(message = "宠物ID不能为空") @Positive(message = "宠物ID必须为正数") Long petId) {
        log.info("获取宠物领养时间线，宠物ID: {}", petId);
        Long userId = UserContext.getCurrentUserId();
        AdoptionTimelineResponse timeline = adoptionsService.getAdoptionTimeline(petId.intValue(), userId);
        return Result.success(timeline);
    }

    /**
     * 更新宠物信息
     */
    @PutMapping("/{petId}")
    @Operation(summary = "更新宠物信息", description = "更新指定宠物的信息")
    public Result<PetListVo> updatePet(
            @PathVariable @NotNull(message = "宠物ID不能为空") @Positive(message = "宠物ID必须为正数") Long petId,
            @Valid @RequestBody PetUpdateDto petUpdateDto) {
        log.info("更新宠物信息，宠物ID: {}, 更新数据: {}", petId, petUpdateDto);
        
        petUpdateDto.setPid(petId);
        PetListVo updatedPet = petsService.update(petUpdateDto);
        return Result.success(updatedPet);
    }

    /**
     * 领养界面根据ID获取宠物详情
     */
    @GetMapping("/details/{id}")
    @Operation(summary = "根据ID获取宠物详情", description = "根据宠物ID获取宠物的详细信息")
    // 根据ID获取宠物详情
    public Result<PetsDetailsVo> getPetById(@PathVariable @NotNull(message = "宠物ID不能为空") @Positive(message = "宠物ID必须为正数") Long id) {
        log.info("根据ID获取宠物详情: {}", id);
        
        PetsDetailsVo pet = listPetsService.getPetById(id.intValue());
        if (pet == null) {
            throw new BizException(ErrorCode.PET_NOT_FOUND);
        }
        return Result.success(pet);
    }
}
