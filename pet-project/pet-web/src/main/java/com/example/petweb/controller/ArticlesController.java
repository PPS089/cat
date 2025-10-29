package com.example.petweb.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.petcommon.result.Result;
import com.example.petservice.service.ArticlesService;
import com.example.petpojo.vo.ArticlesVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/articles")
@Slf4j
@RequiredArgsConstructor
public class ArticlesController {

    private final ArticlesService articlesService;


    @GetMapping
    public Result<IPage<ArticlesVo>> getArticles(
            @RequestParam Integer currentPage, 
            @RequestParam Integer pageSize) {
        IPage<ArticlesVo> articles =articlesService.getArticles(currentPage, pageSize);
        return Result.success(articles);
    }

    @GetMapping("/{id}")
    public Result<ArticlesVo> getArticleDetail(
            @PathVariable Integer id) {
        ArticlesVo articles =articlesService.getArticleDetail(id);
        return Result.success(articles);
    }

}