package com.example.petservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.petpojo.entity.Pets;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.petpojo.vo.AdminPetVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PetsMapper extends BaseMapper<Pets> {
    /**
     * 获取已领养宠物名字列表
     */
    List<String> getAdoptionPetNames();

    IPage<AdminPetVo> selectAdminPets(Page<AdminPetVo> page, @Param("status") String status, @Param("shelterId") Integer shelterId);
}
