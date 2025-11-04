package com.example.petweb.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.example.petpojo.dto.AdoptionTimelineResponse;
import com.example.petpojo.dto.FosterRequest;
import com.example.petpojo.dto.PetUpdateDto;
import com.example.petpojo.entity.Fosters;
import com.example.petpojo.entity.Pets;
import com.example.petservice.service.AdoptionsService;
import com.example.petservice.service.FosterService;
import com.example.petservice.service.ListPetsService;
import com.example.petpojo.vo.ListPetsVo;
import com.example.petpojo.vo.PetsDetailsVo;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
public class PetsController {

    private final PetsService petsService;
    private final FosterService fosterService;
    private final AdoptionsService adoptionsService;
    private final ListPetsService listPetsService;


    /**
     * listPetsVo 转换为 PetsDetailsVo
     */

    private PetsDetailsVo convertToPetVO(ListPetsVo listPetsVo) { 
          PetsDetailsVo pet = new PetsDetailsVo();
            pet.setId(listPetsVo.getPid());  // 使用pid作为id
            pet.setPid(listPetsVo.getPid()); // 使用pid作为pid
            pet.setName(listPetsVo.getName());
            // 使用从数据库查询到的实际物种信息，而不是硬编码为"adoption"
            pet.setSpecies(listPetsVo.getSpecies() != null ? listPetsVo.getSpecies() : "未知物种");
            pet.setBreed(listPetsVo.getBreed());
            pet.setAge(listPetsVo.getAge());
            pet.setGender(listPetsVo.getGender());
            pet.setStatus(listPetsVo.getStatus());
            pet.setDescription(listPetsVo.getName() + "是一只可爱的" + listPetsVo.getBreed() + "，正在寻找温暖的家庭");
            pet.setImage(listPetsVo.getImgUrl() != null ? listPetsVo.getImgUrl() : "/dog.jpg");
            pet.setHealthStatus("健康");
            pet.setVaccinated(true);
            pet.setSpayed(false);
            pet.setAdoptionFee(200);
            pet.setFosterFee(50);
            pet.setShelterName(listPetsVo.getShelterName());
            pet.setShelterAddress(listPetsVo.getShelterAddress());
            pet.setShelterId(1L);

            return pet;
    }

    

