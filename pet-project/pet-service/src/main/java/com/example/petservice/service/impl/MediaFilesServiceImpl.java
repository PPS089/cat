package com.example.petservice.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petcommon.context.UserContext;
import com.example.petpojo.entity.MediaFiles;
import com.example.petpojo.entity.PetRecords;
import com.example.petpojo.vo.MediaFileVo;
import com.example.petservice.mapper.MediaFilesMapper;
import com.example.petservice.mapper.PetRecordsMapper;
import com.example.petservice.service.MediaFilesService;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * 媒体文件服务实现
 */
@Service
@Slf4j
public class MediaFilesServiceImpl extends ServiceImpl<MediaFilesMapper, MediaFiles> implements MediaFilesService {

    private final MediaFilesMapper mediaFilesMapper;
    private final PetRecordsMapper petRecordsMapper;

    @Value("${app.upload-dir:./uploads/media}")
    private String uploadDirConfig;
    
    private String uploadDir;

    @Value("${app.upload-max-size:52428800}")
    private long maxFileSize;

    @PostConstruct
    public void initUploadDir() {
        // 处理路径中的环境变量
        uploadDir = uploadDirConfig.replace("${java.io.tmpdir}", System.getProperty("java.io.tmpdir"));
        log.info("上传目录已初始化: {}", uploadDir);
    }

    public MediaFilesServiceImpl(MediaFilesMapper mediaFilesMapper, PetRecordsMapper petRecordsMapper) {
        this.mediaFilesMapper = mediaFilesMapper;
        this.petRecordsMapper = petRecordsMapper;
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
    public MediaFileVo saveMediaFile(Integer recordId, @Deprecated Integer uid, String fileName, String filePath, String mediaType, Long fileSize) {
        log.info("保存媒体文件: recordId={}, uid={}, fileName={}", recordId, uid, fileName);
        
        // 从UserContext获取当前用户ID，如果为null则使用传入的uid参数
        Long userId = UserContext.getCurrentUserId();
        Integer currentUserId = (userId != null) ? userId.intValue() : uid;
        
        // 创建MediaFiles对象，显式设置uid
        MediaFiles mediaFiles = MediaFiles.builder()
                .recordId(recordId)
                .uid(currentUserId)
                .fileName(fileName)
                .filePath(filePath)
                .mediaType(mediaType)
                .fileSize(fileSize)
                .build();
        
        this.save(mediaFiles);
        log.info("媒体文件保存成功，文件ID: {}, uid: {}", mediaFiles.getMid(), mediaFiles.getUid());
        
        return convertToVo(mediaFiles);
    }

    @Override
    public List<MediaFileVo> uploadMediaFiles(Integer recordId, MultipartFile[] files) {
        log.info("上传媒体文件: 记录ID={}, 文件数量={}", recordId, files.length);
        
        if (files.length == 0) {
            throw new IllegalArgumentException("请选择要上传的文件");
        }
        
        if (files.length > 5) {
            throw new IllegalArgumentException("单次最多上传5个文件");
        }
        
        // 从UserContext获取当前用户ID
        Long userId = UserContext.getCurrentUserId();
        log.info("当前用户ID: {}", userId);
        
        // 创建上传目录
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }
        
        List<MediaFileVo> uploadedFiles = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                log.warn("跳过空文件");
                continue;
            }
            
            String originalFilename = file.getOriginalFilename();
            String contentType = file.getContentType();
            long fileSize = file.getSize();
            
            log.info("处理文件: {}, ContentType: {}, Size: {} bytes", originalFilename, contentType, fileSize);
            
            // 验证文件大小
            if (fileSize > maxFileSize) {
                log.warn("文件过大: {} ({}MB, 限制{}MB)", originalFilename, fileSize / 1024 / 1024, maxFileSize / 1024 / 1024);
                continue;
            }
            
            // 验证文件类型
            if (!isValidMediaType(contentType)) {
                log.warn("不支持的文件类型: {} (ContentType: {})", originalFilename, contentType);
                continue;
            }
            
            try {
                // 生成唯一文件名
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String savedFileName = UUID.randomUUID().toString() + fileExtension;
                
                // 保存文件
                Path filePath = Paths.get(uploadDir, savedFileName);
                file.transferTo(filePath.toFile());
                
                // 验证文件是否保存成功
                if (!Files.exists(filePath)) {
                    log.error("文件保存失败: {}", savedFileName);
                    continue;
                }
                
                // 确定媒体类型
                String mediaType = getMediaType(contentType);
                
                // 保存到数据库
                String filePathStr = "/media/download/" + savedFileName;  // 不含 /api 前缀，上传的是相对于 /api 的路径
                MediaFileVo mediaFileVo = saveMediaFile(
                        recordId,
                        userId != null ? userId.intValue() : null,  // 传递当前用户ID，如果为null则传递null
                        originalFilename,
                        filePathStr,
                        mediaType,
                        file.getSize()
                );
                
                uploadedFiles.add(mediaFileVo);
                log.info("文件上传成功: {} -> {}", originalFilename, savedFileName);
                
            } catch (IOException e) {
                log.error("保存文件失败: {}", file.getOriginalFilename(), e);
            }
        }
        
        if (uploadedFiles.isEmpty()) {
            throw new RuntimeException("文件上传失败，请检查文件格式和大小");
        }
        
        // 如果有文件上传成功，更新关联记录的更新时间
        if (!uploadedFiles.isEmpty()) {
            try {
                // 获取关联的记录信息
                PetRecords petRecords = petRecordsMapper.selectById(recordId);
                if (petRecords != null) {
                    // 手动设置更新时间，确保updatedAt字段被更新
                    petRecords.setUpdatedAt(LocalDateTime.now());
                    petRecordsMapper.updateById(petRecords);
                    log.info("批量删除媒体文件成功，更新关联记录，记录ID: {}", recordId);
                }
            } catch (Exception e) {
                log.warn("更新关联记录失败，记录ID: {}", recordId, e);
                // 不影响文件上传的主要功能，只记录警告
            }
        }
        
        return uploadedFiles;
    }

    @Override
    public boolean deleteMediaFile(Integer mid) {
        log.info("删除媒体文件: mid={}", mid);
        
        // 获取媒体文件信息
        MediaFiles mediaFile = this.getById(mid);
        if (mediaFile != null) {
            boolean result = this.removeById(mid);
            if (result) {
                // 删除成功后，更新关联记录，让MyBatis Plus自动填充updatedAt
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

    @Override
    public boolean deleteMediaByRecordId(Integer recordId) {
        log.info("删除记录 {} 的所有媒体文件", recordId);
        LambdaQueryWrapper<MediaFiles> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MediaFiles::getRecordId, recordId);
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

    private MediaFileVo convertToVo(MediaFiles mediaFiles) {
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