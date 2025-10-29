package com.example.petservice.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petpojo.entity.Articles;
import com.example.petpojo.entity.enums.CommonEnum;
import com.example.petservice.mapper.ArticlesMapper;
import com.example.petservice.service.ArticlesService;
import com.example.petpojo.vo.ArticlesVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;


@Service
public class ArticlesServiceImpl extends ServiceImpl<ArticlesMapper, Articles> implements ArticlesService {


    private ArticlesVo convertToVo(Articles articles) {
        ArticlesVo articlesVo = new ArticlesVo();
        BeanUtils.copyProperties(articles, articlesVo);
        return articlesVo;
    }
    /**
     * 查询文章列表
     * @return 文章列表
     */
    @Override
    public IPage<ArticlesVo> getArticles(Integer currentPage, Integer pageSize) {
        // 分页查询文章列表
        Page<Articles> page = new Page<>(currentPage, pageSize);
        IPage<ArticlesVo> articles = lambdaQuery()
                .eq(Articles::getStatus, CommonEnum.ArticleStatusEnum.PUBLISHED)
                .page(page)
                .convert(this::convertToVo);

        return articles;
    }
    /**
     * 查询文章详情
     * @param id 文章ID
     * @return 文章详情
     */
    @Override
    public ArticlesVo getArticleDetail(int id) {
        Articles articles = getById(id);
        if (articles == null) {
            return null;
        }
        return convertToVo(articles);
    }

}