    /**
     * 领养宠物
     */
    @PostMapping("/adopt")
    @Operation(summary = "领养宠物", description = "用户领养指定的宠物")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "领养成功"),
            @ApiResponse(responseCode = "400", description = "领养失败，宠物可能已被领养或不存在")
    })
    public Result<Pets> adoptPet(@Parameter(description = "宠物ID", required = true) @RequestParam Long petId) {
        log.info("领养宠物ID: {}", petId);
        try {
            Pets adopted = petsService.adop(petId);
            if (adopted != null) {
                return Result.success(adopted);
            } else {
                return Result.error("领养失败");
            }
        } catch (IllegalStateException e) {
            // 处理宠物已被领养的情况
            log.warn("宠物已被领养，宠物ID: {}", petId);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("领养宠物失败，宠物ID: {}", petId, e);
            return Result.error("领养失败: " + e.getMessage());
        }
    }



    /**
     * 寄养宠物
     */
    @PostMapping("/{petId}/foster")
    @Operation(summary = "寄养宠物", description = "将宠物寄养到指定的收容所")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "寄养成功"),
            @ApiResponse(responseCode = "400", description = "寄养失败")
    })
    public Result<Fosters> fosterPet(
            @Parameter(description = "宠物ID", required = true) @PathVariable Long petId, 
            @Parameter(description = "寄养请求信息", required = true) @RequestBody FosterRequest request) {
        log.info("寄养宠物ID: {}, 收容所ID: {}, 开始日期: {}", petId, request.getShelterId(), request.getStartDate());
        Fosters fostered = fosterService.createFoster(petId, request.getShelterId(), request.getStartDate());
        if (fostered != null) {
            return Result.success(fostered);
        } else {
            return Result.error("寄养失败");
        }
    }



     /**
     * 删除宠物寄养记录
     */
    @DeleteMapping("delete/{id}")
    @Operation(summary = "删除宠物寄养记录", description = "根据ID删除宠物的寄养记录")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "删除失败")
    })
    public Result<String> deletePetFoster(@Parameter(description = "寄养记录ID", required = true) @PathVariable Long id) {
        log.info("删除宠物寄养记录ID: {}", id);
        boolean deleted = fosterService.deleteFoster(id);
        if (deleted) {
            return Result.success("宠物寄养记录删除成功");
        } else {
            return Result.error("宠物寄养记录删除失败");
        }
    }

    /**
     * 结束宠物寄养
     */
    @PostMapping("/{petId}/foster/end")
    @Operation(summary = "结束宠物寄养", description = "结束指定宠物的寄养状态")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "结束寄养成功"),
            @ApiResponse(responseCode = "400", description = "结束寄养失败")
    })
    public Result<Map<String, Object>> endPetFoster(@Parameter(description = "宠物ID", required = true) @PathVariable Long petId) {
        log.info("结束宠物寄养ID: {}", petId);
        boolean ended = fosterService.endFosterByPetId(petId);
        if (ended) {
            Map<String, Object> data = new HashMap<>();
            data.put("end_date", LocalDateTime.now());
            data.put("pet_id", petId);
            return Result.success(data);
        } else {
            return Result.error("结束寄养失败");
        }
    }



    /**
     * 获取宠物领养时间线
     */
    @GetMapping("/{petId}/adoption-timeline")
    @Operation(summary = "获取宠物领养时间线", description = "获取指定宠物的领养过程时间线")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "400", description = "获取失败")
    })
    public Result<AdoptionTimelineResponse> getAdoptionTimeline(@Parameter(description = "宠物ID", required = true) @PathVariable Long petId) {
        log.info("获取宠物领养时间线，宠物ID: {}", petId);
        try {
            Long userId = UserContext.getCurrentUserId();
            AdoptionTimelineResponse timeline = adoptionsService.getAdoptionTimeline(petId.intValue(), userId);
            return Result.success(timeline);
        } catch (Exception e) {
            log.error("获取宠物领养时间线失败", e);
            return Result.error("获取宠物领养时间线失败: " + e.getMessage());
        }
    }

    /**
     * 更新宠物信息
     */
    @PutMapping("/{petId}")
    @Operation(summary = "更新宠物信息", description = "更新指定宠物的信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "更新失败，宠物不存在或参数错误")
    })
    public Result<Pets> updatePet(
            @Parameter(description = "宠物ID", required = true) @PathVariable Long petId, 
            @Parameter(description = "宠物更新信息", required = true) @RequestBody PetUpdateDto petUpdateDto) {
        log.info("更新宠物信息，宠物ID: {}, 更新数据: {}", petId, petUpdateDto);
        
        try {
            // 设置宠物ID
            petUpdateDto.setPid(petId);
            
            // 调用服务层更新宠物
            Pets updatedPet = petsService.update(petUpdateDto);
            
            log.info("宠物信息更新成功，宠物ID: {}", petId);
            return Result.success(updatedPet);
        } catch (IllegalArgumentException e) {
            log.error("宠物不存在，宠物ID: {}", petId);
            return Result.error("宠物不存在: " + e.getMessage());
        } catch (Exception e) {
            log.error("更新宠物信息失败，宠物ID: {}", petId, e);
            return Result.error("更新宠物信息失败: " + e.getMessage());
        }
    }



    /**
     * 领养界面根据ID获取宠物详情
     */
    @GetMapping("/details/{id}")
    // 根据ID获取宠物详情
    public Result<PetsDetailsVo> getPetById(@PathVariable Long id) {
        log.info("根据ID获取宠物详情: {}", id);
        
        try {
            log.info("开始查询宠物详情，宠物ID: {}", id);
            // 使用ListPetsService从数据库查询宠物详情
            ListPetsVo listPetsVo = listPetsService.getPetById(id.intValue());
            
            if (listPetsVo == null) {
                log.warn("未找到ID为{}的宠物", id);
                return Result.error("宠物不存在");
            }
            
            log.info("成功查询到宠物数据: {}", listPetsVo);
            
            PetsDetailsVo pet = convertToPetVO(listPetsVo);
            
            log.info("成功查询到宠物详情: {}", pet);
            return Result.success(pet);
            
        } catch (Exception e) {
            log.error("查询宠物详情失败，宠物ID: {}", id, e);
            return Result.error("查询宠物详情失败");
        }
    }





}