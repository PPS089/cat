package com.example.petservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.petpojo.entity.Adoptions;
import com.example.petpojo.vo.AdoptionsVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AdoptionsMapper extends BaseMapper<Adoptions> {
    /**
     *  分页查询领养记录
     * @param offset 偏移量
     * @param pageSize 每页数量
     * @param id 用户ID
     * @return 领养记录列表
     */
    List<AdoptionsVo> getAdoptionsInfo(Integer offset, Integer pageSize, Integer id);

    /**
     * 查询用户领养记录（带分页）
     * @param userId 用户ID
     * @param offset 偏移量
     * @param pageSize 每页数量
     * @return 用户领养记录列表
     */
    List<AdoptionsVo> getUserAdoptions(Integer userId, Integer offset, Integer pageSize);

    /**
     * 查询用户已通过的领养记录（带分页，适用于“已领养宠物”场景）
     */
    List<AdoptionsVo> getUserApprovedAdoptions(Integer userId, Integer offset, Integer pageSize);

    /**
     * 简化版查询用户领养记录（只关注领养本身，不关联寄养状态）
     * @param userId 用户ID
     * @param offset 偏移量
     * @param pageSize 每页数量
     * @return 用户领养记录列表
     */
    List<AdoptionsVo> getUserAdoptionsSimple(Integer userId, Integer offset, Integer pageSize);

    /**
     * MyBatis Plus分页查询用户领养记录
     * @param page 分页对象
     * @param userId 用户ID
     * @return 分页结果
     */
    IPage<AdoptionsVo> selectUserAdoptionsWithPage(Page<AdoptionsVo> page, Integer userId);

    /**
     * MyBatis Plus分页查询用户已通过的领养记录
     */
    IPage<AdoptionsVo> selectUserApprovedAdoptionsWithPage(Page<AdoptionsVo> page, Integer userId);

    /**
     * 管理员分页查询领养记录（可按状态过滤）
     */
    List<AdoptionsVo> getAdminAdoptions(Integer offset, Integer pageSize, String status, Integer shelterId);

    /**
     * 管理员统计领养记录总数（与 getAdminAdoptions 过滤条件一致）
     */
    Long countAdminAdoptions(String status, Integer shelterId);

}
