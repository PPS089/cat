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

    /**
     * 管理员创建文章
     */
    ArticlesVo createArticle(com.example.petpojo.dto.ArticleRequest request);

    /**
     * 管理员更新文章
     */
    ArticlesVo updateArticle(Integer id, com.example.petpojo.dto.ArticleRequest request);

    /**
     * 管理员删除文章
     */
    void deleteArticle(Integer id);

    /**
     * 管理员分页查看文章（可按状态过滤）
     */
    com.baomidou.mybatisplus.core.metadata.IPage<ArticlesVo> listArticlesForAdmin(Integer currentPage, Integer pageSize, String status);

}
