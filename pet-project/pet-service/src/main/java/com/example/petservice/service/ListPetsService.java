package com.example.petservice.service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.petpojo.entity.Pets;
import com.example.petpojo.vo.PetListVo; // 更改导入的VO类
import com.example.petpojo.vo.PetsDetailsVo; // 导入详情VO类

/**
 * 宠物列表服务接口
 * 定义宠物列表相关的业务方法
 */
public interface ListPetsService extends IService<Pets>{

    /**
     * 分页查询宠物列表
     * @param currentPage 当前页码
     * @param pageSize 每页数量
     * @return 宠物列表分页对象
     */
        IPage<PetListVo> listPets(Integer currentPage, Integer pageSize);
        
    /**
     * 根据ID查询宠物详情
     * @param petId 宠物ID
     * @return 宠物详情VO对象
     */
        PetsDetailsVo getPetById(Integer petId);
}