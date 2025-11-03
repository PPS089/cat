package com.example.petweb.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.example.petcommon.context.UserContext;
import com.example.petcommon.result.Result;
import com.example.petpojo.dto.RecordDto;
import com.example.petpojo.entity.PetRecords;
import com.example.petpojo.vo.EventVo;
import com.example.petservice.service.PetRecordsService;

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

    /**
     * 获取用户的事件记录列表
     */
    @GetMapping
    public Result<List<EventVo>> getUserEvents() {
        
        Long userId = UserContext.getCurrentUserId();
        log.info("获取用户事件记录，用户ID: {}", userId);
        
        // 直接获取EventVo列表，避免在Controller中进行转换
        List<EventVo> eventVos = petRecordsService.getUserEventVos(userId.intValue());
        
        return Result.success(eventVos);
    }

    /**
     * 创建事件记录
     */
    @PostMapping
    public Result<EventVo> createEvent(@Valid @RequestBody RecordDto recordDto) {
        log.info("创建事件记录，用户ID: {}, 宠物ID: {}", recordDto.getUid(), recordDto.getPid());
        
        PetRecords petRecords = petRecordsService.createRecord(recordDto);
        // 使用新的方法获取EventVo
        EventVo eventVo = petRecordsService.getEventVoById(petRecords.getRecordId());
        
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
        // 使用新的方法获取EventVo
        EventVo eventVo = petRecordsService.getEventVoById(petRecords.getRecordId());
        
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