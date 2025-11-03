package com.example.petservice.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.petpojo.entity.MediaFiles;

/**
 * 媒体文件Mapper
 */
@Mapper
public interface MediaFilesMapper extends BaseMapper<MediaFiles> {
    /**
     * 根据记录ID获取媒体文件列表
     */
    List<MediaFiles> getMediaByRecordId(Integer recordId);
    
    /**
     * 根据用户ID获取媒体文件列表
     */
    List<MediaFiles> getMediaByUserId(Integer uid);
}
