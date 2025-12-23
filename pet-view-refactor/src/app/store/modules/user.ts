import { defineStore } from 'pinia'
import { reactive, computed, ref } from 'vue'
import type { UserInfo } from '@/modules/profile/types'
import request, { TAB_SESSION_ROLE_KEY } from '@/shared/utils/request'
import dogImage from '@/assets/img/dog.jpg'
import {
  USER_SESSION_CLEARED_EVENT,
  USER_SESSION_REFRESHED_EVENT,
  dispatchUserSessionCleared,
  dispatchUserSessionRefreshed,
  type UserSessionRefreshedPayload,
} from '@/shared/utils/events'

interface LoginResponse {
  userId: number
  username: string
  accessToken: string
  refreshToken?: string
  expireTime?: number
  accessTokenExpiresAt?: number
  userInfo?: Partial<UserInfo>
  role?: string
  adminShelterId?: number | null
  ok: boolean
  message?: string
}

const DEFAULT_AVATAR = dogImage
const REFRESH_BUFFER_MS = 60 * 1000

type SessionRole = 'ADMIN' | 'USER'

const normalizeRole = (role?: string): SessionRole => {
  return role && role.toUpperCase() === 'ADMIN' ? 'ADMIN' : 'USER'
}

const getTabRole = (): SessionRole | undefined => {
  if (typeof window === 'undefined') return undefined
  const explicit = window.sessionStorage.getItem(TAB_SESSION_ROLE_KEY) || undefined
  if (explicit) return normalizeRole(explicit)
  const legacy = window.sessionStorage.getItem('session_role') || undefined
  return legacy ? normalizeRole(legacy) : undefined
}

const setTabRole = (role: SessionRole) => {
  if (typeof window === 'undefined') return
  window.sessionStorage.setItem(TAB_SESSION_ROLE_KEY, role)
  window.sessionStorage.setItem('session_role', role)
}

const clearTabRole = () => {
  if (typeof window === 'undefined') return
  window.sessionStorage.removeItem(TAB_SESSION_ROLE_KEY)
  window.sessionStorage.removeItem('session_role')
}

const roleSlug = (role: SessionRole) => (role === 'ADMIN' ? 'admin' : 'user')

const inferRoleFromLocation = (): SessionRole | undefined => {
  if (typeof window === 'undefined') return undefined
  const path = window.location?.pathname?.toLowerCase() || ''
  if (path.includes('/admin/')) return 'ADMIN'
  return undefined
}

const readSessionRole = (): SessionRole | undefined => getTabRole()

const detectInitialRole = (): SessionRole => {
  const sessionRole = readSessionRole()
  if (sessionRole) return sessionRole
  const locationRole = inferRoleFromLocation()
  if (locationRole) {
    setTabRole(locationRole)
    return locationRole
  }
  const stored = normalizeRole(localStorage.getItem('session_role') || undefined)
  if (stored) {
    setTabRole(stored)
    return stored
  }
  return 'USER'
}

