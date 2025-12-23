package com.example.petweb.controller.admin;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.petcommon.result.Result;
import com.example.petservice.service.AdoptionsService;

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
@RequestMapping("/admin/adoptions")
@Tag(name = "管理员-领养审核")
@Validated
@RequiredArgsConstructor
public class AdminAdoptionController {

    private final AdoptionsService adoptionsService;

    @PostMapping("/{id}/approve")
    public Result<String> approve(@PathVariable @Positive(message = "领养记录ID必须为正数") Integer id, @Valid @RequestBody(required = false) java.util.Map<String, String> payload) {
        String note = payload != null ? payload.getOrDefault("note", null) : null;
        adoptionsService.approveAdoption(id, note);
        return Result.success("审核通过");
    }

    @PostMapping("/{id}/reject")
    public Result<String> reject(@PathVariable @Positive(message = "领养记录ID必须为正数") Integer id, @Valid @RequestBody(required = false) java.util.Map<String, String> payload) {
        String note = payload != null ? payload.getOrDefault("note", null) : null;
        adoptionsService.rejectAdoption(id, note);
        return Result.success("已拒绝");
    }

    @GetMapping
    public Result<Object> list(
            @RequestParam(value = "status", required = false) @Size(max = 20, message = "状态长度不能超过20字符") String status,
            @RequestParam(value = "current_page", defaultValue = "1") @Min(value = 1, message = "current_page 必须>=1") Integer currentPage,
            @RequestParam(value = "per_page", defaultValue = "10") @Min(value = 1, message = "per_page 必须>=1") @Max(value = 100, message = "per_page 不能超过100") Integer pageSize) {
        return Result.success(adoptionsService.listAdoptionsForAdmin(currentPage, pageSize, status));
    }
}
