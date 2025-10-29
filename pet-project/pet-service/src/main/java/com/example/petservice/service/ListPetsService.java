package com.example.petservice.service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.petpojo.entity.Pets;
import com.example.petpojo.vo.ListPetsVo;
import org.apache.ibatis.annotations.Param;

public interface ListPetsService extends IService<Pets>{


        IPage<ListPetsVo> listPets(@Param("current_page") Integer currentPage, @Param("per_page") Integer pageSize);
        
        ListPetsVo getPetById(Integer petId);
}
