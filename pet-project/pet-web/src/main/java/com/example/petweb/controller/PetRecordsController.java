package com.example.petweb.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.example.petcommon.context.UserContext;
import com.example.petcommon.result.Result;
import com.example.petpojo.dto.RecordDto;
import com.example.petpojo.entity.PetRecords;
import com.example.petpojo.vo.EventVo;
import com.example.petservice.service.PetRecordsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "宠物事件记录", description = "宠物事件记录相关的 API 接口")
@SecurityRequirement(name = "bearer-key")
public class PetRecordsController {

    private final PetRecordsService petRecordsService;

    /**
     * 获取用户的事件记录列表
     */
    @GetMapping
    @Operation(summary = "获取用户的事件记录列表", description = "获取当前登录用户的所有宠物事件记录列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
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
    @Operation(summary = "创建事件记录", description = "创建一个新的宠物事件记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    public Result<EventVo> createEvent(
            @Parameter(description = "事件记录数据传输对象") @Valid @RequestBody RecordDto recordDto) {
        log.info("创建事件记录，用户ID: {}, 宠物ID: {}", recordDto.getUid(), recordDto.getPid());
        
        // 直接获取EventVo对象
        EventVo eventVo = petRecordsService.createRecord(recordDto);
        
        return Result.success(eventVo);
    }

    /**
     * 更新事件记录
     */
    @PutMapping("/{eventId}")
    @Operation(summary = "更新事件记录", description = "更新指定ID的宠物事件记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "404", description = "事件记录不存在")
    })
    public Result<EventVo> updateEvent(
            @Parameter(description = "事件记录ID") @PathVariable Integer eventId,
            @Parameter(description = "事件记录数据传输对象") @Valid @RequestBody RecordDto recordDto) {
        
        log.info("更新事件记录，记录ID: {}", eventId);
        
        // 直接获取EventVo对象
        EventVo eventVo = petRecordsService.updateRecord(eventId, recordDto);
        
        return Result.success(eventVo);
    }

    /**
     * 删除事件记录
     */
    @DeleteMapping("/{eventId}")
    @Operation(summary = "删除事件记录", description = "删除指定ID的宠物事件记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "404", description = "事件记录不存在")
    })
    public Result<String> deleteEvent(
            @Parameter(description = "事件记录ID") @PathVariable Integer eventId) {
        log.info("删除事件记录，记录ID: {}", eventId);
        
        boolean result = petRecordsService.deleteRecord(eventId);
        
        if (result) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }
}