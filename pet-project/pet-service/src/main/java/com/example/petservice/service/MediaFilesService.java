package com.example.petservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.petpojo.entity.MediaFiles;
import com.example.petpojo.vo.MediaFileVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 媒体文件服务接口
 * 定义媒体文件相关的业务方法
 */
public interface MediaFilesService extends IService<MediaFiles> {

    /**
     * 根据记录ID获取媒体文件列表
     * @param recordId 记录ID
     * @return 媒体文件VO列表
     */
    List<MediaFileVo> getMediaByRecordId(Integer recordId);
    
    /**
     * 根据用户ID获取媒体文件列表
     * @param uid 用户ID
     * @return 媒体文件VO列表
     */
    List<MediaFileVo> getMediaByUserId(Integer uid);
    
    /**
     * 保存媒体文件
     * @param recordId 记录ID
     * @param fileName 文件名
     * @param filePath 文件路径
     * @param mediaType 媒体类型
     * @param fileSize 文件大小
     * @return 媒体文件信息
     */
    MediaFileVo saveMediaFile(Integer recordId, String fileName, String filePath, String mediaType, Long fileSize);
    
    /**
     * 上传媒体文件
     * @param recordId 记录ID
     * @param files 要上传的文件数组
     * @return 上传成功的媒体文件列表
     */
    List<MediaFileVo> uploadMediaFiles(Integer recordId, MultipartFile[] files);
    
    /**
     * 删除媒体文件
     * @param mid 媒体文件ID
     * @return 是否删除成功
     */
    boolean deleteMediaFile(Integer mid);
    
    /**
     * 删除记录相关的所有媒体文件
     * @param recordId 记录ID
     * @return 是否删除成功
     */
    boolean deleteMediaByRecordId(Integer recordId);

    /**
     * 通用文件上传（不绑定记录），返回 OSS URL
     * 供后台管理等场景使用
     */
    String uploadRawFile(MultipartFile file);

    /**
     * 通用删除（按完整 OSS URL 删除），供替换封面时清理旧图
     */
    void deleteByUrl(String url);
}
