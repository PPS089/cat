package com.example.petservice.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petcommon.error.ErrorCode;
import com.example.petcommon.exception.BizException;
import com.example.petpojo.entity.Articles;
import com.example.petpojo.entity.enums.CommonEnum;
import com.example.petpojo.vo.ArticlesVo;
import com.example.petservice.mapper.ArticlesMapper;
import com.example.petservice.service.ArticlesService;


@Service
public class ArticlesServiceImpl extends ServiceImpl<ArticlesMapper, Articles> implements ArticlesService {


    private ArticlesVo convertToVo(@NonNull Articles articles) {
        ArticlesVo articlesVo = new ArticlesVo();
        BeanUtils.copyProperties(articles, articlesVo);
        return articlesVo;
    }
    /**
     * 查询文章列表
     * @param currentPage 当前页码
     * @param pageSize 每页数量
     * @return 文章列表分页对象
     */
    @Override
    @Transactional(readOnly = true)
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
     * @return 文章详情VO对象
     */
    @Override
    @Transactional
    public ArticlesVo getArticleDetail(int id) {
        Articles articles = getById(id);
        if (articles == null) {
           throw new BizException(ErrorCode.NOT_FOUND);
        }
        
        // 增加文章浏览次数
        articles.setViewCount(articles.getViewCount() == null ? 1 : articles.getViewCount() + 1);
        updateById(articles);
        
        return convertToVo(articles);
    }

}
