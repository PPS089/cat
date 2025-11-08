package com.example.petservice.service;

import java.time.LocalDate;
import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.petpojo.entity.Fosters;
import com.example.petpojo.entity.enums.CommonEnum;
import com.example.petpojo.vo.FostersVo;

public interface FosterService extends IService<Fosters> {
    /**
     * 创建寄养信息（带收容所和开始日期）
     * @param petId 宠物id
     * @param shelterId 收容所id
     * @param startDate 开始日期
     * @return 寄养信息VO对象
     */
    FostersVo createFoster(Long petId, Long shelterId, LocalDate startDate);
    /**
     * 结束寄养（不删除记录，只更新状态和结束日期）
     * @param id 寄养信息id
     * @return 是否结束成功
     */
    boolean endFoster(Long id);
    /**
     * 删除寄养信息
     * @param id 寄养信息id
     * @return 删除结果状态
     */
     CommonEnum.FosterDeleteResultEnum deleteFoster(Integer id);
     /**
      * 查询寄养信息
      * @return 寄养信息
      */
     List<FostersVo> fosterInfo(Integer currentPage, Integer pageSize);
     
     /**
      * 查询用户寄养记录（带分页和总记录数）
      * @param userId 用户ID
      * @param currentPage 当前页码
      * @param pageSize 每页数量
      * @return 寄养记录分页对象
      */
     IPage<FostersVo> getUserFostersWithPage(Long userId, Integer currentPage, Integer pageSize);

     /**
      * 根据宠物ID结束寄养
      * @param petId 宠物ID
      * @return 是否结束成功
      */
     boolean endFosterByPetId(Long petId);

}