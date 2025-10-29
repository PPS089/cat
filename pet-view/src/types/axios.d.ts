// 导入 Axios 基础类型（必须）
import type { AxiosRequestConfig, AxiosInstance } from 'axios'

// 1. 定义后端返回的通用业务数据结构（根据你的实际接口调整）
// 例如：后端接口统一返回 { code: 200, data: ..., message?: '成功' }
interface ApiResponse<T = any> {
  code: number;         // 业务状态码（如 200 表示成功）
  data: T;              // 接口返回的实际数据（泛型，根据接口动态变化）
  message?: string;     // 可选的消息提示（如错误信息）
}

// 2. 扩展 AxiosInstance 类型，覆盖 get/post 等方法的返回值类型
declare module 'axios' {
  // 扩展 AxiosInstance 接口，重新定义请求方法的返回类型
  interface AxiosInstance {
    // 返回 Promise<ApiResponse<T>>，T 是 data 的类型
    get<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>>;
    post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>>;
    put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>>;
    delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>>;
    upload<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>>;
  }
}