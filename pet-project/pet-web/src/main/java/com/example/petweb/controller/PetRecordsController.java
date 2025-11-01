package com.example.petweb.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.example.petcommon.context.UserContext;
import com.example.petcommon.result.Result;
import com.example.petpojo.dto.RecordDto;
import com.example.petpojo.entity.PetRecords;
import com.example.petservice.service.PetRecordsService;
import com.example.petpojo.vo.EventVo;
import com.example.petservice.service.MediaFilesService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 宠物事件记录控制器
 * 提供宠物事件记录的 REST API 接口
 */
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class PetRecordsController {

    private final PetRecordsService petRecordsService;
    private final MediaFilesService mediaFilesService;


        /**
     * 将 PetRecords 转换为 EventVo
     */
    private EventVo convertToEventVo(PetRecords petRecords) {
        EventVo eventVo = new EventVo();
        eventVo.setEid(petRecords.getRecordId());
        eventVo.setPid(petRecords.getPid());
        eventVo.setUid(petRecords.getUid());
        eventVo.setEventType(petRecords.getEventType());
        eventVo.setEventTime(petRecords.getRecordTime());
        eventVo.setMood(petRecords.getMood());
        eventVo.setDescription(petRecords.getDescription());
        eventVo.setLocation(petRecords.getLocation());
        eventVo.setMediaUrl(petRecords.getMediaUrl());
        eventVo.setMediaType(petRecords.getMediaType());
        eventVo.setCreatedAt(petRecords.getCreatedAt());
        eventVo.setUpdatedAt(petRecords.getUpdatedAt());
        
        // 加载该记录的媒体文件
        List<com.example.petpojo.vo.MediaFileVo> mediaList = mediaFilesService.getMediaByRecordId(petRecords.getRecordId());
        eventVo.setMediaList(mediaList);
        
        return eventVo;
    }

    /**
     * 获取用户的事件记录列表
     */
    @GetMapping
    public Result<List<EventVo>> getUserEvents() {
        
        Long userId = UserContext.getCurrentUserId();
        log.info("获取用户事件记录，用户ID: {}", userId);
        
        // 获取用户的事件记录
        List<PetRecords> records = petRecordsService.getUserRecords(userId.intValue());
        
        // 使用 BeanUtils 转换为 VO
        List<EventVo> eventVos = records.stream()
                .map(record -> convertToEventVo(record))
                .toList();
        
        return Result.success(eventVos);
    }


    /**
     * 创建事件记录
     */
    @PostMapping
    public Result<EventVo> createEvent(@Valid @RequestBody RecordDto recordDto) {
        log.info("创建事件记录，用户ID: {}, 宠物ID: {}", recordDto.getUid(), recordDto.getPid());
        
        PetRecords petRecords = petRecordsService.createRecord(recordDto);
        EventVo eventVo = convertToEventVo(petRecords);
        
        return Result.success(eventVo);
    }

    /**
     * 更新事件记录
     */
    @PutMapping("/{eventId}")
    public Result<EventVo> updateEvent(
            @PathVariable Integer eventId,
            @Valid @RequestBody RecordDto recordDto) {
        
        log.info("更新事件记录，记录ID: {}", eventId);
        
        PetRecords petRecords = petRecordsService.updateRecord(eventId, recordDto);
        EventVo eventVo = convertToEventVo(petRecords);
        
        return Result.success(eventVo);
    }

    /**
     * 删除事件记录
     */
    @DeleteMapping("/{eventId}")
    public Result<String> deleteEvent(
            @PathVariable Integer eventId) {
        log.info("删除事件记录，记录ID: {}", eventId);
        
        boolean result = petRecordsService.deleteRecord(eventId);
        
        if (result) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }


}