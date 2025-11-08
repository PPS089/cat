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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 媒体文件上传下载控制器
 */
@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "媒体文件", description = "媒体文件上传下载相关的 API 接口")
@SecurityRequirement(name = "bearer-key")
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
    @Operation(summary = "上传媒体文件", description = "上传单个或多个媒体文件到事件记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "上传成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<List<MediaFileVo>> uploadMedia(
            @Parameter(description = "要上传的文件数组") @RequestParam("files") MultipartFile[] files,
            @Parameter(description = "记录ID") @RequestParam("recordId") Integer recordId) {
        
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
    @Operation(summary = "删除媒体文件", description = "根据媒体文件ID删除媒体文件")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<String> deleteMedia(
            @Parameter(description = "媒体文件ID") @PathVariable Integer mediaId) {
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
    @Operation(summary = "获取记录的媒体文件列表", description = "根据记录ID获取关联的媒体文件列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<List<MediaFileVo>> getRecordMedia(
            @Parameter(description = "记录ID") @PathVariable Integer recordId) {
        log.info("获取记录媒体文件列表: recordId={}", recordId);
        List<MediaFileVo> mediaFiles = mediaFilesService.getMediaByRecordId(recordId);
        return Result.success(mediaFiles);
    }


}