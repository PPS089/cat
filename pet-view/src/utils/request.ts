import axios, {
  type AxiosInstance,
  type AxiosResponse,
  type InternalAxiosRequestConfig
} from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { getRequestKey, removePending } from './requestOptimize'

// Token刷新状态标志，防止并发刷新
let isRefreshingToken = false
// 存储等待刷新完成的请求回调
let refreshSubscribers: Array<(token: string | null) => void> = []

// 订阅token刷新事件
const subscribeTokenRefresh = (cb: (token: string | null) => void) => {
  refreshSubscribers.push(cb)
}

// 通知所有订阅者token刷新结果
const notifyTokenRefreshed = (token: string | null) => {
  refreshSubscribers.forEach(cb => cb(token))
  refreshSubscribers = []
}

// 登出操作：清除本地存储并跳转到登录页
const logout = () => {
  localStorage.removeItem('jwt_token')
  localStorage.removeItem('userId')
  localStorage.removeItem('userInfo')
  router.push('/login')
}

// 获取API基础URL
const getBaseURL = () => {
  // 如果有VITE_API_BASE环境变量（公网地址），则使用它
  if (import.meta.env.VITE_API_BASE) {
    return import.meta.env.VITE_API_BASE
  }
  // 否则使用VITE_APP_BASE_API（通常是/api用于本地开发）
  return import.meta.env.VITE_APP_BASE_API || '/api'
}

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: getBaseURL(),
  timeout: 300000,
  withCredentials: false 
})

// 请求拦截器：添加认证token
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('jwt_token') 
    const loginUrls = ['/login', '/user/login']
    const isLoginUrl = loginUrls.some(url => config.url?.endsWith(url))
    if (config.url && !isLoginUrl && token) {
      if (!config.headers) config.headers = {} as any
      const headersAny = config.headers as any
      if (typeof headersAny.set === 'function') {
        headersAny.set('Authorization', `Bearer ${token}`)
      } else {
        headersAny.Authorization = `Bearer ${token}`
      }
    }
    
    return config
  },
  (error) => Promise.reject(error)
)

// 处理未授权错误（token过期）
const handleUnauthorized = (config: any) => {
  // 如果正在刷新token，则将请求加入等待队列
  if (isRefreshingToken) {
    return new Promise((resolve, reject) => {
      subscribeTokenRefresh((newToken) => {
        if (newToken) {
          if (!config.headers) config.headers = {} as any
          const headersAny = config.headers as any
          if (typeof headersAny.set === 'function') {
            headersAny.set('Authorization', `Bearer ${newToken}`)
          } else {
            headersAny.Authorization = `Bearer ${newToken}`
          }
          resolve(service(config))
        } else {
          reject(new Error('Token刷新失败'))
        }
      })
    })
  }

  // 开始刷新token流程
  isRefreshingToken = true
  return new Promise((resolve, reject) => {
    // 使用refresh token获取新的access token
    const refreshToken = localStorage.getItem('refresh_token')
    
    if (!refreshToken) {
      console.error('没有refresh token，需要重新登录')
      isRefreshingToken = false
      logout()
      reject(new Error('没有refresh token，需要重新登录'))
      return
    }
    
    // 发送refresh token到服务器，不需要Authorization头
    service
      .post('/user/refresh-token', { 
        refreshToken: refreshToken
      }, {
        // 不添加Authorization头，使用refresh token进行认证
        headers: {}
      })
      .then((res: any) => {
        const newAccessToken = res?.data?.accessToken
        const newRefreshToken = res?.data?.refreshToken
        
        if (newAccessToken) {
          // 保存新token到localStorage
          localStorage.setItem('jwt_token', newAccessToken)
          
          // 如果服务器返回了新的refresh token，也保存它
          if (newRefreshToken) {
            localStorage.setItem('refresh_token', newRefreshToken)
          }
          
          isRefreshingToken = false
          notifyTokenRefreshed(newAccessToken)
          
          // 更新请求头中的Authorization
          if (!config.headers) config.headers = {} as any
          const headersAny = config.headers as any
          if (typeof headersAny.set === 'function') {
            headersAny.set('Authorization', `Bearer ${newAccessToken}`)
          } else {
            headersAny.Authorization = `Bearer ${newAccessToken}`
          }
          resolve(service(config))
        } else {
          console.error('刷新token失败：服务器未返回新的access token')
          isRefreshingToken = false
          notifyTokenRefreshed(null)
          logout()
          reject(new Error('Token刷新失败'))
        }
      })
      .catch((error: any) => {
        console.error('刷新token请求失败:', error)
        isRefreshingToken = false
        notifyTokenRefreshed(null)
        logout()
        reject(error)
      })
  })
}

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    // 移除请求去重标记
    const key = getRequestKey(response.config)
    removePending(key)

    const { data } = response

    // 处理401未授权错误（token过期）
    if (data.code !== undefined && [401].includes(data.code)) {
      return handleUnauthorized(response.config)
    }

    // 处理其他业务错误
    if (data.code !== undefined && ![1, 200].includes(data.code)) {
      const msg = data.msg || '请求失败'
      ElMessage.error(msg)
      return Promise.reject(new Error(msg))
    }

    return data
  },
  (error: any) => {
    // 移除请求去重标记
    if (error.config) {
      const key = getRequestKey(error.config)
      removePending(key)
    }

    if (error.response) {
      const { status, data } = error.response
      // 特殊处理token刷新失败的情况
      if (status === 401) {
        if (error.config?.url?.endsWith('/user/refresh-token')) {
          logout()
          return Promise.reject(new Error('Token刷新失败'))
        }
        return handleUnauthorized(error.config)
      }
      // 处理其他HTTP错误状态码
      const messageMap: Record<number, string> = {
        400: '请求参数错误',
        404: '请求的资源不存在',
        405: '请求方法不允许',
        500: '服务器内部错误',
        502: '网关错误',
        503: '服务不可用'
      }

      const msg = data?.msg || messageMap[status] || `请求错误：${status}`
      ElMessage.error(msg)
    } else {
      // 处理网络错误
      if (error.message === 'Network Error') {
        ElMessage.error('网络连接错误，请检查网络')
      } else if (error.code === 'ECONNABORTED') {
        ElMessage.error('请求超时，请稍后重试')
      } else if (error.message !== '重复请求，已取消') {
        ElMessage.error(error.message || '未知错误')
      }
    }

    return Promise.reject(error)
  }
)

// ------------------------ 文件上传 ------------------------
export const upload = async (url: string, file: File): Promise<any> => {
  const formData = new FormData()
  formData.append('file', file)

  const response = await service.post(url, formData)

  return response
}


// ------------------------ 扩展实例 ------------------------
;(service as any).upload = upload

export default service