package com.example.petservice.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.example.petpojo.vo.AdoptionTimelineResponse;
import com.example.petpojo.vo.AdoptionTimelineVo;
import com.example.petpojo.entity.Adoptions;
import com.example.petpojo.entity.Fosters;
import com.example.petpojo.entity.Pets;
import com.example.petpojo.entity.Shelters;
import com.example.petpojo.entity.Users;
import com.example.petpojo.entity.enums.CommonEnum;
import com.example.petpojo.vo.AdoptionsVo;
import com.example.petpojo.vo.AdoptionsWithFosterStatusVo;
import com.example.petpojo.vo.FostersVo;
import com.example.petservice.cache.PetCacheInvalidator;
import com.example.petservice.mapper.AdoptionsMapper;
import com.example.petservice.mapper.FosterMapper;
import com.example.petservice.mapper.ListPetsMapper;
import com.example.petservice.mapper.PetsMapper;
import com.example.petservice.mapper.SheltersMapper;
import com.example.petservice.mapper.UsersMapper;
import com.example.petservice.service.AdoptionsService;
import com.example.petservice.service.FosterService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdoptionsServiceImpl extends ServiceImpl<AdoptionsMapper, Adoptions> implements AdoptionsService {

    private final AdoptionsMapper adoptionsMapper;
    private final FosterMapper fosterMapper;
    private final ListPetsMapper listPetsMapper;
    private final PetsMapper petsMapper;
    private final SheltersMapper sheltersMapper;
    private final UsersMapper usersMapper;
    private final FosterService fosterService;
    private final PetCacheInvalidator petCacheInvalidator;
    private final WebSocketNotificationService webSocketNotificationService;
    
    /**
     * 创建领养信息
     * @param pid 宠物id
     * @return 领养信息VO
     */
    @Override
    @Transactional
    public AdoptionsVo createAdoption(Integer pid) {
        // 每次领养申请都应生成独立记录；同时禁止同一宠物存在“进行中/已通过”的领养申请
        LambdaQueryWrapper<Adoptions> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(Adoptions::getPid, pid)
                .in(Adoptions::getStatus,
                        CommonEnum.AdoptionStatusEnum.PENDING,
                        CommonEnum.AdoptionStatusEnum.APPROVED);
        if (this.count(activeWrapper) > 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "该宠物已有进行中的或已通过的领养申请");
        }

        Adoptions adoptions = new Adoptions();
        adoptions.setPid(pid);
        adoptions.setUid(UserContext.getCurrentUserId().intValue());
        adoptions.setStatus(CommonEnum.AdoptionStatusEnum.PENDING);
        adoptions.setAdoptDate(LocalDateTime.now());
        boolean isSuccess = this.save(adoptions);
        if (!isSuccess) {
            throw new BizException(ErrorCode.ADOPTION_CREATE_FAILED);
        }

        petCacheInvalidator.evictPetListPages();
        
        // 通过关联查询获取完整的领养信息
        Integer userId = UserContext.getCurrentUserId().intValue();
        Users applicant = usersMapper.selectById(userId);
        String applicantName = applicant != null && applicant.getUserName() != null && !applicant.getUserName().isBlank()
                ? applicant.getUserName()
                : "用户" + userId;
        String applicantPhone = applicant != null ? applicant.getPhone() : null;
        List<AdoptionsVo> adoptionsVos = adoptionsMapper.getUserAdoptions(userId, 0, 1);
        if (!adoptionsVos.isEmpty()) {
            // 发送WebSocket通知给管理员
            AdoptionsVo adoptionVo = adoptionsVos.get(0);
            Pets pet = petsMapper.selectById(pid);
            Integer shelterId = pet != null ? pet.getShelterId() : adoptionVo.getSid();
            String petName = "未知宠物";
            if (pet != null && pet.getName() != null) {
                petName = pet.getName();
            } else if (adoptionVo.getName() != null) {
                petName = adoptionVo.getName();
            }
            adoptionVo.setApplicantName(applicantName);
            adoptionVo.setApplicantPhone(applicantPhone);
            webSocketNotificationService.sendNewAdoptionNotification(adoptions.getAid(), petName, applicantName, shelterId);
            
            return adoptionVo;
        }
        
        // 如果没有找到，创建一个基本的VO对象
        AdoptionsVo vo = new AdoptionsVo();
        vo.setAid(adoptions.getAid());
        vo.setAdoptionDate(adoptions.getAdoptDate());
        vo.setPid(pid);
        return vo;
    }
    
    /**
     * 查询领养信息
     * @param currentPage 当前页码
     * @param pageSize 每页数量
     * @return 领养信息列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<AdoptionsVo> adoptionInfo(Integer currentPage, Integer pageSize) {
        Integer offset = (currentPage - 1) * pageSize;
        List<AdoptionsVo> adoptionsVo =adoptionsMapper.getAdoptionsInfo(offset, pageSize, UserContext.getCurrentUserId().intValue());
        return adoptionsVo;
    }

    /**
     * 查询用户领养记录（带分页）
     * @param userId 用户ID
     * @param currentPage 当前页码
     * @param pageSize 每页数量
     * @return 用户领养记录列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<AdoptionsVo> getUserAdoptions(Long userId, Integer currentPage, Integer pageSize) {
        Integer offset = (currentPage - 1) * pageSize;
        return adoptionsMapper.getUserAdoptions(userId.intValue(), offset, pageSize);
    }

    /**
     * 查询用户已通过的领养记录（仅 APPROVED）
     */
    @Override
    @Transactional(readOnly = true)
    public List<AdoptionsVo> getUserApprovedAdoptions(Long userId, Integer currentPage, Integer pageSize) {
        Integer offset = (currentPage - 1) * pageSize;
        return adoptionsMapper.getUserApprovedAdoptions(userId.intValue(), offset, pageSize);
    }

    /**
     * 简化版查询用户领养记录（只关注领养本身，不关联寄养状态）
     * @param userId 用户ID
     * @param currentPage 当前页码
     * @param pageSize 每页数量
     * @return 用户领养记录列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<AdoptionsVo> getUserAdoptionsSimple(Long userId, Integer currentPage, Integer pageSize) {
        Integer offset = (currentPage - 1) * pageSize;
        return adoptionsMapper.getUserAdoptionsSimple(userId.intValue(), offset, pageSize);
    }

    /**
     * 查询用户领养记录（带分页和总记录数）
     * @param userId 用户ID
     * @param currentPage 当前页码
     * @param pageSize 每页数量
     * @return 领养记录分页对象
     */
    @Override
    @Transactional(readOnly = true)
    public IPage<AdoptionsVo> getUserAdoptionsWithPage(Long userId, Integer currentPage, Integer pageSize) {
        return getUserAdoptionsWithPage(userId, currentPage, pageSize, null);
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<AdoptionsVo> getUserAdoptionsWithPage(Long userId, Integer currentPage, Integer pageSize, String status) {
        // 创建分页对象
        Page<AdoptionsVo> page = new Page<>(currentPage, pageSize);

        // 状态为 APPROVED 时使用专用 SQL，避免与申请列表混用
        if (status != null && "APPROVED".equalsIgnoreCase(status.trim())) {
            return adoptionsMapper.selectUserApprovedAdoptionsWithPage(page, userId.intValue());
        }

        // 默认返回全部状态（用于申请记录列表）
        return adoptionsMapper.selectUserAdoptionsWithPage(page, userId.intValue());
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<AdoptionsVo> getUserApprovedAdoptionsWithPage(Long userId, Integer currentPage, Integer pageSize) {
        Page<AdoptionsVo> page = new Page<>(currentPage, pageSize);
        return adoptionsMapper.selectUserApprovedAdoptionsWithPage(page, userId.intValue());
    }

    /**
     * 查询用户领养记录（带寄养状态）
     * @param userId 用户ID
     * @param currentPage 当前页码
     * @param pageSize 每页数量
     * @return 用户领养记录列表（带寄养状态）
     */
    @Override
    @Transactional(readOnly = true)
    public List<AdoptionsWithFosterStatusVo> getUserAdoptionsWithFosterStatus(Long userId, Integer currentPage, Integer pageSize) {
        // 1. 获取用户的领养记录
        Integer offset = (currentPage - 1) * pageSize;
        List<AdoptionsVo> adoptions = adoptionsMapper.getUserAdoptions(userId.intValue(), offset, pageSize);
        
        // 2. 获取用户的所有寄养记录（包括已结束的）
        List<FostersVo> allFosters = fosterMapper.getFostersInfo(0, 100, userId.intValue(), null);
        
        // 3. 构建宠物ID到最新寄养状态的映射
        Map<Integer, String> petFosterStatusMap = allFosters.stream()
                .collect(Collectors.toMap(
                        foster -> foster.getPet().getPid(),
                        foster -> {
                            String status = foster.getStatus();
                            if ("active".equalsIgnoreCase(status)) {
                                return "active";
                            }
                            if ("ended".equalsIgnoreCase(status)) {
                                return "ended";
                            }
                            if ("archived".equalsIgnoreCase(status)) {
                                return "archived";
                            }
                            return "unknown";
                        },
                        (existing, replacement) -> {
                            if ("active".equals(replacement)) {
                                return replacement;
                            }
                            if ("ended".equals(replacement) && !"active".equals(existing)) {
                                return replacement;
                            }
                            if ("archived".equals(replacement) && existing == null) {
                                return replacement;
                            }
                            return existing;
                        }
                ));
        
        // 4. 转换为新的VO格式，包含寄养状态
        return adoptions.stream()
                .map(adoption -> {
                    AdoptionsWithFosterStatusVo vo = new AdoptionsWithFosterStatusVo();
                    vo.setId(adoption.getAid());
                    vo.setAdoptDate(adoption.getAdoptionDate());
                    
                    // 设置宠物信息
                    AdoptionsWithFosterStatusVo.PetInfo petInfo = new AdoptionsWithFosterStatusVo.PetInfo(
                            adoption.getPid(),
                            adoption.getName(),
                            adoption.getBreed(),
                            adoption.getAge(),
                            adoption.getGender(),
                            adoption.getImage()
                    );
                    vo.setPet(petInfo);
                    
                    // 设置收容所信息
                    AdoptionsWithFosterStatusVo.ShelterInfo shelterInfo = new AdoptionsWithFosterStatusVo.ShelterInfo(
                            adoption.getSid(),
                            adoption.getSname(),
                            adoption.getLocation()
                    );
                    vo.setShelter(shelterInfo);
                    
                    // 判断寄养状态
                    String fosterStatus = petFosterStatusMap.get(adoption.getPid());
                    if (fosterStatus != null) {
                        vo.setFosterStatus(fosterStatus);
                    } else {
                        vo.setFosterStatus("available");
                    }
                    
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取宠物领养时间线
     * @param petId 宠物ID
     * @param userId 用户ID
     * @return 领养时间线数据
     */
    @Override
    @Transactional(readOnly = true)
    public AdoptionTimelineResponse getAdoptionTimeline(Integer petId, Long userId) {
        // 获取宠物信息
        Pets pet = listPetsMapper.selectPetById(petId);
        if (pet == null) {
            throw new BizException(ErrorCode.PET_NOT_FOUND);
        }
        
        // 获取用户信息
        Users user = usersMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.USER_NOT_FOUND);
        }
        
        List<AdoptionTimelineVo> timeline = new ArrayList<>();
        
        // 1. 获取领养记录
        LambdaQueryWrapper<Adoptions> adoptionWrapper = new LambdaQueryWrapper<>();
        adoptionWrapper.eq(Adoptions::getPid, petId)
                       .eq(Adoptions::getUid, userId.intValue())
                       .orderByDesc(Adoptions::getAdoptDate)
                       .last("LIMIT 1");
        Adoptions adoption = this.getOne(adoptionWrapper, false);
        // 拒绝/无记录：直接返回空时间线
        if (adoption == null
                || CommonEnum.AdoptionStatusEnum.REJECTED.equals(adoption.getStatus())) {
            return AdoptionTimelineResponse.builder()
                    .timeline(new ArrayList<>())
                    .total(0)
                    .petName(pet.getName())
                    .petBreed(pet.getBreed())
                    .build();
        }
        
        final LocalDateTime adoptDate = adoption != null ? adoption.getAdoptDate() : null;
        if (adoptDate != null && adoption != null && CommonEnum.AdoptionStatusEnum.APPROVED.equals(adoption.getStatus())) {
            timeline.add(createTimelineVo(
                    petId,
                    adoption.getUid(),
                    "adopted",
                    adoptDate,
                    String.format("%s 领养了 %s (%s)", user.getUserName(), pet.getName(), pet.getBreed()),
                    resolveShelterInfo(pet.getShelterId()),
                    "已领养"));
        }
        
        // 2. 获取寄养记录
        List<Fosters> fosters = filterFostersAfterAdoption(
                adoptDate,
                fosterService.listFostersByPetAndUser(petId, userId.intValue()));
        timeline.addAll(buildFosterTimeline(petId, pet, user, fosters));

        List<AdoptionTimelineVo> orderedTimeline = timeline.stream()
                .sorted(Comparator
                        .comparing(AdoptionTimelineVo::getActionTime)
                        .thenComparing(vo -> actionPriority(vo.getAction()))
                        .thenComparing(AdoptionTimelineVo::getDescription))
                .collect(Collectors.toList());

        for (int i = 0; i < orderedTimeline.size(); i++) {
            orderedTimeline.get(i).setId(i + 1);
        }

        return AdoptionTimelineResponse.builder()
                .timeline(orderedTimeline)
                .total(orderedTimeline.size())
                .petName(pet.getName())
                .petBreed(pet.getBreed())
                .build();
    }

    /**
     * 通过宠物ID获取宠物名称
     * @param pid 宠物ID
     * @return 宠物名称
     */
    @Override
    public String getPetNameById(Long pid) {
        try {
            Pets pet = petsMapper.selectById(pid);
            return pet != null ? pet.getName() : "宠物";
        } catch (Exception e) {
            log.warn("获取宠物名称失败: pid={}, error={}", pid, e.getMessage());
            return "宠物";
        }
    }

    @Override
    @Transactional
    public void approveAdoption(Integer adoptionId, String note) {
        Adoptions adoption = this.getById(adoptionId);
        if (adoption == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "领养记录不存在");
        }
        if (!CommonEnum.AdoptionStatusEnum.PENDING.equals(adoption.getStatus())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "当前状态不可审批");
        }
        Pets pet = petsMapper.selectById(adoption.getPid());
        if (pet == null) {
            throw new BizException(ErrorCode.PET_NOT_FOUND);
        }
        adoption.setStatus(CommonEnum.AdoptionStatusEnum.APPROVED);
        adoption.setAdoptDate(LocalDateTime.now());
        adoption.setReviewerId(UserContext.getCurrentUserId().intValue());
        adoption.setReviewTime(LocalDateTime.now());
        adoption.setReviewNote(note);
        if (!this.updateById(adoption)) {
            throw new BizException(ErrorCode.ADOPTION_CREATE_FAILED, "更新领养状态失败");
        }
        pet.setStatus(CommonEnum.PetStatusEnum.ADOPTED);
        petsMapper.updateById(pet);

        petCacheInvalidator.evictPetDetail(adoption.getPid());
        petCacheInvalidator.evictPetListPages();
    }

    @Override
    @Transactional
    public void rejectAdoption(Integer adoptionId, String note) {
        Adoptions adoption = this.getById(adoptionId);
        if (adoption == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "领养记录不存在");
        }
        if (!CommonEnum.AdoptionStatusEnum.PENDING.equals(adoption.getStatus())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "当前状态不可拒绝");
        }
        adoption.setStatus(CommonEnum.AdoptionStatusEnum.REJECTED);
        adoption.setReviewerId(UserContext.getCurrentUserId().intValue());
        adoption.setReviewTime(LocalDateTime.now());
        adoption.setReviewNote(note);
        if (!this.updateById(adoption)) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "更新领养状态失败");
        }
        // 领养被拒绝，宠物回到可领养
        Pets pet = petsMapper.selectById(adoption.getPid());
        if (pet != null) {
            pet.setStatus(CommonEnum.PetStatusEnum.AVAILABLE);
            petsMapper.updateById(pet);
        }

        petCacheInvalidator.evictPetDetail(adoption.getPid());
        petCacheInvalidator.evictPetListPages();
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<AdoptionsVo> listAdoptionsForAdmin(Integer currentPage, Integer pageSize, String status) {
        Page<AdoptionsVo> page = new Page<>(currentPage, pageSize);
        Integer adminShelterId = UserContext.getCurrentAdminShelterId();
        String normalizedStatus = (status != null && !status.isBlank()) ? status.toUpperCase() : null;
        List<AdoptionsVo> records = adoptionsMapper.getAdminAdoptions(
                (int) ((currentPage - 1L) * pageSize),
                pageSize,
                normalizedStatus,
                adminShelterId);
        Long total = adoptionsMapper.countAdminAdoptions(normalizedStatus, adminShelterId);
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    private List<Fosters> filterFostersAfterAdoption(LocalDateTime adoptDate, List<Fosters> fosters) {
        if (adoptDate == null) {
            return fosters;
        }
        LocalDate adoptDay = adoptDate.toLocalDate();
        return fosters.stream()
                .filter(foster -> {
                    LocalDateTime startTime = resolveStartTime(foster);
                    if (startTime == null) {
                        return true;
                    }
                    LocalDate fosterDay = startTime.toLocalDate();
                    return !fosterDay.isBefore(adoptDay);
                })
                .collect(Collectors.toList());
    }

    private List<AdoptionTimelineVo> buildFosterTimeline(Integer petId, Pets pet, Users user, List<Fosters> fosters) {
        List<AdoptionTimelineVo> events = new ArrayList<>();
        for (Fosters foster : fosters) {
            // 寄养申请中/已拒绝：不属于“寄养记录”，不展示开始/结束时间线
            if (CommonEnum.FosterStatusEnum.PENDING.equals(foster.getStatus())
                    || CommonEnum.FosterStatusEnum.REJECTED.equals(foster.getStatus())) {
                continue;
            }
            AdoptionTimelineVo.ShelterInfo fosterShelter = resolveShelterInfo(foster.getSid());
            LocalDateTime startTime = resolveStartTime(foster);
            String startedStatusLabel = CommonEnum.FosterStatusEnum.COMPLETED.equals(foster.getStatus()) ? "寄养已结束" : "寄养中";
            events.add(createTimelineVo(
                    petId,
                    foster.getUid(),
                    "foster_started",
                    startTime,
                    String.format("%s 将 %s 寄养在 %s", user.getUserName(), pet.getName(),
                            fosterShelter != null ? fosterShelter.getName() : "未知收容所"),
                    fosterShelter,
                    startedStatusLabel));

            if (hasFosterEnded(foster)) {
                LocalDateTime endTime = resolveEndTime(foster);
                events.add(createTimelineVo(
                        petId,
                        foster.getUid(),
                        "foster_ended",
                        endTime,
                        String.format("%s 的寄养结束，%s 返回主人身边", pet.getName(), pet.getName()),
                        fosterShelter,
                        CommonEnum.FosterStatusEnum.REJECTED.equals(foster.getStatus()) ? "寄养终止" : "寄养结束"));
            }
        }
        return events;
    }

    private LocalDateTime resolveStartTime(Fosters foster) {
        if (foster == null) {
            return null;
        }
        if (foster.getStartDate() != null) {
            return foster.getStartDate();
        }
        return foster.getCreateTime();
    }

    private LocalDateTime resolveEndTime(Fosters foster) {
        if (foster == null) {
            return null;
        }
        if (foster.getEndDate() != null) {
            return foster.getEndDate();
        }
        if (CommonEnum.FosterStatusEnum.COMPLETED.equals(foster.getStatus())
                || CommonEnum.FosterStatusEnum.REJECTED.equals(foster.getStatus())) {
            return foster.getUpdateTime();
        }
        return null;
    }

    private AdoptionTimelineVo createTimelineVo(Integer petId, Integer operatorId, String action,
                                                LocalDateTime actionTime, String description,
                                                AdoptionTimelineVo.ShelterInfo shelterInfo, String statusLabel) {
        return AdoptionTimelineVo.builder()
                .petId(petId)
                .action(action)
                .actionTime(actionTime)
                .operatorUserId(operatorId)
                .description(description)
                .shelter(shelterInfo)
                .statusLabel(statusLabel)
                .build();
    }

    private boolean hasFosterEnded(Fosters foster) {
        if (foster == null) {
            return false;
        }
        return CommonEnum.FosterStatusEnum.COMPLETED.equals(foster.getStatus())
                || CommonEnum.FosterStatusEnum.REJECTED.equals(foster.getStatus())
                || foster.getEndDate() != null;
    }

    private int actionPriority(String action) {
        if ("adopted".equals(action)) {
            return 0;
        }
        if ("foster_started".equals(action)) {
            return 1;
        }
        if ("foster_ended".equals(action)) {
            return 2;
        }
        return 3;
    }

    private AdoptionTimelineVo.ShelterInfo resolveShelterInfo(Integer shelterId) {
        if (shelterId == null) {
            return null;
        }
        Shelters shelter = sheltersMapper.selectById(shelterId);
        if (shelter == null) {
            return null;
        }
        return new AdoptionTimelineVo.ShelterInfo(
                shelter.getSid(),
                shelter.getName(),
                shelter.getLocation());
    }
}
