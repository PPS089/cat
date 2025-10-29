package com.example.petservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.petpojo.dto.PetUpdateDto;
import com.example.petpojo.entity.Pets;

import java.util.List;

/**
 * 宠物服务接口
 * 定义宠物相关的业务逻辑接口
 */
public interface PetsService extends IService<Pets> {

    /**
     * 开始领养，修改宠物领养状态为已领养
     */

    Pets adop(Long petId);
    /**
     * 更新宠物信息
     */
    Pets update(PetUpdateDto petUpdateDto);

    /**
     * 已领养宠物列表
     */
    List<String> getAdoptedPetsName();

    /**
     * 获取用户已领养的宠物列表
     */
    List<Pets> getUserAdoptedPets();

}