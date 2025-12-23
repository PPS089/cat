import axios, { type AxiosInstance, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/app/router'
import { getRequestKey, removePending } from './requestOptimize'
import { dispatchUserSessionCleared, dispatchUserSessionRefreshed } from '@/shared/utils/events'

export const TAB_SESSION_ROLE_KEY = 'tab_session_role'

const AUTH_FREE_ENDPOINTS = ['/user/login', '/user/register', '/user/send-reset-code', '/user/forgot-password', '/user/refresh-token']

const AUTH_NOTICE_KEY = 'auth_notice'
type AuthNoticeReason = 'expired' | 'refresh_failed' | 'unauthorized'

let isRefreshingToken = false
let refreshSubscribers: Array<(token: string | null) => void> = []
let isLogoutRedirecting = false
let lastLogoutNoticeAt = 0

type SessionRole = 'ADMIN' | 'USER'
type ApiResult<T = any> = {
  code?: number
  msg?: string
  data?: T
}

const ADMIN_ARTIFACT_KEYS = ['admin_pets_filters', 'admin_articles_filters', 'admin_dashboard_state']
const ADMIN_SESSION_KEYS = ['admin_active_tab', 'admin_current_project']

const normalizeRole = (role?: string | null): SessionRole | undefined => {
  if (!role) return undefined
  return role.toUpperCase() === 'ADMIN' ? 'ADMIN' : 'USER'
}
const roleSlug = (role: SessionRole) => (role === 'ADMIN' ? 'admin' : 'user')

const inferRoleFromUrl = (url?: string | null): SessionRole | null => {
  if (!url) return null
  const normalized = url.toLowerCase()
  if (normalized.includes('/admin/')) return 'ADMIN'
  if (normalized.includes('/user/')) return 'USER'
  return null
}

const getTabRole = (): SessionRole | undefined => {
  if (typeof window === 'undefined') return undefined
  return normalizeRole(sessionStorage.getItem(TAB_SESSION_ROLE_KEY))
}

const rememberTabRole = (role?: SessionRole) => {
  if (typeof window === 'undefined' || !role) return
  sessionStorage.setItem(TAB_SESSION_ROLE_KEY, role)
  sessionStorage.setItem('session_role', role)
}

const readSessionRole = (): SessionRole | undefined => {
  const fromTab = getTabRole()
  if (fromTab) return fromTab
  const fromSession = normalizeRole(
    typeof sessionStorage === 'undefined' ? undefined : sessionStorage.getItem('session_role'),
  )
  if (fromSession) return fromSession
  return normalizeRole(localStorage.getItem('session_role'))
}

type RoleAwareConfig = Pick<InternalAxiosRequestConfig, 'url' | 'headers'> & { sessionRole?: SessionRole | string }

const resolveRole = (config?: RoleAwareConfig): SessionRole => {
  const fromHeader = normalizeRole(config?.headers?.['X-Session-Role'] as any)
  if (fromHeader) {
    rememberTabRole(fromHeader)
    return fromHeader
  }
  const fromExplicit = normalizeRole(config?.sessionRole as any)
  if (fromExplicit) {
    rememberTabRole(fromExplicit)
    return fromExplicit
  }
  const fromUrl = inferRoleFromUrl(config?.url)
  if (fromUrl) {
    rememberTabRole(fromUrl)
    return fromUrl
  }
  const fromStored = readSessionRole()
  if (fromStored) {
    rememberTabRole(fromStored)
    return fromStored
  }
  return 'USER'
}

const storageKeys = (role: SessionRole) => {
  const slug = roleSlug(role)
  return {
    token: `${slug}_jwt_token`,
    refresh: `${slug}_refresh_token`,
    expire: `${slug}_jwt_expire_at`,
    userId: `${slug}_userId`,
    userInfo: `${slug}_userInfo`,
    userName: `${slug}_userName`,
  }
}

const detectCurrentRole = (): SessionRole => {
  return readSessionRole() || 'USER'
}

const clearRoleArtifacts = (role: SessionRole) => {
  if (role === 'USER') {
    return
  }
  ADMIN_ARTIFACT_KEYS.forEach(key => localStorage.removeItem(key))
  if (typeof sessionStorage !== 'undefined') {
    ADMIN_SESSION_KEYS.forEach(key => sessionStorage.removeItem(key))
  }
}

const subscribeTokenRefresh = (cb: (token: string | null) => void) => {
  refreshSubscribers.push(cb)
}

const notifyTokenRefreshed = (token: string | null) => {
  refreshSubscribers.forEach(cb => cb(token))
  refreshSubscribers = []
}

const isAuthFreeEndpoint = (url?: string | null) => {
  if (!url) return false
  return AUTH_FREE_ENDPOINTS.some(endpoint => url.endsWith(endpoint))
}

const setAuthNotice = (reason: AuthNoticeReason) => {
  if (typeof sessionStorage === 'undefined') return
  sessionStorage.setItem(AUTH_NOTICE_KEY, reason)
}

export const consumeAuthNotice = (): AuthNoticeReason | null => {
  if (typeof sessionStorage === 'undefined') return null
  const reason = sessionStorage.getItem(AUTH_NOTICE_KEY) as AuthNoticeReason | null
  if (!reason) return null
  sessionStorage.removeItem(AUTH_NOTICE_KEY)
  return reason
}

const logout = (role?: SessionRole, reason: AuthNoticeReason = 'unauthorized') => {
  const r = role || detectCurrentRole()
  const keys = storageKeys(r)
  localStorage.removeItem(keys.token)
  localStorage.removeItem(keys.refresh)
  localStorage.removeItem(keys.userId)
  localStorage.removeItem(keys.userInfo)
  localStorage.removeItem(keys.userName)
  localStorage.removeItem(keys.expire)
  clearRoleArtifacts(r)
  if (typeof sessionStorage !== 'undefined') {
    sessionStorage.removeItem(TAB_SESSION_ROLE_KEY)
    sessionStorage.removeItem('session_role')
  }
  localStorage.removeItem('session_role')
  dispatchUserSessionCleared({ role: r })

  setAuthNotice(reason)

  const targetPath = r === 'ADMIN' ? '/admin/login' : '/login'
  const currentPath = router.currentRoute?.value?.path
  if (currentPath === targetPath) {
    return
  }
  const now = Date.now()
  if (isLogoutRedirecting && now - lastLogoutNoticeAt < 1000) {
    return
  }
  isLogoutRedirecting = true
  lastLogoutNoticeAt = now
  router
    .replace(targetPath)
    .catch(() => {})
    .finally(() => {
      isLogoutRedirecting = false
    })
}

const getBaseURL = () => {
  const explicitBase = import.meta.env.VITE_API_BASE || import.meta.env.VITE_APP_BASE_API
  return explicitBase || '/api'
}

const service: AxiosInstance = axios.create({
  baseURL: getBaseURL(),
  timeout: 300000,
  withCredentials: false,
})

const ERROR_TOAST_DEDUPE_MS = 1200
const lastErrorAtByMsg = new Map<string, number>()

const notifyError = (msg: string) => {
  const normalized = (msg || '').trim()
  if (!normalized) return
  const now = Date.now()
  const last = lastErrorAtByMsg.get(normalized) || 0
  if (now - last < ERROR_TOAST_DEDUPE_MS) return
  lastErrorAtByMsg.set(normalized, now)
  ElMessage.error(normalized)
}

const isBizUnauthorized = (code?: number) => {
  if (code === undefined || code === null) {
    return false
  }
  if (code === 401) {
    return true
  }
  const normalized = String(code)
  return normalized.startsWith('401')
}

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const role = resolveRole(config as RoleAwareConfig)
    const token = localStorage.getItem(storageKeys(role).token)
    const isPublicEndpoint = isAuthFreeEndpoint(config.url)
    if (config.url && !isPublicEndpoint && token) {
      config.headers = config.headers ?? {}
      config.headers.Authorization = `Bearer ${token}`
      config.headers['X-Session-Role'] = role
    }
    return config
  },
  error => Promise.reject(error),
)

