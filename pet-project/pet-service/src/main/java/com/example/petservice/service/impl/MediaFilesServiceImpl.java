package com.example.petservice.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petcommon.context.UserContext;
import com.example.petcommon.exception.BizException;
import com.example.petcommon.error.ErrorCode;
import com.example.petpojo.entity.MediaFiles;
import com.example.petpojo.entity.PetRecords;
import com.example.petpojo.vo.MediaFileVo;
import com.example.petservice.mapper.MediaFilesMapper;
import com.example.petservice.mapper.PetRecordsMapper;
import com.example.petservice.service.MediaFilesService;
import com.example.petservice.config.AliyunOSSOperator;
import org.springframework.lang.NonNull;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 媒体文件服务实现
 * 实现媒体文件的上传、下载、删除等业务逻辑
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MediaFilesServiceImpl extends ServiceImpl<MediaFilesMapper, MediaFiles> implements MediaFilesService {

    private final MediaFilesMapper mediaFilesMapper;
    private final PetRecordsMapper petRecordsMapper;
    private final AliyunOSSOperator ossOperator;
    
    @Value("${app.upload-max-size:524288500}")
    private long maxFileSize;

    /**
     * 根据记录ID获取媒体文件列表
     * @param recordId 记录ID
     * @return 媒体文件VO列表
     */
    @Override
    public List<MediaFileVo> getMediaByRecordId(Integer recordId) {
        log.info("获取记录 {} 的媒体文件列表", recordId);
        List<MediaFiles> mediaFiles = mediaFilesMapper.getMediaByRecordId(recordId);
        return mediaFiles.stream().map(this::convertToVo).toList();
    }

    /**
     * 根据用户ID获取媒体文件列表
     * @param uid 用户ID
     * @return 媒体文件VO列表
     */
    @Override
    public List<MediaFileVo> getMediaByUserId(Integer uid) {
        log.info("获取用户 {} 的媒体文件列表", uid);
        List<MediaFiles> mediaFiles = mediaFilesMapper.getMediaByUserId(uid);
        return mediaFiles.stream().map(this::convertToVo).toList();
    }

    /**
     * 保存媒体文件
     * @param recordId 记录ID
     * @param fileName 文件名
     * @param filePath 文件路径（OSS URL）
     * @param mediaType 媒体类型
     * @param fileSize 文件大小
     * @return 媒体文件VO对象
     */
    @Override
    public MediaFileVo saveMediaFile(Integer recordId, String fileName, String filePath, String mediaType, Long fileSize) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        
        log.info("保存媒体文件: recordId={}, userId={}, fileName={}", recordId, userId, fileName);
        
        // 创建MediaFiles对象
        MediaFiles mediaFiles = MediaFiles.builder()
                .recordId(recordId)
                .uid(userId.intValue())
                .fileName(fileName)
                .filePath(filePath)
                .mediaType(mediaType)
                .fileSize(fileSize)
                .build();
        
        this.save(mediaFiles);
        log.info("媒体文件保存成功，文件ID: {}, uid: {}", mediaFiles.getMid(), mediaFiles.getUid());
        
        return convertToVo(mediaFiles);
    }

    /**
     * 上传媒体文件到OSS
     * @param recordId 记录ID
     * @param files 要上传的文件数组
     * @return 上传成功的媒体文件VO列表
     */
    @Override
    public List<MediaFileVo> uploadMediaFiles(Integer recordId, MultipartFile[] files) {
        log.info("上传媒体文件到OSS: 记录ID={}, 文件数量={}", recordId, files.length);
        
        validateUploadRequest(files);
        
        Long userId = UserContext.getCurrentUserId();
        log.info("当前用户ID: {}", userId);
        
        List<MediaFileVo> uploadedFiles = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                log.warn("跳过空文件");
                continue;
            }
            
            MediaFileVo uploadedFile = processSingleFile(recordId,file);
            if (uploadedFile != null) {
                uploadedFiles.add(uploadedFile);
            }
        }
        
        if (uploadedFiles.isEmpty()) {
            throw new BizException(ErrorCode.MEDIA_FILE_UPLOAD_FAILED);
        }
        
        updateRelatedRecord(recordId);
        
        log.info("文件上传完成: 成功上传{}个文件", uploadedFiles.size());
        return uploadedFiles;
    }
    
    /**
     * 验证上传请求
     */
    private void validateUploadRequest(MultipartFile[] files) {
        if (files.length == 0) {
            log.warn("没有文件需要上传");
            throw new BizException(ErrorCode.VALIDATION_ERROR);
        }
        
        if (files.length > 5) {
            log.warn("上传文件数量超过限制: {}", files.length);
            throw new BizException(ErrorCode.MEDIA_FILE_COUNT_EXCEEDED);
        }
    }
    
    /**
     * 处理单个文件上传
     */
    private MediaFileVo processSingleFile(Integer recordId,MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        long fileSize = file.getSize();
        
        log.info("处理文件: {}, ContentType: {}, Size: {} bytes", originalFilename, contentType, fileSize);
        
        validateFile(originalFilename, contentType, fileSize);
        
        try {
            String fileUrl = uploadToOss(file, originalFilename);
            String mediaType = getMediaType(contentType);
            
            return saveMediaFile(
                    recordId,
                    originalFilename,
                    fileUrl,
                    mediaType,
                    file.getSize()
            );
        } catch (Exception e) {
            log.error("文件上传到OSS失败: {}", e.getMessage(), e);
            throw new BizException(ErrorCode.MEDIA_FILE_UPLOAD_FAILED);
        }
    }
    
    /**
     * 验证文件
     */
    private void validateFile(String originalFilename, String contentType, long fileSize) {
        if (fileSize > maxFileSize) {
            log.warn("文件过大: {} ({}MB, 限制{}MB)", 
                originalFilename, 
                String.format("%.2f", fileSize / 1024.0 / 1024.0), 
                String.format("%.2f", maxFileSize / 1024.0 / 1024.0));
            throw new BizException(ErrorCode.MEDIA_FILE_SIZE_EXCEEDED);
        }
        
        if (!isValidMediaType(contentType)) {
            log.warn("不支持的文件类型: {} (ContentType: {})", originalFilename, contentType);
            throw new BizException(ErrorCode.MEDIA_FILE_TYPE_UNSUPPORTED);
        }
    }
    
    /**
     * 上传文件到OSS
     */
    private String uploadToOss(MultipartFile file, String originalFilename) throws Exception {
        log.info("开始上传文件到OSS: {}", originalFilename);
        String fileUrl = ossOperator.upload(file.getBytes(), originalFilename);
        log.info("文件上传OSS成功: {} -> {}", originalFilename, fileUrl);
        return fileUrl;
    }
    
    /**
     * 更新关联记录
     */
    private void updateRelatedRecord(Integer recordId) {
        try {
            PetRecords petRecords = petRecordsMapper.selectById(recordId);
            if (petRecords != null) {
                petRecords.setUpdatedAt(LocalDateTime.now());
                petRecordsMapper.updateById(petRecords);
                log.info("文件上传成功，更新关联记录，记录ID: {}", recordId);
            }
        } catch (Exception e) {
            log.warn("更新关联记录失败，记录ID: {}", recordId, e);
        }
    }

    /**
     * 删除OSS上的媒体文件
     * @param mid 媒体文件ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteMediaFile(Integer mid) {
        log.info("删除OSS上的媒体文件: mid={}", mid);
        
        // 获取媒体文件信息
        MediaFiles mediaFile = this.getById(mid);
        if (mediaFile != null) {
            try {
                // 直接使用文件URL删除OSS上的文件
                String fileUrl = mediaFile.getFilePath();
                if (fileUrl != null && (fileUrl.startsWith("https://") || fileUrl.startsWith("http://"))) {
                    ossOperator.delete(fileUrl);
                    log.info("成功删除OSS上的媒体文件: {}", fileUrl);
                }
            } catch (Exception e) {
                log.warn("删除OSS文件失败: {}", e.getMessage(), e);
                throw new BizException(ErrorCode.MEDIA_FILE_DELETE_FAILED);
            }
            
            boolean result = this.removeById(mid);
            if (result) {
                // 删除成功后，更新关联记录
                try {
                    PetRecords petRecords = petRecordsMapper.selectById(mediaFile.getRecordId());
                    if (petRecords != null) {
                        // 手动设置更新时间，确保updatedAt字段被更新
                        petRecords.setUpdatedAt(LocalDateTime.now());
                        petRecordsMapper.updateById(petRecords);
                        log.info("媒体文件删除成功，更新关联记录，记录ID: {}", 
                                mediaFile.getRecordId());
                    }
                } catch (Exception e) {
                    log.warn("更新关联记录失败，记录ID: {}", mediaFile.getRecordId(), e);
                }
            }
            return result;
        }
        
        return false;
    }

    /**
     * 删除记录相关的所有OSS媒体文件
     * @param recordId 记录ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteMediaByRecordId(Integer recordId) {
        log.info("删除记录 {} 的所有OSS媒体文件", recordId);
        
        // 先获取所有要删除的文件，以便删除OSS上的文件
        LambdaQueryWrapper<MediaFiles> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MediaFiles::getRecordId, recordId);
        List<MediaFiles> mediaFiles = this.list(wrapper);
        
        // 删除OSS上的文件
        for (MediaFiles mediaFile : mediaFiles) {
            try {
                // 直接使用文件URL删除OSS上的文件
                String fileUrl = mediaFile.getFilePath();
                if (fileUrl != null && (fileUrl.startsWith("https://") || fileUrl.startsWith("http://"))) {
                    ossOperator.delete(fileUrl);
                    log.info("成功删除OSS上的媒体文件: {}", fileUrl);
                }
            } catch (Exception e) {
                log.warn("删除OSS文件失败: {}", e.getMessage(), e);
                throw new BizException(ErrorCode.MEDIA_FILE_DELETE_FAILED);
            }
        }
        
        // 从数据库中删除记录
        boolean result = this.remove(wrapper);
        
        if (result) {
            // 批量删除成功后，更新关联记录，让MyBatis Plus自动填充updatedAt
            try {
                PetRecords petRecords = petRecordsMapper.selectById(recordId);
                if (petRecords != null) {
                    petRecords.setDescription(petRecords.getDescription()); // 设置相同值触发更新
                    petRecordsMapper.updateById(petRecords);
                    log.info("批量删除媒体文件成功，更新关联记录，记录ID: {}", recordId);
                }
            } catch (Exception e) {
                log.warn("更新关联记录失败，记录ID: {}", recordId, e);
            }
        }
        
        return result;
    }

    private MediaFileVo convertToVo(@NonNull MediaFiles mediaFiles) {
        MediaFileVo vo = new MediaFileVo();
        BeanUtils.copyProperties(mediaFiles, vo);
        return vo;
    }

    /**
     * 验证是否为有效的媒体类型
     */
    private boolean isValidMediaType(String contentType) {
        if (contentType == null) {
            return false;
        }
        return contentType.startsWith("image/") || contentType.startsWith("video/");
    }

    /**
     * 获取媒体类型
     */
    private String getMediaType(String contentType) {
        if (contentType != null && contentType.startsWith("video/")) {
            return "video";
        }
        return "image";
    }
}