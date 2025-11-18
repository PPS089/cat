package com.example.petweb.controller;

import java.util.List;

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


    /**
     * 上传单个或多个媒体文件到事件记录
     */
    @PostMapping("/upload")
    @Operation(summary = "上传媒体文件", description = "上传单个或多个媒体文件到事件记录")
    public Result<List<MediaFileVo>> uploadMedia(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("recordId") Integer recordId) {
        List<MediaFileVo> uploadedFiles = mediaFilesService.uploadMediaFiles(recordId, files);
        return Result.success(uploadedFiles);
    }



    /**
     * 删除媒体文件
     */
    @DeleteMapping("/{mediaId}")
    @Operation(summary = "删除媒体文件", description = "根据媒体文件ID删除媒体文件")
    public Result<String> deleteMedia(
            @PathVariable Integer mediaId) {
        boolean result = mediaFilesService.deleteMediaFile(mediaId);
        if (!result) {
            throw new BizException(ErrorCode.BAD_REQUEST, "删除失败");
        }
        return Result.success("删除成功");
    }

    /**
     * 获取记录的媒体文件列表
     */
    @GetMapping("/record/{recordId}")
    @Operation(summary = "获取记录的媒体文件列表", description = "根据记录ID获取关联的媒体文件列表")
    public Result<List<MediaFileVo>> getRecordMedia(
            @PathVariable Integer recordId) {
        log.info("获取记录媒体文件列表: recordId={}", recordId);
        List<MediaFileVo> mediaFiles = mediaFilesService.getMediaByRecordId(recordId);
        return Result.success(mediaFiles);
    }


}
