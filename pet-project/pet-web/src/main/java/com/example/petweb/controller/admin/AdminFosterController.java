package com.example.petweb.controller.admin;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.petcommon.result.Result;
import com.example.petservice.service.FosterService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import lombok.RequiredArgsConstructor;

/**
 * @author 33185
 */
@RestController
@RequestMapping("/admin/fosters")
@Tag(name = "管理员-寄养审核")
@Validated
@RequiredArgsConstructor
public class AdminFosterController {

    private final FosterService fosterService;

    @PostMapping("/{id}/approve")
    public Result<String> approve(@PathVariable @Positive(message = "寄养记录ID必须为正数") Integer id, @Valid @RequestBody(required = false) java.util.Map<String, String> payload) {
        String note = payload != null ? payload.getOrDefault("note", null) : null;
        fosterService.approveFoster(id, note);
        return Result.success("寄养审核通过");
    }

    @PostMapping("/{id}/reject")
    public Result<String> reject(@PathVariable @Positive(message = "寄养记录ID必须为正数") Integer id, @Valid @RequestBody(required = false) java.util.Map<String, String> payload) {
        String note = payload != null ? payload.getOrDefault("note", null) : null;
        fosterService.rejectFoster(id, note);
        return Result.success("寄养已拒绝");
    }

    @PostMapping("/{id}/complete")
    public Result<String> complete(@PathVariable @Positive(message = "寄养记录ID必须为正数") Integer id) {
        fosterService.completeFoster(id);
        return Result.success("寄养已完成");
    }

    @GetMapping
    public Result<Object> list(
            @RequestParam(value = "status", required = false) @Size(max = 20, message = "状态长度不能超过20字符") String status,
            @RequestParam(value = "current_page", defaultValue = "1") @Min(value = 1, message = "current_page 必须>=1") Integer currentPage,
            @RequestParam(value = "per_page", defaultValue = "10") @Min(value = 1, message = "per_page 必须>=1") Integer pageSize) {
        if (pageSize > 100) {
            pageSize = 100;
        }
        return Result.success(fosterService.listFostersForAdmin(currentPage, pageSize, status));
    }
}
