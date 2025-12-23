package com.example.petservice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.petpojo.entity.Adoptions;
import com.example.petpojo.vo.AdoptionTimelineResponse;
import com.example.petpojo.vo.AdoptionsVo;
import com.example.petpojo.vo.AdoptionsWithFosterStatusVo;

import java.util.List;


public interface AdoptionsService extends IService<Adoptions> {
    /**
     * 创建领养信息
     * @param pid 宠物id
     * @return 领养信息VO
     */
    AdoptionsVo createAdoption(Integer pid);

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
     * 查询用户已通过的领养记录（仅 APPROVED）
     */
    List<AdoptionsVo> getUserApprovedAdoptions(Long userId, Integer currentPage, Integer pageSize);

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
    default IPage<AdoptionsVo> getUserAdoptionsWithPage(Long userId, Integer currentPage, Integer pageSize) {
        return getUserAdoptionsWithPage(userId, currentPage, pageSize, null);
    }

    /**
     * 查询用户领养记录（带分页和总记录数，可选按状态过滤）
     */
    IPage<AdoptionsVo> getUserAdoptionsWithPage(Long userId, Integer currentPage, Integer pageSize, String status);

    /**
     * 查询用户已通过的领养记录（分页）
     */
    IPage<AdoptionsVo> getUserApprovedAdoptionsWithPage(Long userId, Integer currentPage, Integer pageSize);

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
    AdoptionTimelineResponse getAdoptionTimeline(Integer petId, Long userId);
    
    /**
     * 通过宠物ID获取宠物名称
     * @param pid 宠物ID
     * @return 宠物名称
     */
    String getPetNameById(Long pid);

    /**
     * 管理员审核通过领养
     */
    void approveAdoption(Integer adoptionId, String note);
    default void approveAdoption(Integer adoptionId) {
        approveAdoption(adoptionId, null);
    }

    /**
     * 管理员审核拒绝领养
     */
    void rejectAdoption(Integer adoptionId, String note);

    /**
     * 管理员分页查看领养记录
     */
    com.baomidou.mybatisplus.core.metadata.IPage<com.example.petpojo.vo.AdoptionsVo> listAdoptionsForAdmin(Integer currentPage, Integer pageSize, String status);
}
