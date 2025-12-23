package com.example.petservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.petpojo.entity.Fosters;
import com.example.petpojo.vo.FostersVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FosterMapper extends BaseMapper<Fosters> {
    /**
     * 查询寄养信息
     * @param offset 偏移量
     * @param pageSize 每页数量
     * @param id 用户ID
     * @return 寄养信息列表
     */
    List<FostersVo> getFostersInfo(Integer offset, Integer pageSize, Integer id, String status);
    
    /**
     * 根据寄养ID查询寄养信息
     * @param id 寄养ID
     * @return 寄养信息VO对象
     */
    FostersVo getFosterById(Integer id);

    /**
     * 管理员分页查询寄养记录（可按状态过滤）
     */
    List<FostersVo> getAdminFosters(Integer offset, Integer pageSize, String status, Integer shelterId);
}
