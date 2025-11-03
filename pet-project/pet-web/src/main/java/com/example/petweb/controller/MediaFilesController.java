package com.example.petweb.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.petcommon.result.Result;
import com.example.petpojo.vo.MediaFileVo;
import com.example.petservice.service.MediaFilesService;

import jakarta.annotation.PostConstruct;
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


    /**
     * 上传单个或多个媒体文件到事件记录
     */
    @PostMapping("/upload")
    public Result<List<MediaFileVo>> uploadMedia(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("recordId") Integer recordId) {
        
        try {
            List<MediaFileVo> uploadedFiles = mediaFilesService.uploadMediaFiles(recordId, files);
            log.info("媒体文件上传成功: 共{}个文件", uploadedFiles.size());
            return Result.success(uploadedFiles);
        } catch (IllegalArgumentException e) {
            log.warn("媒体文件上传参数错误: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (RuntimeException e) {
            log.error("媒体文件上传失败: {}", e.getMessage());
            return Result.error("上传失败: " + e.getMessage());
        }
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


}