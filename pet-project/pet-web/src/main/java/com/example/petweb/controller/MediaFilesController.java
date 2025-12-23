package com.example.petweb.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.petcommon.result.Result;
import com.example.petcommon.exception.BizException;
import com.example.petcommon.error.ErrorCode;
import com.example.petpojo.vo.MediaFileVo;
import com.example.petservice.service.MediaFilesService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 媒体文件上传下载控制器
 * @author 33185
 */
@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "媒体文件", description = "媒体文件上传下载相关的 API 接口")
@SecurityRequirement(name = "bearer-key")
@Validated
public class MediaFilesController {

    private final MediaFilesService mediaFilesService;


    /**
     * 上传单个或多个媒体文件到事件记录
     */
    @PostMapping("/upload")
    @Operation(summary = "上传媒体文件", description = "上传单个或多个媒体文件到事件记录")
    public Result<List<MediaFileVo>> uploadMedia(
            @RequestParam("files") @NotEmpty(message = "文件列表不能为空") MultipartFile[] files,
            @RequestParam("recordId") @NotNull(message = "recordId 不能为空") @Positive(message = "recordId 必须为正数") Integer recordId) {
        List<MediaFileVo> uploadedFiles = mediaFilesService.uploadMediaFiles(recordId, files);
        return Result.success(uploadedFiles);
    }

    /**
     * 通用上传（不绑定记录），返回 OSS URL
     */
    @PostMapping("/upload/raw")
    @Operation(summary = "通用上传文件到OSS", description = "不绑定宠物事件记录，直接返回 OSS URL，供管理端封面/图片上传使用")
    public Result<String> uploadRaw(@RequestParam("file") @NotNull(message = "文件不能为空") MultipartFile file) {
        String url = mediaFilesService.uploadRawFile(file);
        return Result.success(url);
    }


    /**
     * 删除媒体文件
     */
    @DeleteMapping("/{mediaId}")
    @Operation(summary = "删除媒体文件", description = "根据媒体文件ID删除媒体文件")
    public Result<String> deleteMedia(
            @PathVariable @NotNull(message = "媒体文件ID不能为空") @Positive(message = "媒体文件ID必须为正数") Integer mediaId) {
        boolean result = mediaFilesService.deleteMediaFile(mediaId);
        if (!result) {
            throw new BizException(ErrorCode.BAD_REQUEST, "删除失败");
        }
        return Result.success("删除成功");
    }

    /**
     * 按 URL 删除 OSS 文件（无记录场景下的替换/清理）
     */
    @DeleteMapping("/raw")
    @Operation(summary = "按URL删除文件", description = "删除不绑定记录的 OSS 文件，用于替换封面后清理旧图")
    public Result<String> deleteRaw(@RequestParam("url") @NotBlank(message = "url 不能为空") @Size(max = 1024, message = "url 长度不能超过1024字符") String url) {
        mediaFilesService.deleteByUrl(url);
        return Result.success("删除成功");
    }

    /**
     * 获取记录的媒体文件列表
     */
    @GetMapping("/record/{recordId}")
    @Operation(summary = "获取记录的媒体文件列表", description = "根据记录ID获取关联的媒体文件列表")
    public Result<List<MediaFileVo>> getRecordMedia(
            @PathVariable @NotNull(message = "记录ID不能为空") @Positive(message = "记录ID必须为正数") Integer recordId) {
        log.info("获取记录媒体文件列表: recordId={}", recordId);
        List<MediaFileVo> mediaFiles = mediaFilesService.getMediaByRecordId(recordId);
        return Result.success(mediaFiles);
    }


}
