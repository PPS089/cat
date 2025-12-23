package com.example.petservice.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petcommon.context.UserContext;
import com.example.petcommon.error.ErrorCode;
import com.example.petcommon.exception.BizException;
import com.example.petpojo.dto.PetCreateDto;
import com.example.petpojo.dto.PetUpdateDto;
import com.example.petpojo.entity.Adoptions;
import com.example.petpojo.entity.Breed;
import com.example.petpojo.entity.Pets;
import com.example.petpojo.entity.Species;
import com.example.petpojo.entity.enums.CommonEnum;
import com.example.petpojo.vo.AdoptionResultVo;
import com.example.petpojo.vo.AdoptionsVo;
import com.example.petpojo.vo.PetListVo;
import com.example.petservice.cache.PetCacheInvalidator;
import com.example.petservice.mapper.BreedMapper;
import com.example.petservice.mapper.PetsMapper;
import com.example.petservice.mapper.SpeciesMapper;
import com.example.petservice.service.AdoptionsService;
import com.example.petservice.service.PetsService;
import com.example.petservice.config.AliyunOSSOperator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 宠物服务实现类
 * 实现宠物相关的业务逻辑
 * @author 33185
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PetsServiceImpl extends ServiceImpl<PetsMapper, Pets> implements PetsService {

    private final PetsMapper petsMapper;
    private final SpeciesMapper speciesMapper;
    private final BreedMapper breedMapper;
    private final AdoptionsService adoptionsService;
    private final AliyunOSSOperator ossOperator;
    private final PetCacheInvalidator petCacheInvalidator;

    /**
     * 开始领养，修改宠物领养状态
     */
    @Override
    @Transactional
    public AdoptionResultVo adop(Long petId) {
        // 参数校验
        validatePetId(petId);

        Long userId = UserContext.getCurrentUserId();
        log.info("用户ID: {} 尝试领养宠物ID: {}", userId, petId);

        // 查询宠物信息
        Pets pet = this.getById(petId);
        if (pet == null) {
            throw new BizException(ErrorCode.PET_NOT_FOUND, "宠物不存在，ID: " + petId);
        }

        // 检查宠物状态
        validatePetForAdoption(pet, petId);

        // 创建领养记录（PENDING，等待管理员审核）
        AdoptionsVo adoptionsVo = adoptionsService.createAdoption(petId.intValue());
        if (adoptionsVo == null) {
            throw new BizException(ErrorCode.ADOPTION_CREATE_FAILED);
        }

        log.info("领养申请提交成功，宠物ID: {}，用户ID: {}", petId, userId);

        // 构建并返回结果
        AdoptionResultVo result = new AdoptionResultVo();
        result.setPetId(pet.getPid().longValue());
        result.setPetName(pet.getName());
        result.setSpecies(resolveSpeciesName(pet.getSpeciesId()));
        result.setBreed(resolveBreedName(pet.getBreedId()));
        result.setStatus(pet.getStatus() != null ? pet.getStatus().getCode() : null);
        result.setAdoptionDate(LocalDateTime.now());
        result.setMessage("领养申请提交成功，等待审核");
        return result;
    }

    /**
     * 验证宠物是否可被领养
     */
    private void validatePetForAdoption(Pets pet, Long petId) {
        if (CommonEnum.PetStatusEnum.ADOPTED.equals(pet.getStatus())
                || CommonEnum.PetStatusEnum.FOSTERING.equals(pet.getStatus())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "该宠物当前不可领养");
        }

        // 仅检查处于 PENDING/APPROVED 的领养，避免历史记录误判；用 count 避免 getOne 多结果异常
        LambdaQueryWrapper<Adoptions> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Adoptions::getPid, petId.intValue())
                .in(Adoptions::getStatus,
                        CommonEnum.AdoptionStatusEnum.PENDING,
                        CommonEnum.AdoptionStatusEnum.APPROVED);
        boolean hasActiveAdoption = adoptionsService.count(queryWrapper) > 0;
        if (hasActiveAdoption) {
            throw new BizException(ErrorCode.BAD_REQUEST, "该宠物已有进行中的或已通过的领养申请");
        }
    }

    /**
     * 验证宠物ID
     */
    private void validatePetId(Long petId) {
        if (petId == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "宠物ID不能为空");
        }
    }

    /**
     * 更新宠物信息
     */
    @Override
    @Transactional
    public PetListVo update(PetUpdateDto petUpdateDto) {
        Pets pet = lambdaQuery()
                .eq(Pets::getPid, petUpdateDto.getPid())
                .one();

        if (pet == null) {
            log.warn("宠物不存在，pid: {}", petUpdateDto.getPid());
            throw new BizException(ErrorCode.PET_NOT_FOUND, "宠物不存在，ID: " + petUpdateDto.getPid());
        }

        if (petUpdateDto.getShelterId() != null && petUpdateDto.getShelterId() <= 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "收容所ID必须为正数");
        }
        // 若未传收容所，则沿用原值，避免被 BeanUtils 复制为 null
        if (petUpdateDto.getShelterId() == null) {
            petUpdateDto.setShelterId(pet.getShelterId());
        }

        petUpdateDto.setGender(normalizeGender(petUpdateDto.getGender()));
        validateImageUrl(petUpdateDto.getImageUrl());
        validateSpeciesBreed(petUpdateDto.getSpeciesId(), petUpdateDto.getBreedId());
        BeanUtils.copyProperties(petUpdateDto, pet);
        this.updateById(pet);

        petCacheInvalidator.evictPetDetail(pet.getPid());
        petCacheInvalidator.evictPetListPages();

        return buildPetListVo(pet);
    }

    @Override
    @Transactional
    public PetListVo createPet(PetCreateDto petCreateDto) {
        validateSpeciesBreed(petCreateDto.getSpeciesId(), petCreateDto.getBreedId());
        Pets pet = new Pets();
        pet.setName(petCreateDto.getName());
        pet.setSpeciesId(petCreateDto.getSpeciesId());
        pet.setBreedId(petCreateDto.getBreedId());
        pet.setAge(petCreateDto.getAge());
        pet.setGender(normalizeGender(petCreateDto.getGender()));
        validateImageUrl(petCreateDto.getImageUrl());
        pet.setImageUrl(petCreateDto.getImageUrl());
        pet.setShelterId(petCreateDto.getShelterId());
        CommonEnum.PetStatusEnum status = parsePetStatus(petCreateDto.getStatus());
        pet.setStatus(status != null ? status : CommonEnum.PetStatusEnum.AVAILABLE);
        this.save(pet);

        petCacheInvalidator.evictPetListPages();
        return buildPetListVo(pet);
    }

    /**
     * 校验图片 URL：禁止 data URL，控制长度
     */
    private void validateImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }
        if (imageUrl.trim().startsWith("data:")) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请先将图片上传至 OSS 并提供外链 URL");
        }
        if (imageUrl.length() > 512) {
            throw new BizException(ErrorCode.BAD_REQUEST, "图片URL长度不能超过512字符");
        }
    }

    @Override
    @Transactional
    public void deletePet(Integer petId) {
        Pets pet = this.getById(petId);
        if (pet == null) {
            throw new BizException(ErrorCode.PET_NOT_FOUND);
        }
        if (CommonEnum.PetStatusEnum.ADOPTED.equals(pet.getStatus())
                || CommonEnum.PetStatusEnum.FOSTERING.equals(pet.getStatus())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "已领养或寄养中的宠物不可删除");
        }
        LambdaQueryWrapper<Adoptions> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Adoptions::getPid, petId)
                .in(Adoptions::getStatus,
                        CommonEnum.AdoptionStatusEnum.PENDING,
                        CommonEnum.AdoptionStatusEnum.APPROVED);
        if (adoptionsService.count(wrapper) > 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "有进行中的领养记录，暂不可删除");
        }
        String imageUrl = pet.getImageUrl();
        this.removeById(petId);
        petCacheInvalidator.evictPetDetail(petId);
        petCacheInvalidator.evictPetListPages();
        deleteImageFromOss(imageUrl);
    }

    @Override
    @Transactional
    public PetListVo updatePetStatus(Integer petId, String status) {
        Pets pet = this.getById(petId);
        if (pet == null) {
            throw new BizException(ErrorCode.PET_NOT_FOUND);
        }
        CommonEnum.PetStatusEnum target = parsePetStatus(status);
        if (target == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "无效的宠物状态");
        }
        pet.setStatus(target);
        this.updateById(pet);
        petCacheInvalidator.evictPetDetail(petId);
        petCacheInvalidator.evictPetListPages();
        return buildPetListVo(pet);
    }

    private String normalizeGender(String gender) {
        if (gender == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "性别不能为空");
        }
        String g = gender.trim();
        if ("雄".equals(g)) {
            return "雄";
        }
        if ("雌".equals(g)) {
            return "雌";
        }
        throw new BizException(ErrorCode.BAD_REQUEST, "性别只能为雄或雌");
    }

    private void deleteImageFromOss(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }
        if (!imageUrl.startsWith("https://")) {
            return;
        }
        try {
            ossOperator.delete(imageUrl);
            log.info("已删除宠物图片: {}", imageUrl);
        } catch (Exception e) {
            log.warn("删除宠物图片失败: {}", imageUrl, e);
        }
    }

    private CommonEnum.PetStatusEnum parsePetStatus(String status) {
        if (status == null) {
            return null;
        }
        try {
            return CommonEnum.PetStatusEnum.valueOf(status.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    private PetListVo buildPetListVo(Pets pet) {
        PetListVo petListVo = new PetListVo();
        petListVo.setPid(pet.getPid());
        petListVo.setName(pet.getName());
        petListVo.setSpeciesId(pet.getSpeciesId());
        petListVo.setSpecies(resolveSpeciesName(pet.getSpeciesId()));
        petListVo.setBreedId(pet.getBreedId());
        petListVo.setBreed(resolveBreedName(pet.getBreedId()));
        petListVo.setAge(pet.getAge());
        petListVo.setGender(pet.getGender());
        petListVo.setImage(pet.getImageUrl());
        petListVo.setStatus(pet.getStatus() != null ? pet.getStatus().getCode() : null);
        return petListVo;
    }

    private void validateSpeciesBreed(Integer speciesId, Integer breedId) {
        if (speciesId == null || speciesId <= 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "物种ID不能为空");
        }
        if (breedId == null || breedId <= 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "品种ID不能为空");
        }
        Species species = speciesMapper.selectById(speciesId);
        if (species == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "物种不存在");
        }
        Breed breed = breedMapper.selectById(breedId);
        if (breed == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "品种不存在");
        }
        if (!speciesId.equals(breed.getSpeciesId())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "品种不属于所选物种");
        }
    }

    private String resolveSpeciesName(Integer speciesId) {
        if (speciesId == null) {
            return null;
        }
        Species species = speciesMapper.selectById(speciesId);
        return species != null ? species.getName() : null;
    }

    private String resolveBreedName(Integer breedId) {
        if (breedId == null) {
            return null;
        }
        Breed breed = breedMapper.selectById(breedId);
        return breed != null ? breed.getName() : null;
    }

    @Override
    @Transactional(readOnly = true)
    public com.baomidou.mybatisplus.core.metadata.IPage<com.example.petpojo.vo.AdminPetVo> listPetsForAdmin(Integer currentPage, Integer pageSize, String status) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.example.petpojo.vo.AdminPetVo> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(currentPage, pageSize);
        Integer adminShelterId = UserContext.getCurrentAdminShelterId();
        boolean isPlatformAdmin = UserContext.isPlatformAdmin();
        if (!isPlatformAdmin && adminShelterId == null) {
            log.warn("当前管理员未绑定收容所ID，默认返回空结果");
            return page;
        }
        Integer shelterIdForQuery = isPlatformAdmin ? null : adminShelterId;
        return petsMapper.selectAdminPets(page, status, shelterIdForQuery);
    }
}
