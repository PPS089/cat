package com.example.petservice.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.example.petservice.service.WebSocketNotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petcommon.context.UserContext;
import com.example.petcommon.error.ErrorCode;
import com.example.petcommon.exception.BizException;
import com.example.petpojo.entity.Adoptions;
import com.example.petpojo.entity.Fosters;
import com.example.petpojo.entity.Pets;
import com.example.petpojo.entity.Users;
import com.example.petpojo.entity.enums.CommonEnum;
import com.example.petpojo.vo.FostersVo;
import com.example.petservice.cache.PetCacheInvalidator;
import com.example.petservice.mapper.AdoptionsMapper;
import com.example.petservice.mapper.FosterMapper;
import com.example.petservice.mapper.PetsMapper;
import com.example.petservice.mapper.UsersMapper;
import com.example.petservice.service.FosterService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 寄养服务实现类
 * @author 33185
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FosterServiceImpl extends ServiceImpl<FosterMapper, Fosters> implements FosterService {
    private final FosterMapper fosterMapper;
    private final PetsMapper petsMapper;
    private final AdoptionsMapper adoptionsMapper;
    private final UsersMapper usersMapper;
    private final PetCacheInvalidator petCacheInvalidator;
    private final WebSocketNotificationService webSocketNotificationService;

    @Override
    @Transactional
    public FostersVo createFoster(Long petId, Long shelterId, LocalDate startDate) {
        Pets pet = petsMapper.selectById(petId);
        if (pet == null) {
            throw new BizException(ErrorCode.PET_NOT_FOUND);
        }
        if (!CommonEnum.PetStatusEnum.ADOPTED.equals(pet.getStatus())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "宠物尚未被领养，无法寄养");
        }

        Integer currentUserId = UserContext.getCurrentUserId().intValue();
        assertPetOwnership(pet.getPid(), currentUserId);

        Fosters activeFoster = findActiveFosterRecord(petId.intValue(), currentUserId);
        if (activeFoster != null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "已存在寄养申请或正在寄养，无法重复申请");
        }

        Fosters foster = Fosters.builder()
                .pid(petId.intValue())
                .uid(currentUserId)
                .sid(shelterId.intValue())
                .startDate(resolveStartDate(startDate))
                .endDate(null)
                .status(CommonEnum.FosterStatusEnum.PENDING)
                .deleted(Boolean.FALSE)
                .build();

        if (!this.save(foster)) {
            throw new BizException(ErrorCode.FOSTER_CREATE_FAILED, "寄养记录创建失败");
        }

        // 发送WebSocket通知给管理员
        String petName = pet.getName() != null ? pet.getName() : "未知宠物";
        Users applicant = usersMapper.selectById(currentUserId);
        String applicantName = applicant != null && applicant.getUserName() != null && !applicant.getUserName().isBlank()
                ? applicant.getUserName()
                : "用户" + currentUserId;
        webSocketNotificationService.sendNewFosterNotification(foster.getFid(), petName, applicantName, shelterId.intValue());

        return fosterMapper.getFosterById(foster.getFid());
    }

    @Override
    @Transactional
    public boolean endFoster(Long id) {
        Fosters foster = this.getById(id);
        if (foster == null || Boolean.TRUE.equals(foster.getDeleted())) {
            return false;
        }

        Integer currentUserId = UserContext.getCurrentUserId().intValue();
        assertPetOwnership(foster.getPid(), currentUserId);
        if (!Objects.equals(foster.getUid(), currentUserId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无法操作他人的寄养记录");
        }

        if (!CommonEnum.FosterStatusEnum.ONGOING.equals(foster.getStatus())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "寄养记录未在进行中");
        }

        foster.setEndDate(LocalDateTime.now());
        foster.setStatus(CommonEnum.FosterStatusEnum.COMPLETED);
        if (!this.updateById(foster)) {
            throw new BizException(ErrorCode.FOSTER_END_FAILED, "寄养记录结束失败");
        }

        updatePetStatusIfNoActiveFoster(foster.getPid());
        return true;
    }

    @Override
    @Transactional
    public boolean deleteFoster(Integer id) {
        Fosters foster = this.getById(id);
        if (foster == null) {
            throw new BizException(ErrorCode.FOSTER_NOT_FOUND, "寄养记录不存在");
        }

        Integer currentUserId = UserContext.getCurrentUserId().intValue();
        assertPetOwnership(foster.getPid(), currentUserId);
        if (!Objects.equals(foster.getUid(), currentUserId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无法操作他人的寄养记录");
        }

        if (CommonEnum.FosterStatusEnum.ONGOING.equals(foster.getStatus())) {
            throw new BizException(ErrorCode.FOSTER_PET_IS_FOSTERING, "宠物正在寄养中，无法删除记录");
        }

        LocalDateTime endDate = foster.getEndDate() != null ? foster.getEndDate() : LocalDateTime.now();
        boolean updated = this.lambdaUpdate()
                .eq(Fosters::getFid, id)
                .set(Fosters::getDeleted, Boolean.TRUE)
                .set(Fosters::getEndDate, endDate)
                .update();
        if (!updated) {
            throw new BizException(ErrorCode.FOSTER_DELETE_FAILED, "寄养记录删除失败");
        }
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FostersVo> fosterInfo(Integer currentPage, Integer pageSize) {
        Integer offset = (currentPage - 1) * pageSize;
        return fosterMapper.getFostersInfo(offset, pageSize, UserContext.getCurrentUserId().intValue(), null);
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<FostersVo> getUserFostersWithPage(Long userId, Integer currentPage, Integer pageSize, String status) {
        Page<FostersVo> page = new Page<>(currentPage, pageSize);
        String normalizedStatus = (status == null || status.isBlank()) ? null : status.trim().toUpperCase();

        LambdaQueryWrapper<Fosters> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(Fosters::getUid, userId.intValue())
                .eq(Fosters::getDeleted, Boolean.FALSE);
        if (normalizedStatus != null) {
            try {
                CommonEnum.FosterStatusEnum parsed = CommonEnum.FosterStatusEnum.valueOf(normalizedStatus);
                countWrapper.eq(Fosters::getStatus, parsed);
            } catch (Exception ignored) {
            }
        }
        long total = this.count(countWrapper);

        Integer offset = (currentPage - 1) * pageSize;
        List<FostersVo> records = fosterMapper.getFostersInfo(offset, pageSize, userId.intValue(), normalizedStatus);
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    @Override
    @Transactional
    public boolean endFosterByPetId(Long petId) {
        Pets pet = petsMapper.selectById(petId);
        if (pet == null) {
            throw new BizException(ErrorCode.PET_NOT_FOUND);
        }

        Integer currentUserId = UserContext.getCurrentUserId().intValue();
        assertPetOwnership(pet.getPid(), currentUserId);

        Fosters foster = findActiveFosterRecord(petId.intValue(), currentUserId);
        if (foster == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "未找到活跃的寄养记录");
        }

        foster.setEndDate(LocalDateTime.now());
        foster.setStatus(CommonEnum.FosterStatusEnum.COMPLETED);
        if (!this.updateById(foster)) {
            throw new BizException(ErrorCode.FOSTER_END_FAILED, "寄养记录结束失败");
        }

        updatePetStatusIfNoActiveFoster(foster.getPid());
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Fosters> listFostersByPetAndUser(Integer petId, Integer userId) {
        LambdaQueryWrapper<Fosters> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Fosters::getPid, petId)
                .eq(Fosters::getUid, userId)
                .orderByAsc(Fosters::getStartDate);
        return this.list(queryWrapper);
    }

    @Override
    @Transactional
    public void approveFoster(Integer fosterId, String note) {
        Fosters foster = this.getById(fosterId);
        if (foster == null) {
            throw new BizException(ErrorCode.FOSTER_NOT_FOUND, "寄养记录不存在");
        }
        if (!CommonEnum.FosterStatusEnum.PENDING.equals(foster.getStatus())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "当前状态不可审批");
        }
        foster.setStatus(CommonEnum.FosterStatusEnum.ONGOING);
        foster.setReviewerId(UserContext.getCurrentUserId().intValue());
        foster.setReviewTime(LocalDateTime.now());
        foster.setReviewNote(note);
        if (foster.getStartDate() == null) {
            foster.setStartDate(LocalDateTime.now());
        }
        if (!this.updateById(foster)) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "更新寄养状态失败");
        }
        Pets pet = petsMapper.selectById(foster.getPid());
        if (pet != null) {
            pet.setStatus(CommonEnum.PetStatusEnum.FOSTERING);
            petsMapper.updateById(pet);
            petCacheInvalidator.evictPetDetail(pet.getPid());
            petCacheInvalidator.evictPetListPages();
        }
    }

    @Override
    @Transactional
    public void rejectFoster(Integer fosterId, String note) {
        Fosters foster = this.getById(fosterId);
        if (foster == null) {
            throw new BizException(ErrorCode.FOSTER_NOT_FOUND, "寄养记录不存在");
        }
        if (!CommonEnum.FosterStatusEnum.PENDING.equals(foster.getStatus())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "当前状态不可拒绝");
        }
        foster.setStatus(CommonEnum.FosterStatusEnum.REJECTED);
        foster.setReviewerId(UserContext.getCurrentUserId().intValue());
        foster.setReviewTime(LocalDateTime.now());
        foster.setReviewNote(note);
        this.updateById(foster);
        updatePetStatusIfNoActiveFoster(foster.getPid());
    }

    @Override
    @Transactional
    public void completeFoster(Integer fosterId) {
        Fosters foster = this.getById(fosterId);
        if (foster == null) {
            throw new BizException(ErrorCode.FOSTER_NOT_FOUND, "寄养记录不存在");
        }
        if (!CommonEnum.FosterStatusEnum.ONGOING.equals(foster.getStatus())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "仅进行中的寄养可完成");
        }
        foster.setStatus(CommonEnum.FosterStatusEnum.COMPLETED);
        foster.setEndDate(LocalDateTime.now());
        if (!this.updateById(foster)) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "更新寄养状态失败");
        }
        updatePetStatusIfNoActiveFoster(foster.getPid());
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<FostersVo> listFostersForAdmin(Integer currentPage, Integer pageSize, String status) {
        Page<FostersVo> page = new Page<>(currentPage, pageSize);
        Integer adminShelterId = UserContext.getCurrentAdminShelterId();
        List<FostersVo> records = fosterMapper.getAdminFosters(
                (int) ((currentPage - 1L) * pageSize),
                pageSize,
                status,
                adminShelterId);
        LambdaQueryWrapper<Fosters> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Fosters::getDeleted, Boolean.FALSE);
        if (status != null && !status.isBlank()) {
            try {
                CommonEnum.FosterStatusEnum parsed = CommonEnum.FosterStatusEnum.valueOf(status.toUpperCase());
                wrapper.eq(Fosters::getStatus, parsed);
            } catch (Exception ignored) {
            }
        }
        if (adminShelterId != null) {
            wrapper.eq(Fosters::getSid, adminShelterId);
        }
        long total = this.count(wrapper);
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    private Fosters findActiveFosterRecord(Integer petId, Integer userId) {
        LambdaQueryWrapper<Fosters> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Fosters::getPid, petId)
                .eq(Fosters::getUid, userId)
                .eq(Fosters::getDeleted, Boolean.FALSE)
                .in(Fosters::getStatus,
                        CommonEnum.FosterStatusEnum.PENDING,
                        CommonEnum.FosterStatusEnum.ONGOING)
                .orderByDesc(Fosters::getStartDate)
                .last("LIMIT 1");
        return this.getOne(queryWrapper, false);
    }

    private void assertPetOwnership(Integer petId, Integer userId) {
        LambdaQueryWrapper<Adoptions> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Adoptions::getPid, petId)
                .eq(Adoptions::getUid, userId)
                .eq(Adoptions::getStatus, CommonEnum.AdoptionStatusEnum.APPROVED)
                .orderByDesc(Adoptions::getAdoptDate)
                .last("LIMIT 1");
        Adoptions adoption = adoptionsMapper.selectOne(wrapper);
        if (adoption == null) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权操作该宠物的寄养记录");
        }
    }

    private void updatePetStatusIfNoActiveFoster(Integer petId) {
        LambdaQueryWrapper<Fosters> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Fosters::getPid, petId)
                .eq(Fosters::getStatus, CommonEnum.FosterStatusEnum.ONGOING)
                .eq(Fosters::getDeleted, Boolean.FALSE);
        long count = this.count(wrapper);
        if (count == 0) {
            Pets pet = petsMapper.selectById(petId);
            if (pet != null && CommonEnum.PetStatusEnum.FOSTERING.equals(pet.getStatus())) {
                pet.setStatus(CommonEnum.PetStatusEnum.ADOPTED);
                petsMapper.updateById(pet);
                petCacheInvalidator.evictPetDetail(pet.getPid());
                petCacheInvalidator.evictPetListPages();
            }
        }
    }

    private LocalDateTime resolveStartDate(LocalDate startDate) {
        if (startDate == null) {
            return LocalDateTime.now();
        }
        LocalDate today = LocalDate.now();
        if (startDate.isEqual(today)) {
            return LocalDateTime.now();
        }
        return startDate.atStartOfDay();
    }
}
