package com.example.petservice.service.impl;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petpojo.entity.MediaFiles;
import com.example.petservice.mapper.MediaFilesMapper;
import com.example.petservice.service.MediaFilesService;
import com.example.petpojo.vo.MediaFileVo;

import lombok.extern.slf4j.Slf4j;

/**
 * 媒体文件服务实现
 */
@Service
@Slf4j
public class MediaFilesServiceImpl extends ServiceImpl<MediaFilesMapper, MediaFiles> implements MediaFilesService {

    private final MediaFilesMapper mediaFilesMapper;

    public MediaFilesServiceImpl(MediaFilesMapper mediaFilesMapper) {
        this.mediaFilesMapper = mediaFilesMapper;
    }

    @Override
    public List<MediaFileVo> getMediaByRecordId(Integer recordId) {
        log.info("获取记录 {} 的媒体文件列表", recordId);
        List<MediaFiles> mediaFiles = mediaFilesMapper.getMediaByRecordId(recordId);
        return mediaFiles.stream().map(this::convertToVo).toList();
    }

    @Override
    public List<MediaFileVo> getMediaByUserId(Integer uid) {
        log.info("获取用户 {} 的媒体文件列表", uid);
        List<MediaFiles> mediaFiles = mediaFilesMapper.getMediaByUserId(uid);
        return mediaFiles.stream().map(this::convertToVo).toList();
    }

    @Override
    public MediaFileVo saveMediaFile(Integer recordId, Integer uid, String fileName, String filePath, String mediaType, Long fileSize) {
        log.info("保存媒体文件: recordId={}, fileName={}", recordId, fileName);
        
        MediaFiles mediaFiles = MediaFiles.builder()
                .recordId(recordId)
                .uid(uid)
                .fileName(fileName)
                .filePath(filePath)
                .mediaType(mediaType)
                .fileSize(fileSize)
                .build();
        
        this.save(mediaFiles);
        log.info("媒体文件保存成功，文件ID: {}", mediaFiles.getMid());
        
        return convertToVo(mediaFiles);
    }

    @Override
    public boolean deleteMediaFile(Integer mid) {
        log.info("删除媒体文件: mid={}", mid);
        return this.removeById(mid);
    }

    @Override
    public boolean deleteMediaByRecordId(Integer recordId) {
        log.info("删除记录 {} 的所有媒体文件", recordId);
        LambdaQueryWrapper<MediaFiles> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MediaFiles::getRecordId, recordId);
        return this.remove(wrapper);
    }

    private MediaFileVo convertToVo(MediaFiles mediaFiles) {
        MediaFileVo vo = new MediaFileVo();
        BeanUtils.copyProperties(mediaFiles, vo);
        return vo;
    }
}