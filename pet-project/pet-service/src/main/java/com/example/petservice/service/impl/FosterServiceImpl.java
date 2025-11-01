package com.example.petservice.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petcommon.context.UserContext;
import com.example.petpojo.entity.Fosters;
import com.example.petpojo.entity.Pets;
import com.example.petpojo.entity.enums.CommonEnum;
import com.example.petservice.mapper.FosterMapper;
import com.example.petservice.mapper.PetsMapper;
import com.example.petservice.service.FosterService;
import com.example.petpojo.vo.FostersVo;

import lombok.RequiredArgsConstructor;



@Service
@RequiredArgsConstructor
public class FosterServiceImpl extends ServiceImpl<FosterMapper, Fosters> implements FosterService {

    private final FosterMapper fosterMapper;
    private final PetsMapper petsMapper;

    /**
     * 创建寄养信息
     * @param petId 宠物id
     */
    @Override
    public Fosters createFoster(Long petId) {
        Fosters foster = new Fosters();
        foster.setPid(petId.intValue());
        foster.setUid(UserContext.getCurrentUserId().intValue());
        return foster;
    }

    /**
     * 创建寄养信息（带收容所和开始日期）
     * @param petId 宠物id
     * @param shelterId 收容所id
     * @param startDate 开始日期
     */
    @Override
    @Transactional
    public Fosters createFoster(Long petId, Long shelterId, LocalDate startDate) {
        // 检查宠物是否存在且属于当前用户
        Pets pet = petsMapper.selectById(petId);
        if (pet == null) {
            throw new RuntimeException("宠物不存在");
        }
        
        // 检查宠物状态是否为已领养
        if (!CommonEnum.PetStatusEnum.ADOPTED.equals(pet.getStatus())) {
            throw new RuntimeException("宠物尚未被领养，无法寄养");
        }
        
        // 创建寄养记录
        Fosters foster = new Fosters();
        foster.setPid(petId.intValue());
        foster.setUid(UserContext.getCurrentUserId().intValue());
        
        // 设置开始日期为当前时间（忽略传入的日期，使用当前时间）
        foster.setStartDate(LocalDateTime.now());
        
        // 保存寄养记录
        this.save(foster);
        
        // 更新宠物状态为寄养中
        pet.setStatus(CommonEnum.PetStatusEnum.FOSTERING);
        petsMapper.updateById(pet);
        
        return foster;
    }

    /**
     * 结束寄养（不删除记录，只更新状态和结束日期）
     * @param id 寄养信息id
     */
    @Override
    @Transactional
    public boolean endFoster(Long id) {
        // 检查寄养记录是否存在
        Fosters foster = this.getById(id);
        if (foster == null) {
            return false;
        }
        
        // 获取对应的宠物
        Pets pet = petsMapper.selectById(foster.getPid());
        if (pet == null) {
            throw new RuntimeException("宠物不存在");
        }
        
        // 设置寄养结束日期为当前时间
        foster.setEndDate(LocalDateTime.now());
        boolean updated = this.updateById(foster);
        if (!updated) {
            return false;
        }
        
        // 更新宠物状态为已领养（结束寄养）
        pet.setStatus(CommonEnum.PetStatusEnum.ADOPTED);
        petsMapper.updateById(pet);
        
        return true;
    }

    /**
     * 删除寄养记录（真正从数据库删除）
     * @param id 寄养信息id
     */
    @Override
    @Transactional
    public boolean deleteFoster(Long id) {
        // 检查寄养记录是否存在
        Fosters foster = this.getById(id);
        if (foster == null) {
            return false;
        }
        
        // 获取对应的宠物
        Pets pet = petsMapper.selectById(foster.getPid());
        if (pet == null) {
            throw new RuntimeException("宠物不存在");
        }
        
        // 真正从数据库删除寄养记录
        boolean deleted = this.removeById(id);
        if (!deleted) {
            return false;
        }
        
        // 更新宠物状态为已领养
        pet.setStatus(CommonEnum.PetStatusEnum.ADOPTED);
        petsMapper.updateById(pet);
        
        return true;
    }

    /**
     * 查询寄养信息
     */
    @Transactional
    @Override
    public List<FostersVo> fosterInfo(Integer currentPage, Integer pageSize) {
        Integer offset = (currentPage - 1) * pageSize;
        List<FostersVo> fosterList =fosterMapper.getFostersInfo(offset, pageSize, UserContext.getCurrentUserId().intValue());
        return fosterList;
    }
    
    /**
     * 查询用户寄养记录（带分页和总记录数）
     * @param userId 用户ID
     * @param currentPage 当前页码
     * @param pageSize 每页数量
     */
    @Override
    public IPage<FostersVo> getUserFostersWithPage(Long userId, Integer currentPage, Integer pageSize) {
        // 创建分页对象
        Page<FostersVo> page = new Page<>(currentPage, pageSize);
        
        // 查询总记录数
        Long total = fosterMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Fosters>()
                .eq(Fosters::getUid, userId.intValue()));
        
        // 查询当前页数据
        Integer offset = (currentPage - 1) * pageSize;
        List<FostersVo> records = fosterMapper.getFostersInfo(offset, pageSize, userId.intValue());
        
        // 设置分页结果
        page.setTotal(total);
        page.setRecords(records);
        
        return page;
    }

    /**
     * 根据宠物ID结束寄养
     * @param petId 宠物ID
     */
    @Override
    @Transactional
    public boolean endFosterByPetId(Long petId) {
        // 查询宠物是否存在
        Pets pet = petsMapper.selectById(petId);
        if (pet == null) {
            throw new RuntimeException("宠物不存在");
        }
        
        // 检查宠物状态是否为寄养中
        if (!CommonEnum.PetStatusEnum.FOSTERING.equals(pet.getStatus())) {
            throw new RuntimeException("宠物当前不在寄养中");
        }
        
        // 查询当前活跃的寄养记录（没有结束日期的）
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Fosters> queryWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(Fosters::getPid, petId.intValue())
                   .isNull(Fosters::getEndDate)
                   .orderByDesc(Fosters::getStartDate)
                   .last("LIMIT 1");
        
        Fosters currentFoster = this.getOne(queryWrapper);
        if (currentFoster == null) {
            throw new RuntimeException("未找到活跃的寄养记录");
        }
        
        // 设置寄养结束日期
        currentFoster.setEndDate(LocalDateTime.now());
        boolean updated = this.updateById(currentFoster);
        if (!updated) {
            return false;
        }
        
        // 更新宠物状态为已领养
        pet.setStatus(CommonEnum.PetStatusEnum.ADOPTED);
        petsMapper.updateById(pet);
        
        return true;
    }
}