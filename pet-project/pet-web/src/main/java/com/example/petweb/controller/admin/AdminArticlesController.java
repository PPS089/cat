package com.example.petweb.controller.admin;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.petcommon.result.Result;
import com.example.petpojo.dto.ArticleRequest;
import com.example.petpojo.vo.ArticlesVo;
import com.example.petservice.service.ArticlesService;

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
@RequestMapping("/admin/articles")
@Tag(name = "管理员-文章管理")
@Validated
@RequiredArgsConstructor
public class AdminArticlesController {

    private final ArticlesService articlesService;

    @PostMapping
    public Result<ArticlesVo> create(@Valid @RequestBody ArticleRequest request) {
        return Result.success(articlesService.createArticle(request));
    }

    @PutMapping("/{id}")
    public Result<ArticlesVo> update(@PathVariable @Positive(message = "文章ID必须为正数") Integer id, @Valid @RequestBody ArticleRequest request) {
        return Result.success(articlesService.updateArticle(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable @Positive(message = "文章ID必须为正数") Integer id) {
        articlesService.deleteArticle(id);
        return Result.success("删除成功");
    }

    @GetMapping
    public Result<Object> list(
            @RequestParam(value = "status", required = false) @Size(max = 20, message = "状态长度不能超过20字符") String status,
            @RequestParam(value = "current_page", defaultValue = "1") @Min(value = 1, message = "current_page 必须>=1") Integer currentPage,
            @RequestParam(value = "per_page", defaultValue = "10") @Min(value = 1, message = "per_page 必须>=1") @Max(value = 100, message = "per_page 不能超过100") Integer pageSize) {
        return Result.success(articlesService.listArticlesForAdmin(currentPage, pageSize, status));
    }
}
