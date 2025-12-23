export interface Article {
  id: number
  title: string
  content: string
  coverImage: string
  author: string
  viewCount: number
  createTime: string
  updateTime?: string
}

export interface ArticlePageData {
  records: Article[]
  total: number
  size: number
  current: number
  pages: number
}

export interface ArticleListParams {
  currentPage: number
  pageSize: number
}
