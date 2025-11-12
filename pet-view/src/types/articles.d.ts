// 文章类型定义
export interface Article {
  id: number
  title: string
  content: string
  summary: string
  coverImage: string
  author: string
  viewCount: number
  createdAt: string
  updatedAt?: string
}


// 分页响应数据类型
export interface ArticlePageData {
  records: Article[]
  total: number
  size: number
  current: number
  pages: number
  searchCount?: boolean
}



