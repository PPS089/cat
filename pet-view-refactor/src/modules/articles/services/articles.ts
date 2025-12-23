import request from '@/shared/utils/request'
import type { ApiResponse } from '@/types/api'
import type { ArticlePageData, Article, ArticleListParams } from '../types'

export const articlesService = {
  list(params: ArticleListParams) {
    return request
      .get<ApiResponse<ArticlePageData>>('/articles', {
        params: {
          currentPage: params.currentPage,
          pageSize: params.pageSize,
        },
      })
      .then(response => response as unknown as ApiResponse<ArticlePageData>)
  },
  detail(id: number, increase = true) {
    return request
      .get<ApiResponse<Article>>(`/articles/${id}`, {
        params: {
          increase,
        },
      })
      .then(response => response as unknown as ApiResponse<Article>)
  },
}