const handleUnauthorized = (config: any) => {
  const role = resolveRole(config as RoleAwareConfig)
  const keys = storageKeys(role)
  if (isRefreshingToken) {
    return new Promise((resolve, reject) => {
      subscribeTokenRefresh(newToken => {
        if (newToken) {
          config.headers = config.headers ?? {}
          config.headers.Authorization = `Bearer ${newToken}`
          resolve(service(config))
        } else {
          reject(new Error('Token刷新失败'))
        }
      })
    })
  }

  isRefreshingToken = true
  return new Promise((resolve, reject) => {
    const refreshToken = localStorage.getItem(keys.refresh)
    if (!refreshToken) {
      isRefreshingToken = false
      logout(role, 'expired')
      reject(new Error('没有refresh token，需要重新登录'))
      return
    }

    service
      .post(
        '/user/refresh-token',
        { refreshToken },
        {
          headers: {
            'X-Session-Role': role,
          },
        },
      )
      .then((res: any) => {
        const newAccessToken = res?.data?.accessToken
        const newRefreshToken = res?.data?.refreshToken
        const newExpireAt = res?.data?.expireTime ?? res?.data?.accessTokenExpiresAt
        if (newAccessToken) {
          localStorage.setItem(keys.token, newAccessToken)
          if (newRefreshToken) {
            localStorage.setItem(keys.refresh, newRefreshToken)
          }
          if (newExpireAt) {
            localStorage.setItem(keys.expire, String(newExpireAt))
          } else {
            localStorage.removeItem(keys.expire)
          }
          rememberTabRole(role)
          isRefreshingToken = false
          dispatchUserSessionRefreshed({
            role,
            accessToken: newAccessToken,
            refreshToken: newRefreshToken,
            expireTime: newExpireAt,
            source: 'request',
          })
          notifyTokenRefreshed(newAccessToken)
          config.headers = config.headers ?? {}
          config.headers.Authorization = `Bearer ${newAccessToken}`
          resolve(service(config))
        } else {
          isRefreshingToken = false
          notifyTokenRefreshed(null)
          logout(role, 'refresh_failed')
          reject(new Error('Token刷新失败'))
        }
      })
      .catch(error => {
        isRefreshingToken = false
        notifyTokenRefreshed(null)
        logout(role, 'refresh_failed')
        reject(error)
      })
  })
}

