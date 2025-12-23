package com.example.petweb.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.petcommon.context.UserContext;
import com.example.petcommon.result.Result;
import com.example.petpojo.dto.RecordDto;
import com.example.petpojo.vo.EventVo;
import com.example.petservice.service.PetRecordsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 宠物事件记录控制器
 * 提供宠物事件记录的 REST API 接口
 * @author 33185
 */
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "宠物事件记录", description = "宠物事件记录相关的 API 接口")
@SecurityRequirement(name = "bearer-key")
@Validated
public class PetRecordsController {

    private final PetRecordsService petRecordsService;

    /**
     * 获取用户的事件记录列表
     */
    @GetMapping
    @Operation(summary = "获取用户的事件记录列表", description = "获取当前登录用户的所有宠物事件记录列表")
    public Result<List<EventVo>> getUserEvents() {
        
        Long userId = UserContext.getCurrentUserId();
        // 直接获取EventVo列表
        List<EventVo> eventVos = petRecordsService.getUserEventVos(userId.intValue());
        
        return Result.success(eventVos);
    }

    /**
     * 创建事件记录
     */
    @PostMapping
    @Operation(summary = "创建事件记录", description = "创建一个新的宠物事件记录")
    public Result<EventVo> createEvent(
            @Valid @RequestBody RecordDto recordDto) {
        Long userId = UserContext.getCurrentUserId();
        log.info("创建事件记录，用户ID: {}, 宠物ID: {}", userId, recordDto.getPid());
        EventVo eventVo = petRecordsService.createRecord(recordDto);
        return Result.success(eventVo);
    }

    /**
     * 更新事件记录
     */
    @PutMapping("/{eventId}")
    @Operation(summary = "更新事件记录", description = "更新指定ID的宠物事件记录")
    public Result<EventVo> updateEvent(
            @PathVariable @Positive(message = "事件ID必须为正数") Integer eventId,
            @Valid @RequestBody RecordDto recordDto) {
        
        log.info("更新事件记录，记录ID: {}", eventId);
        EventVo eventVo = petRecordsService.updateRecord(eventId, recordDto);
        return Result.success(eventVo);
    }

    /**
     * 删除事件记录
     */
    @DeleteMapping("/{eventId}")
    @Operation(summary = "删除事件记录", description = "删除指定ID的宠物事件记录")
    public Result<String> deleteEvent(
            @PathVariable @Positive(message = "事件ID必须为正数") Integer eventId) {
        log.info("删除事件记录，记录ID: {}", eventId);
        petRecordsService.deleteRecord(eventId);
        return Result.success("删除成功");
    }
}
