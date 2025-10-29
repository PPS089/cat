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
     * @param id 用户id
     * @return 寄养信息
     */
    List<FostersVo> getFostersInfo(Integer offset, Integer pageSize, Integer id);
}