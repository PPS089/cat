// 通用 API 响应与分页类型
export interface ApiResult<T = any> {
  code: number
  data: T
  message?: string
}

export interface PageResult<T = any> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export type Id = string | number