service.interceptors.response.use(
  ((response: AxiosResponse<ApiResult>) => {
    const key = getRequestKey(response.config)
    removePending(key)

    if (response.config.url) {
      response.config.url = response.config.url.replace('/api', '')
    }

    const contentType = String((response.headers as any)?.['content-type'] ?? '')
    if (typeof response.data === 'string' && contentType.includes('text/html')) {
      const url = response.config?.url || ''
      const msg = `接口返回HTML（可能未命中Vite代理或后端未启动）：${url}`
      notifyError('接口返回异常，请检查后端服务与代理配置')
      return Promise.reject(new Error(msg))
    }

    const data = response.data as ApiResult
    const skipAuthRecovery = isAuthFreeEndpoint(response.config.url)

    // 401 或业务未授权：自动处理（跳登录或刷新）
    if (isBizUnauthorized(data.code) && !skipAuthRecovery) {
      return handleUnauthorized(response.config)
    }

    if (data.code !== undefined && data.code !== 200) {
      const msg = data.msg || '请求失败'
      notifyError(msg)
      return Promise.reject(new Error(msg))
    }

    return data
  }) as any,
  error => {
    if (error.config) {
      if (error.config.url) {
        error.config.url = error.config.url.replace('/api', '')
      }
      const key = getRequestKey(error.config)
      removePending(key)
    }

    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        const msg = data?.msg || '未授权'

        // refresh-token 自身失败
        if (error.config?.url?.endsWith('/user/refresh-token')) {
          const role = resolveRole(error.config as RoleAwareConfig)
          logout(role, 'refresh_failed')
          return Promise.reject(new Error(msg || 'Token刷新失败'))
        }

        // 静态白名单接口：直接提示
        if (isAuthFreeEndpoint(error.config?.url)) {
          notifyError(msg)
          return Promise.reject(new Error(msg))
        }

        // 401：尝试自动刷新（失败会在 handleUnauthorized 内部 logout）
        return handleUnauthorized(error.config)
      }

      // 【403权限拒绝】跳转到禁止访问页面
      if (status === 403) {
        const msg = data?.msg || '禁止访问'
        const role = resolveRole(error.config as RoleAwareConfig)
        
        // 如果是登录用户，重定向到403页面
        if (role) {
          const currentPath = typeof window !== 'undefined' ? window.location.pathname : ''
          notifyError(msg)
          router
            .push({
              path: '/forbidden',
              query: {
                redirect: currentPath,
                code: '403',
                message: msg,
                reason: '您没有权限访问此资源',
              },
            })
            .catch(() => {})
          return Promise.reject(new Error(msg))
        }
      }

      const messageMap: Record<number, string> = {
        400: '请求参数错误',
        404: '请求的资源不存在',
        405: '请求方法不允许',
        500: '服务器内部错误',
        502: '网关错误',
        503: '服务不可用',
      }

      if (status === 403) {
        const msg403 = data?.msg || '没有权限'
        notifyError(msg403)
        return Promise.reject(new Error(msg403))
      }

      const msg = data?.msg || messageMap[status] || `请求错误：${status}`
      notifyError(msg)
      return Promise.reject(new Error(msg))
    } else {
      if (error.message === 'Network Error') {
        notifyError('网络连接错误，请检查网络')
      } else if (error.code === 'ECONNABORTED') {
        notifyError('请求超时，请稍后重试')
      } else if (error.message !== '重复请求，已取消') {
        notifyError(error.message || '未知错误')
        return Promise.reject(new Error('请求超时，请稍后重试'))
      }
      return Promise.reject(new Error(error.message || '未知错误'))
    }
  },
)

export default service
