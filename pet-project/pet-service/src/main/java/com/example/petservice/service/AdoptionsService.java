package com.example.petservice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.petpojo.entity.Adoptions;
import com.example.petpojo.vo.AdoptionsVo;
import com.example.petpojo.vo.AdoptionsWithFosterStatusVo;

import java.util.List;


public interface AdoptionsService extends IService<Adoptions> {
    /**
     * 创建领养信息
     * @param pid 宠物id
     * @return 领养信息
     */
    Adoptions createAdoption(Integer pid);

    /**
     * 查询领养信息
     * @return 领养信息
     */
    List<AdoptionsVo> adoptionInfo(Integer currentPage, Integer pageSize);

    /**
     * 查询用户领养记录（带分页）
     * @param userId 用户ID
     * @param currentPage 当前页码
     * @param pageSize 每页数量
     * @return 用户领养记录列表
     */
    List<AdoptionsVo> getUserAdoptions(Long userId, Integer currentPage, Integer pageSize);

    /**
     * 简化版查询用户领养记录（只关注领养本身，不关联寄养状态）
     * @param userId 用户ID
     * @param currentPage 当前页码
     * @param pageSize 每页数量
     * @return 用户领养记录列表
     */
    List<AdoptionsVo> getUserAdoptionsSimple(Long userId, Integer currentPage, Integer pageSize);

    /**
     * 查询用户领养记录（带分页和总记录数）
     * @param userId 用户ID
     * @param currentPage 当前页码
     * @param pageSize 每页数量
     * @return 领养记录分页对象
     */
    IPage<AdoptionsVo> getUserAdoptionsWithPage(Long userId, Integer currentPage, Integer pageSize);

    /**
     * 查询用户领养记录（带寄养状态）
     * @param userId 用户ID
     * @param currentPage 当前页码
     * @param pageSize 每页数量
     * @return 用户领养记录列表（带寄养状态）
     */
    List<AdoptionsWithFosterStatusVo> getUserAdoptionsWithFosterStatus(Long userId, Integer currentPage, Integer pageSize);

    /**
     * 获取宠物领养时间线
     * @param petId 宠物ID
     * @param userId 用户ID
     * @return 领养时间线数据
     */
    com.example.petpojo.dto.AdoptionTimelineResponse getAdoptionTimeline(Integer petId, Long userId);

}