const persistSessionRole = (role: SessionRole) => {
  setTabRole(role)
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

const clearRoleArtifacts = (role: SessionRole) => {
  if (typeof localStorage === 'undefined') {
    return
  }
  if (role === 'USER') {
    return
  }
  const adminKeys = ['admin_pets_filters', 'admin_articles_filters', 'admin_dashboard_state']
  adminKeys.forEach(key => localStorage.removeItem(key))
  if (typeof sessionStorage !== 'undefined') {
    const adminSessionKeys = ['admin_active_tab', 'admin_current_project']
    adminSessionKeys.forEach(key => sessionStorage.removeItem(key))
  }
}

const createEmptyInfo = (): UserInfo => ({
  userId: 0,
  userName: '',
  headPic: DEFAULT_AVATAR,
  email: '',
  phone: '',
  introduce: '',
  role: 'USER',
})

export const useUserStore = defineStore('user', () => {
  const refreshTimers: Partial<Record<SessionRole, number>> = {}
  const currentRole = ref<SessionRole>(detectInitialRole())
  const loadInitialInfo = (role: SessionRole) => {
    const keys = storageKeys(role)
    const persistedInfo = localStorage.getItem(keys.userInfo)
    let initialInfo = createEmptyInfo()
    if (persistedInfo) {
      try {
        const parsed = JSON.parse(persistedInfo)
        initialInfo = { ...initialInfo, ...parsed }
      } catch {
        initialInfo = createEmptyInfo()
      }
    }
    if (!initialInfo.userId) {
      const storedId = Number(localStorage.getItem(keys.userId) || 0)
      if (storedId) {
        initialInfo.userId = storedId
      }
    }
    initialInfo.role = role
    return initialInfo
  }
  const infoByRole = reactive<Record<SessionRole, UserInfo>>({
    USER: loadInitialInfo('USER'),
    ADMIN: loadInitialInfo('ADMIN'),
  })
  const sessionTokenByRole = reactive<Record<SessionRole, string>>({
    USER: localStorage.getItem(storageKeys('USER').token) || '',
    ADMIN: localStorage.getItem(storageKeys('ADMIN').token) || '',
  })
  const info = reactive<UserInfo>({ ...infoByRole[currentRole.value] })
  const syncCurrentInfo = (role: SessionRole = currentRole.value) => {
    Object.assign(info, infoByRole[role])
  }

  const profileLoaded = computed(() => info.userId > 0)
  const hasValidSession = computed(
    () => Boolean(sessionTokenByRole[currentRole.value] && info.userId > 0),
  )
  const isAdmin = computed(() => currentRole.value === 'ADMIN')

  const setActiveRole = (role: SessionRole) => {
    currentRole.value = role
    persistSessionRole(role)
    syncCurrentInfo(role)
  }

  const persistInfo = (role: SessionRole = currentRole.value) => {
    const keys = storageKeys(role)
    const target = infoByRole[role]
    localStorage.setItem(keys.userInfo, JSON.stringify(target))
    localStorage.setItem(keys.userName, target.userName)
    localStorage.setItem(keys.userId, target.userId.toString())
  }

  const resetInfoState = (role: SessionRole = currentRole.value) => {
    const target = infoByRole[role]
    target.userId = 0
    target.userName = ''
    target.headPic = DEFAULT_AVATAR
    target.email = ''
    target.phone = ''
    target.introduce = ''
    target.role = role
    if (role === currentRole.value) {
      syncCurrentInfo(role)
    }
  }

  const hydrateInfo = (data: Partial<UserInfo>, role: SessionRole = currentRole.value) => {
    const target = infoByRole[role]
    target.userName = data.userName || ''
    target.headPic = data.headPic || DEFAULT_AVATAR
    target.email = data.email || ''
    target.phone = data.phone || ''
    target.introduce = data.introduce || ''
    target.userId = data.userId ?? target.userId
    target.role = data.role || target.role || role
    target.adminShelterId = data.adminShelterId !== undefined ? data.adminShelterId : target.adminShelterId
    persistInfo(role)
    if (role === currentRole.value) {
      syncCurrentInfo(role)
    }
  }

  const fetchProfile = async (role: SessionRole = currentRole.value) => {
    if (!sessionTokenByRole[role]) return
    try {
      const response = await request.get('/user/profile', {
        headers: {
          'X-Session-Role': role,
        },
      })
      if (response?.data) {
        hydrateInfo(response.data, role)
      }
    } catch (error: any) {
      console.error('[UserStore] fetchProfile failed', error)
    }
  }

  const applyProfileUpdate = (updated: Partial<UserInfo>, role: SessionRole = currentRole.value) => {
    if (!updated) return
    const current = infoByRole[role]
    hydrateInfo(
      {
        userId: updated.userId ?? current.userId,
        userName: updated.userName ?? current.userName,
        headPic: updated.headPic ?? current.headPic,
        email: updated.email ?? current.email,
        phone: updated.phone ?? current.phone,
        introduce: updated.introduce ?? current.introduce,
        role: updated.role ?? current.role ?? role,
      },
      role,
    )
  }

  const getStoredExpireTime = (role: SessionRole = currentRole.value) => {
    const stored = localStorage.getItem(storageKeys(role).expire)
    return stored ? Number(stored) : undefined
  }

  const clearRefreshTimer = (role: SessionRole = currentRole.value) => {
    if (typeof window === 'undefined') {
      delete refreshTimers[role]
      return
    }
    if (refreshTimers[role]) {
      window.clearTimeout(refreshTimers[role])
      delete refreshTimers[role]
    }
  }

  const scheduleTokenRefresh = (role: SessionRole, expireAt?: number) => {
    if (typeof window === 'undefined') {
      return
    }
    clearRefreshTimer(role)
    if (!expireAt) {
      return
    }
    const delay = Math.max(expireAt - Date.now() - REFRESH_BUFFER_MS, 5000)
    refreshTimers[role] = window.setTimeout(() => {
      refreshSession(role).catch(() => {
        logout(role)
      })
    }, delay)
  }

  const applySessionTokens = (data: LoginResponse, role: SessionRole = currentRole.value) => {
    const keys = storageKeys(role)
    sessionTokenByRole[role] = data.accessToken
    localStorage.setItem(keys.token, data.accessToken)
    if (data.refreshToken) {
      localStorage.setItem(keys.refresh, data.refreshToken)
    }
    const expireAt = data.expireTime ?? data.accessTokenExpiresAt
    if (expireAt) {
      localStorage.setItem(keys.expire, expireAt.toString())
    } else {
      localStorage.removeItem(keys.expire)
    }
    scheduleTokenRefresh(role, expireAt ?? getStoredExpireTime(role))
    dispatchUserSessionRefreshed({
      role,
      accessToken: data.accessToken,
      refreshToken: data.refreshToken,
      expireTime: expireAt,
      source: 'store',
    })
  }

  const hydrateFromSession = (data: LoginResponse, role?: SessionRole) => {
    const resolvedRole = role ?? normalizeRole(data.userInfo?.role ?? data.role)
    const targetRole = resolvedRole || currentRole.value
    const hydrated = data.userInfo ?? {}
    const resolvedUserId = hydrated.userId ?? data.userId ?? infoByRole[targetRole].userId
    hydrateInfo(
      {
        userId: resolvedUserId,
        userName: hydrated.userName ?? data.username,
        headPic: hydrated.headPic,
        email: hydrated.email,
        phone: hydrated.phone,
        introduce: hydrated.introduce,
        role: hydrated.role ?? data.role ?? targetRole,
        adminShelterId: data.adminShelterId !== undefined ? data.adminShelterId : hydrated.adminShelterId,
      },
      targetRole,
    )
    if (resolvedUserId) {
      localStorage.setItem(storageKeys(targetRole).userId, resolvedUserId.toString())
    }
  }

  const refreshSession = async (role: SessionRole = currentRole.value) => {
    const keys = storageKeys(role)
    const refreshToken = localStorage.getItem(keys.refresh)
    if (!refreshToken) {
      throw new Error('缺少 refresh token')
    }
    const response = await request.post(
      '/user/refresh-token',
      { refreshToken },
      {
        headers: {
          'X-Session-Role': role,
        },
      },
    )
    const data = response.data as LoginResponse
    if (!data.accessToken) {
      throw new Error(data.message || 'Token刷新失败')
    }
    applySessionTokens(data, role)
    if (data.userInfo) {
      hydrateFromSession(data, role)
    }
    return data
  }

  const login = async (payload: { username: string; password: string }) => {
    const response = await request.post('/user/login', {
      username: payload.username,
      password: payload.password,
    })
    const data = response.data as LoginResponse
    if (!data.ok || !data.accessToken) {
      throw new Error(data.message || '登录失败')
    }
    const targetRole = normalizeRole(data.userInfo?.role ?? data.role)
    
    // 【权限一致性检查】验证JWT中的role与userInfo中的role是否一致
    if (data.userInfo?.role) {
      const userInfoRole = normalizeRole(data.userInfo.role)
      if (targetRole !== userInfoRole) {
        console.warn('[Auth] Role mismatch detected:', {
          tokenRole: targetRole,
          userInfoRole: userInfoRole,
          rawUserInfoRole: data.userInfo.role,
          rawRole: data.role,
        })
        // 权限不一致时，使用userInfo中的role作为准
        setActiveRole(userInfoRole)
        applySessionTokens(data, userInfoRole)
        hydrateFromSession(data, userInfoRole)
        return data
      }
    }
    
    setActiveRole(targetRole)
    applySessionTokens(data, targetRole)
    hydrateFromSession(data, targetRole)
    return data
  }

  const logout = (role: SessionRole = currentRole.value) => {
    sessionTokenByRole[role] = ''
    resetInfoState(role)
    clearRefreshTimer(role)
    const keys = storageKeys(role)
    localStorage.removeItem(keys.token)
    localStorage.removeItem(keys.refresh)
    localStorage.removeItem(keys.userId)
    localStorage.removeItem(keys.userInfo)
    localStorage.removeItem(keys.userName)
    localStorage.removeItem(keys.expire)
    clearRoleArtifacts(role)
    if (currentRole.value === role) {
      const fallback: SessionRole = role === 'ADMIN' ? 'USER' : 'ADMIN'
      if (sessionTokenByRole[fallback]) {
        setActiveRole(fallback)
      } else {
        clearTabRole()
      }
    }
    dispatchUserSessionCleared({ role })
  }

  if (typeof window !== 'undefined') {
    window.addEventListener(USER_SESSION_CLEARED_EVENT, event => {
      const detail = (event as CustomEvent<{ role?: SessionRole }>).detail
      const roles: SessionRole[] = detail?.role ? [detail.role] : ['USER', 'ADMIN']
      roles.forEach(role => {
        const keys = storageKeys(role)
        sessionTokenByRole[role] = localStorage.getItem(keys.token) || ''
        if (!sessionTokenByRole[role]) {
          resetInfoState(role)
          clearRefreshTimer(role)
          if (role === currentRole.value) {
            syncCurrentInfo(role)
          }
        }
      })
    })
    window.addEventListener(USER_SESSION_REFRESHED_EVENT, event => {
      const detail = (event as CustomEvent<UserSessionRefreshedPayload>).detail
      if (!detail || detail.source === 'store') {
        return
      }
      const role = detail.role ?? currentRole.value
      const keys = storageKeys(role)
      sessionTokenByRole[role] = detail.accessToken
      if (detail.refreshToken) {
        localStorage.setItem(keys.refresh, detail.refreshToken)
      }
      const expireAt = detail.expireTime ?? getStoredExpireTime(role)
      if (expireAt) {
        localStorage.setItem(keys.expire, expireAt.toString())
      }
      if (role === currentRole.value) {
        persistSessionRole(role)
      }
      scheduleTokenRefresh(role, expireAt)
    })
    scheduleTokenRefresh(currentRole.value, getStoredExpireTime(currentRole.value))
  }

  return {
    info,
    profileLoaded,
    hasValidSession,
    isAdmin,
    setActiveRole,
    fetchProfile,
    applyProfileUpdate,
    login,
    logout,
    refreshSession,
  }
})
