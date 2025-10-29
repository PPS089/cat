import axios, {
  type AxiosInstance,
  type AxiosResponse,
  type InternalAxiosRequestConfig
} from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { getRequestKey,removePending } from './requestOptimize'

// 标记是否正在刷新token
let isRefreshingToken = false
// 存储待处理的请求队列
const refreshTokenQueue: Array<{
  config: InternalAxiosRequestConfig
  resolve: (value: any) => void
  reject: (reason?: any) => void
}> = []

// 处理待处理请求队列
const processQueue = (newToken: string) => {
  refreshTokenQueue.forEach(({ config, resolve, reject }) => {
    try {
      config.headers.Authorization = `Bearer ${newToken}`
      resolve(service(config))
    } catch (error) {
      reject(error)
    }
  })
  // 清空队列
  refreshTokenQueue.length = 0
}

// ------------------------ Axios 实例 ------------------------
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API, // 通过 Vite 代理解决跨域
  timeout: 60000,
  withCredentials: false 
})


// ------------------------ 请求拦截器 ------------------------
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 获取token，优先使用jwt_token，如果没有则使用token
    const token = localStorage.getItem('jwt_token') || localStorage.getItem('token')

    // 请求头添加token
    // 精确匹配需要排除的登录接口，避免误伤其他包含'login'的接口
    const loginUrls = ['/login', '/user/login'];
    const isLoginUrl = loginUrls.some(url => config.url?.endsWith(url));
    
    if (config.url && !isLoginUrl && config.url !== '/user/refresh-token') {
      if (token) {
        if (!config.headers) {
          config.headers = {} as any
        }
        if (typeof (config.headers as any).set === 'function') {
          ;(config.headers as any).set('Authorization', `Bearer ${token}`)
        } else {
          ;(config.headers as any).Authorization = `Bearer ${token}`
        }
        console.log('DEBUG: 设置Authorization头:', `Bearer ${token}`)
      }
    }
    
    // 打印最终的请求头信息用于调试
    console.log('DEBUG: 最终请求配置:', {
      url: config.url,
      method: config.method,
      headers: config.headers
    })

    return config
  },
  (error) => Promise.reject(error)
)

// ------------------------ 响应拦截器 ------------------------
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const key = getRequestKey(response.config)
    removePending(key)

    const { data } = response

    // 未授权 / Token 过期 - 处理HTTP状态码和响应体中的错误码
    if ([401, 403].includes(response.status) || [401, 403].includes(data.code)) {
      console.log('DEBUG: 检测到未授权响应，状态码:', response.status, '响应码:', data.code)
      
      // 正在刷新token或已经是刷新接口，此时直接清除token並跳转登录
      if (isRefreshingToken || response.config.url?.includes('/user/refresh-token')) {
        console.log('DEBUG: 已经是刷新token请求或第一次刷新失败，直接清除token並跳转')
        localStorage.removeItem('jwt_token')
        localStorage.removeItem('token')
        localStorage.removeItem('userId')
        localStorage.removeItem('userInfo')
        router.push('/login')
        return Promise.reject(new Error('未授权'))
      }
      
      // 需要刷新token
      isRefreshingToken = true
      console.log('DEBUG: 开始刷新token')
      
      return new Promise((resolve, reject) => {
        service
          .post('/user/refresh-token', {})
          .then((res: any) => {
            console.log('DEBUG: token刷新成功，新token:', res.data?.token?.substring(0, 20) + '...')
            const newToken = res.data?.token
            if (newToken) {
              // 保存新token
              localStorage.setItem('jwt_token', newToken)
              localStorage.setItem('token', newToken)
              
              // 更新待处理请求的Authorization头
              isRefreshingToken = false
              processQueue(newToken)
              
              // 也可以重试原请求
              response.config.headers.Authorization = `Bearer ${newToken}`
              resolve(service(response.config))
            } else {
              isRefreshingToken = false
              reject(new Error('refresh token失败'))
            }
          })
          .catch((error: any) => {
            console.error('DEBUG: token刷新失败:', error.message)
            isRefreshingToken = false
            // 刷新失败，清除token并跳转登录
            localStorage.removeItem('jwt_token')
            localStorage.removeItem('token')
            localStorage.removeItem('userId')
            localStorage.removeItem('userInfo')
            router.push('/login')
            reject(error)
          })
      })
    }

    // 业务逻辑错误
    if (data.code !== undefined && ![1, 200].includes(data.code)) {
      const msg = data.msg || data.message || '请求失败'
      ElMessage.error(msg)
      return Promise.reject(new Error(msg))
    }

    return data
  },
  (error: any) => {
    if (error.config) {
      const key = getRequestKey(error.config)
      removePending(key)
    }

    // 网络层错误
    if (error.response) {
      const { status, data } = error.response
      const messageMap: Record<number, string> = {
        401: '登录已过期，请重新登录',
        403: '没有权限访问',
        404: '请求的资源不存在',
        405: '请求方法不允许',
        500: '服务器内部错误',
        502: '网关错误',
        503: '服务不可用'
      }

      const msg = messageMap[status] || data?.message || `请求错误：${status}`
      if (status === 401) {
        ElMessage.error(msg)
        router.push('/login')
      } else ElMessage.error(msg)
    } else {
      // 客户端错误
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

  const response = await service.post(url, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })

  return response
}


// ------------------------ 扩展实例 ------------------------
;(service as any).upload = upload

export default service