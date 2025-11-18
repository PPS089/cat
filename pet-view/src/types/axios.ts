// 导入 Axios 基础类型（必须）
import type { AxiosRequestConfig, AxiosInstance } from 'axios'

// 1. 定义后端返回的通用业务数据结构（根据你的实际接口调整）
// 例如：后端接口统一返回 { code: 200, data: ..., message?: '成功' }
export interface ApiResponse<T = any> {
  code: number;         // 业务状态码（如 200 表示成功）
  data: T;              // 接口返回的实际数据（泛型，根据接口动态变化）
  message?: string;     // 可选的消息提示（如错误信息）
}

// 2. 扩展 AxiosInstance 类型，覆盖 get/post 等方法的返回值类型
declare module 'axios' {
  // 使用导入的类型来扩展 AxiosInstance
  interface AxiosInstance {
    request<T = any, R = ApiResponse<T>, D = any>(config: AxiosRequestConfig<D>): Promise<R>
    get<T = any, R = ApiResponse<T>, D = any>(url: string, config?: AxiosRequestConfig<D>): Promise<R>
    delete<T = any, R = ApiResponse<T>, D = any>(url: string, config?: AxiosRequestConfig<D>): Promise<R>
    post<T = any, R = ApiResponse<T>, D = any>(url: string, data?: D, config?: AxiosRequestConfig<D>): Promise<R>
    put<T = any, R = ApiResponse<T>, D = any>(url: string, data?: D, config?: AxiosRequestConfig<D>): Promise<R>
    upload<T = any, R = ApiResponse<T>, D = any>(url: string, data?: D, config?: AxiosRequestConfig<D>): Promise<R>
    upload<T = any, R = ApiResponse<T>>(url: string, file: File): Promise<R>
  }
}

// 导出类型以确保它们被使用
export type { AxiosRequestConfig, AxiosInstance }