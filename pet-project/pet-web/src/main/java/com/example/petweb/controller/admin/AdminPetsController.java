package com.example.petweb.controller.admin;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.petcommon.result.Result;
import com.example.petpojo.dto.PetCreateDto;
import com.example.petpojo.dto.PetUpdateDto;
import com.example.petpojo.vo.PetListVo;
import com.example.petservice.service.PetsService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import lombok.RequiredArgsConstructor;

/**
 * @author 33185
 */
@RestController
@RequestMapping("/admin/pets")
@Tag(name = "管理员-宠物管理")
@Validated
@RequiredArgsConstructor
public class AdminPetsController {

    private final PetsService petsService;

    @PostMapping
    public Result<PetListVo> create(@Valid @RequestBody PetCreateDto dto) {
        return Result.success(petsService.createPet(dto));
    }

    @PutMapping("/{id}")
    public Result<PetListVo> update(@PathVariable @Positive(message = "宠物ID必须为正数") Integer id, @Valid @RequestBody PetUpdateDto dto) {
        dto.setPid(id.longValue());
        return Result.success(petsService.update(dto));
    }

    @PutMapping("/{id}/status")
    public Result<PetListVo> updateStatus(
            @PathVariable @Positive(message = "宠物ID必须为正数") Integer id,
            @RequestParam @NotBlank(message = "状态不能为空") @Size(max = 20, message = "状态长度不能超过20字符") String status) {
        return Result.success(petsService.updatePetStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable @Positive(message = "宠物ID必须为正数") Integer id) {
        petsService.deletePet(id);
        return Result.success("删除成功");
    }

    @GetMapping
    public Result<Object> list(
            @RequestParam(value = "status", required = false) @Size(max = 20, message = "状态长度不能超过20字符") String status,
            @RequestParam(value = "current_page", defaultValue = "1") @Min(value = 1, message = "current_page 必须>=1") Integer currentPage,
            @RequestParam(value = "per_page", defaultValue = "10") @Min(value = 1, message = "per_page 必须>=1") @Max(value = 100, message = "per_page 不能超过100") Integer pageSize) {
        return Result.success(petsService.listPetsForAdmin(currentPage, pageSize, status));
    }
}
