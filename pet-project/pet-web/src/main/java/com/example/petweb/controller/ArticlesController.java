package com.example.petweb.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.petcommon.result.Result;
import com.example.petservice.service.ArticlesService;
import com.example.petpojo.vo.ArticlesVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文章管理控制器
 * 提供文章相关的 REST API 接口
 */
@RestController
@RequestMapping("/articles")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "文章管理", description = "文章相关的 API 接口")
@SecurityRequirement(name = "bearer-key")
public class ArticlesController {

    private final ArticlesService articlesService;


    @GetMapping
    @Operation(summary = "获取文章列表", description = "分页获取所有文章")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public Result<IPage<ArticlesVo>> getArticles(
            @Parameter(description = "当前页码，从1开始") @RequestParam Integer currentPage, 
            @Parameter(description = "每页记录数") @RequestParam Integer pageSize) {
        IPage<ArticlesVo> articles =articlesService.getArticles(currentPage, pageSize);
        return Result.success(articles);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取文章详情", description = "根据ID获取指定文章的详细内容")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "文章不存在")
    })
    public Result<ArticlesVo> getArticleDetail(
            @Parameter(description = "文章ID", required = true) @PathVariable Integer id) {
        ArticlesVo articles =articlesService.getArticleDetail(id);
        return Result.success(articles);
    }

}