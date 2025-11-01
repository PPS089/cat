package com.example.petweb.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.petcommon.context.UserContext;
import com.example.petcommon.result.Result;
import com.example.petservice.service.MediaFilesService;
import com.example.petpojo.vo.MediaFileVo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 媒体文件上传下载控制器
 */
@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
@Slf4j
public class MediaFilesController {

    private final MediaFilesService mediaFilesService;

    @Value("${app.upload-dir:./uploads/media}")
    private String uploadDirConfig;
    
    private String uploadDir;

    @PostConstruct
    public void initUploadDir() {
        // 处理路径中的环境变量
        uploadDir = uploadDirConfig.replace("${java.io.tmpdir}", System.getProperty("java.io.tmpdir"));
        log.info("上传目录已初始化: {}", uploadDir);
    }

    @Value("${app.upload-max-size:52428800}")
    private long maxFileSize;

    /**
     * 上传单个或多个媒体文件到事件记录
     */
    @PostMapping("/upload")
    public Result<List<MediaFileVo>> uploadMedia(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("recordId") Integer recordId) {
        
        try {
            Long userId = UserContext.getCurrentUserId();
            log.info("上传媒体文件: 用户ID={}, 记录ID={}, 文件数量={}", userId, recordId, files.length);
            
            if (files.length == 0) {
                return Result.error("请选择要上传的文件");
            }
            
            if (files.length > 5) {
                return Result.error("单次最多上传5个文件");
            }
            
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
                    MediaFileVo mediaFileVo = mediaFilesService.saveMediaFile(
                            recordId,
                            userId.intValue(),
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
                return Result.error("文件上传失败，请检查文件格式和大小");
            }
            
            return Result.success(uploadedFiles);
            
        } catch (Exception e) {
            log.error("媒体文件上传异常", e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载媒体文件
     */
    @GetMapping("/download/{fileName}")
    public byte[] downloadMedia(
            @PathVariable String fileName) throws IOException {
        log.info("下载媒体文件: {}", fileName);
        
        // 安全检查：防止路径遍历攻击
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new IllegalArgumentException("非法文件名");
        }
        
        Path filePath = Paths.get(uploadDir, fileName);
        
        if (!Files.exists(filePath)) {
            log.warn("文件不存在: {}", filePath);
            throw new IOException("文件不存在");
        }
        
        return Files.readAllBytes(filePath);
    }

    /**
     * 删除媒体文件
     */
    @DeleteMapping("/{mediaId}")
    public Result<String> deleteMedia(
            @PathVariable Integer mediaId) {
        try {
            log.info("删除媒体文件: mediaId={}", mediaId);
            boolean result = mediaFilesService.deleteMediaFile(mediaId);
            if (result) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除媒体文件异常", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取记录的媒体文件列表
     */
    @GetMapping("/record/{recordId}")
    public Result<List<MediaFileVo>> getRecordMedia(@PathVariable Integer recordId) {
        log.info("获取记录媒体文件列表: recordId={}", recordId);
        List<MediaFileVo> mediaFiles = mediaFilesService.getMediaByRecordId(recordId);
        return Result.success(mediaFiles);
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