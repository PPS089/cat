package com.example.petservice.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petcommon.context.UserContext;
import com.example.petcommon.error.ErrorCode;
import com.example.petcommon.exception.BizException;
import com.example.petpojo.dto.ArticleRequest;
import com.example.petpojo.entity.Articles;
import com.example.petpojo.entity.enums.CommonEnum;
import com.example.petpojo.vo.ArticlesVo;
import com.example.petservice.mapper.ArticlesMapper;
import com.example.petservice.service.ArticlesService;


/**
 * @author 33185
 */
@Service
public class ArticlesServiceImpl extends ServiceImpl<ArticlesMapper, Articles> implements ArticlesService {


    private ArticlesVo convertToVo(@NonNull Articles articles) {
        ArticlesVo articlesVo = new ArticlesVo();
        BeanUtils.copyProperties(articles, articlesVo);
        articlesVo.setStatus(articles.getStatus() != null ? articles.getStatus().getCode() : null);
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

        return lambdaQuery()
                .eq(Articles::getStatus, CommonEnum.ArticleStatusEnum.PUBLISHED)
                .page(page)
                .convert(this::convertToVo);
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

    @Override
    @Transactional
    public ArticlesVo createArticle(ArticleRequest request) {
        Articles article = new Articles();
        BeanUtils.copyProperties(request, article);
        article.setViewCount(0);
        article.setStatus(resolveStatus(request.getStatus()));
        Integer adminShelterId = UserContext.getCurrentAdminShelterId();
        boolean isPlatformAdmin = UserContext.isPlatformAdmin();
        if (!isPlatformAdmin && adminShelterId == null) {
            throw new BizException(ErrorCode.FORBIDDEN, "当前管理员未绑定收容所，无法创建文章");
        }
        // 平台管理员可以创建平台级文章（shelterId 为 null），收容所管理员创建本收容所文章
        if (!isPlatformAdmin) {
            article.setShelterId(adminShelterId);
        }
        this.save(article);
        return convertToVo(article);
    }

    @Override
    @Transactional
    public ArticlesVo updateArticle(Integer id, ArticleRequest request) {
        Articles article = getById(id);
        if (article == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        Integer adminShelterId = UserContext.getCurrentAdminShelterId();
        boolean isPlatformAdmin = UserContext.isPlatformAdmin();
        if (!isPlatformAdmin) {
            if (adminShelterId == null || !adminShelterId.equals(article.getShelterId())) {
                throw new BizException(ErrorCode.FORBIDDEN, "无权编辑其他收容所的文章");
            }
        }
        BeanUtils.copyProperties(request, article);
        article.setStatus(resolveStatus(request.getStatus()));
        this.updateById(article);
        return convertToVo(article);
    }

    @Override
    @Transactional
    public void deleteArticle(Integer id) {
        Articles article = getById(id);
        if (article == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "文章不存在或已删除");
        }
        Integer adminShelterId = UserContext.getCurrentAdminShelterId();
        boolean isPlatformAdmin = UserContext.isPlatformAdmin();
        if (!isPlatformAdmin) {
            if (adminShelterId == null || !adminShelterId.equals(article.getShelterId())) {
                throw new BizException(ErrorCode.FORBIDDEN, "无权删除其他收容所的文章");
            }
        }
        if (!this.removeById(id)) {
            throw new BizException(ErrorCode.NOT_FOUND, "文章不存在或已删除");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<ArticlesVo> listArticlesForAdmin(Integer currentPage, Integer pageSize, String status) {
        Page<Articles> page = new Page<>(currentPage, pageSize);
        Integer adminShelterId = UserContext.getCurrentAdminShelterId();
        boolean isPlatformAdmin = UserContext.isPlatformAdmin();
        return lambdaQuery()
                .eq(status != null && !status.isBlank(), Articles::getStatus, resolveStatus(status))
                .eq(!isPlatformAdmin && adminShelterId != null, Articles::getShelterId, adminShelterId)
                .page(page)
                .convert(this::convertToVo);
    }

    private CommonEnum.ArticleStatusEnum resolveStatus(String status) {
        if (status == null) {
            return CommonEnum.ArticleStatusEnum.PUBLISHED;
        }
        try {
            return CommonEnum.ArticleStatusEnum.valueOf(status.toUpperCase());
        } catch (Exception e) {
            return CommonEnum.ArticleStatusEnum.PUBLISHED;
        }
    }
}
