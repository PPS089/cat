package com.example.petservice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.petpojo.entity.Articles;
import com.example.petpojo.vo.ArticlesVo;


public interface ArticlesService extends IService<Articles> {
    /**
     * 查询文章列表
     * @param currentPage 当前页码
     * @param pageSize 每页数量
     * @return 文章列表分页对象
     */
    IPage<ArticlesVo> getArticles(Integer currentPage, Integer pageSize);
    /**
     * 查询文章详情
     * @param id 文章ID
     * @return 文章详情VO对象
     */
    ArticlesVo getArticleDetail(int id);


}
