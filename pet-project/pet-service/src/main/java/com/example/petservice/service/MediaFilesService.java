package com.example.petservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.petpojo.entity.MediaFiles;
import com.example.petpojo.vo.MediaFileVo;

import java.util.List;

/**
 * 媒体文件服务接口
 */
public interface MediaFilesService extends IService<MediaFiles> {

    /**
     * 根据记录ID获取媒体文件列表
     */
    List<MediaFileVo> getMediaByRecordId(Integer recordId);
    
    /**
     * 根据用户ID获取媒体文件列表
     */
    List<MediaFileVo> getMediaByUserId(Integer uid);
    
    /**
     * 保存媒体文件
     */
    MediaFileVo saveMediaFile(Integer recordId, Integer uid, String fileName, String filePath, String mediaType, Long fileSize);
    
    /**
     * 删除媒体文件
     */
    boolean deleteMediaFile(Integer mid);
    
    /**
     * 删除记录相关的所有媒体文件
     */
    boolean deleteMediaByRecordId(Integer recordId);
}
