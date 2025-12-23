package com.example.petservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.petpojo.dto.PetUpdateDto;
import com.example.petpojo.entity.Pets;
import com.example.petpojo.vo.AdoptionResultVo;
import com.example.petpojo.vo.PetListVo;

/**
 * 宠物服务接口
 * 定义宠物相关的业务逻辑接口
 */
public interface PetsService extends IService<Pets> {

    /**
     * 开始领养，修改宠物领养状态为已领养
     */
    AdoptionResultVo adop(Long petId);
    /**
     * 更新宠物信息
     */
    PetListVo update(PetUpdateDto petUpdateDto);

    /**
     * 管理员创建宠物
     */
    PetListVo createPet(com.example.petpojo.dto.PetCreateDto petCreateDto);

    /**
     * 管理员删除宠物（仅未被领养/寄养）
     */
    void deletePet(Integer petId);

    /**
     * 管理员更新宠物状态
     */
    PetListVo updatePetStatus(Integer petId, String status);

    /**
     * 管理员分页查看宠物列表
     */
    com.baomidou.mybatisplus.core.metadata.IPage<com.example.petpojo.vo.AdminPetVo> listPetsForAdmin(Integer currentPage, Integer pageSize, String status);
}
