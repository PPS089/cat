package com.example.petservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petcommon.context.UserContext;
import com.example.petpojo.dto.AdoptionTimelineResponse;
import com.example.petpojo.entity.Adoptions;
import com.example.petpojo.entity.Fosters;
import com.example.petpojo.entity.Pets;
import com.example.petpojo.entity.Shelters;
import com.example.petpojo.entity.Users;
import com.example.petservice.mapper.AdoptionsMapper;
import com.example.petservice.mapper.FosterMapper;
import com.example.petservice.mapper.PetsMapper;
import com.example.petservice.mapper.SheltersMapper;
import com.example.petservice.mapper.UsersMapper;
import com.example.petservice.service.AdoptionsService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.petpojo.vo.AdoptionTimelineVo;
import com.example.petpojo.vo.AdoptionsVo;
import com.example.petpojo.vo.AdoptionsWithFosterStatusVo;
import com.example.petpojo.vo.FostersVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdoptionsServiceImpl extends ServiceImpl<AdoptionsMapper, Adoptions> implements AdoptionsService {

    @Autowired
    private AdoptionsMapper adoptionsMapper;
    
    @Autowired
    private FosterMapper fosterMapper;
    
    @Autowired
    private PetsMapper petsMapper;
    
    @Autowired
    private SheltersMapper sheltersMapper;
    
    @Autowired
    private UsersMapper usersMapper;
    
    /**
     * 创建领养信息
     * @param pid 宠物id
     */
    @Override
    public Adoptions createAdoption(Integer pid) {
        Adoptions adoptions = new Adoptions();
        adoptions.setPid(pid);
        adoptions.setUid(UserContext.getCurrentUserId().intValue());
        adoptions.setAdoptDate(LocalDateTime.now()); // 设置领养日期为当前时间
        boolean isSuccess = this.save(adoptions);
        if (!isSuccess) {
            throw new RuntimeException("领养信息创建失败");
        }
        return adoptions;
    }
    
    /**
     * 查询领养信息
     */
    @Override
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
     */
    @Override
    public List<AdoptionsVo> getUserAdoptions(Long userId, Integer currentPage, Integer pageSize) {
        Integer offset = (currentPage - 1) * pageSize;
        return adoptionsMapper.getUserAdoptions(userId.intValue(), offset, pageSize);
    }

    /**
     * 简化版查询用户领养记录（只关注领养本身，不关联寄养状态）
     * @param userId 用户ID
     * @param currentPage 当前页码
     * @param pageSize 每页数量
     */
    @Override
    public List<AdoptionsVo> getUserAdoptionsSimple(Long userId, Integer currentPage, Integer pageSize) {
        Integer offset = (currentPage - 1) * pageSize;
        return adoptionsMapper.getUserAdoptionsSimple(userId.intValue(), offset, pageSize);
    }

    /**
     * 查询用户领养记录（带分页和总记录数）
     * @param userId 用户ID
     * @param currentPage 当前页码
     * @param pageSize 每页数量
     */
    @Override
    public IPage<AdoptionsVo> getUserAdoptionsWithPage(Long userId, Integer currentPage, Integer pageSize) {
        // 创建分页对象
        Page<AdoptionsVo> page = new Page<>(currentPage, pageSize);
        
        // 使用MyBatis Plus的分页查询功能
        return adoptionsMapper.selectUserAdoptionsWithPage(page, userId.intValue());
    }

    /**
     * 查询用户领养记录（带寄养状态）
     * @param userId 用户ID
     * @param currentPage 当前页码
     * @param pageSize 每页数量
     */
    @Override
    public List<AdoptionsWithFosterStatusVo> getUserAdoptionsWithFosterStatus(Long userId, Integer currentPage, Integer pageSize) {
        // 1. 获取用户的领养记录
        Integer offset = (currentPage - 1) * pageSize;
        List<AdoptionsVo> adoptions = adoptionsMapper.getUserAdoptions(userId.intValue(), offset, pageSize);
        
        // 2. 获取用户的所有寄养记录（包括已结束的）
        List<FostersVo> allFosters = fosterMapper.getFostersInfo(0, 100, userId.intValue());
        
        // 3. 构建宠物ID到最新寄养状态的映射
        Map<Integer, String> petFosterStatusMap = allFosters.stream()
                .collect(Collectors.toMap(
                        foster -> foster.getPet().getPid(),
                        foster -> {
                            // 根据结束时间判断寄养状态
                            if (foster.getEndDate() == null) {
                                return "active";  // 没有结束时间，寄养中
                            } else {
                                return "ended";   // 有结束时间，寄养结束
                            }
                        },
                        (existing, replacement) -> {
                            // 如果同一个宠物有多条寄养记录，优先选择活跃状态
                            if ("active".equals(replacement)) {
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
     */
    @Override
    public AdoptionTimelineResponse getAdoptionTimeline(Integer petId, Long userId) {
        // 获取宠物信息
        Pets pet = petsMapper.selectById(petId);
        if (pet == null) {
            throw new RuntimeException("宠物不存在");
        }
        
        // 获取用户信息
        Users user = usersMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        List<AdoptionTimelineVo> timeline = new ArrayList<>();
        Integer timelineId = 1;
        
        // 1. 获取领养记录
        LambdaQueryWrapper<Adoptions> adoptionWrapper = new LambdaQueryWrapper<>();
        adoptionWrapper.eq(Adoptions::getPid, petId)
                      .eq(Adoptions::getUid, userId.intValue());
        Adoptions adoption = this.getOne(adoptionWrapper);
        
        if (adoption != null) {
            // 获取收容所信息
            Shelters shelter = sheltersMapper.selectById(pet.getShelterId());
            String shelterName = shelter != null ? shelter.getName() : "未知收容所";
            
            // 添加领养时间线项
            timeline.add(AdoptionTimelineVo.builder()
                    .id(timelineId++)
                    .petId(petId)
                    .petName(pet.getName())
                    .petBreed(pet.getBreed())
                    .action("adopted")
                    .actionDate(adoption.getAdoptDate())
                    .description(String.format("%s 领养了 %s (%s)", user.getUserName(), pet.getName(), pet.getBreed()))
                    .shelterName(shelterName)
                    .status("已完成")
                    .build());
        }
        
        // 2. 获取寄养记录
        LambdaQueryWrapper<Fosters> fosterWrapper = new LambdaQueryWrapper<>();
        fosterWrapper.eq(Fosters::getPid, petId)
                    .eq(Fosters::getUid, userId.intValue())
                    .orderByAsc(Fosters::getStartDate);
        List<Fosters> fosters = fosterMapper.selectList(fosterWrapper);
        
        for (Fosters foster : fosters) {
            // 获取收容所信息 - 使用宠物所属的收容所
            Shelters fosterShelter = sheltersMapper.selectById(pet.getShelterId());
            String fosterShelterName = fosterShelter != null ? fosterShelter.getName() : "未知收容所";
            
            // 添加开始寄养时间线项
            timeline.add(AdoptionTimelineVo.builder()
                    .id(timelineId++)
                    .petId(petId)
                    .petName(pet.getName())
                    .petBreed(pet.getBreed())
                    .action("fostered")
                    .actionDate(foster.getStartDate())
                    .description(String.format("%s 将 %s 寄养在 %s", user.getUserName(), pet.getName(), fosterShelterName))
                    .shelterName(fosterShelterName)
                    .status("寄养中")
                    .build());
            
            // 如果有结束日期，添加结束寄养时间线项
            if (foster.getEndDate() != null) {
                timeline.add(AdoptionTimelineVo.builder()
                        .id(timelineId++)
                        .petId(petId)
                        .petName(pet.getName())
                        .petBreed(pet.getBreed())
                        .action("foster_ended")
                        .actionDate(foster.getEndDate())
                        .description(String.format("%s 的寄养结束，%s 返回主人身边", pet.getName(), pet.getName()))
                        .shelterName(fosterShelterName)
                        .status("寄养结束")
                        .build());
            }
        }
        
        // 按时间排序（最新的在前）
        timeline.sort(Comparator.comparing(AdoptionTimelineVo::getActionDate).reversed());
        
        // 重新设置ID，按时间顺序
        for (int i = 0; i < timeline.size(); i++) {
            timeline.get(i).setId(i + 1);
        }
        
        return AdoptionTimelineResponse.builder()
                .timeline(timeline)
                .total(timeline.size())
                .petName(pet.getName())
                .petBreed(pet.getBreed())
                .build();
    }

}