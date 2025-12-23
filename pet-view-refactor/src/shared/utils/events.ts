export const NAV_STATS_REFRESH_EVENT = 'nav-stats:refresh'
export const USER_SESSION_CLEARED_EVENT = 'user-session:cleared'
export const USER_SESSION_REFRESHED_EVENT = 'user-session:refreshed'

type SessionRole = 'ADMIN' | 'USER'

export interface NavStatsRefreshPayload {
  reason?: string
  timestamp?: number
}

export const dispatchNavStatsRefresh = (payload?: NavStatsRefreshPayload) => {
  if (typeof window === 'undefined') return
  window.dispatchEvent(
    new CustomEvent(NAV_STATS_REFRESH_EVENT, {
      detail: {
        reason: payload?.reason,
        timestamp: payload?.timestamp ?? Date.now(),
      },
    }),
  )
}

export interface UserSessionClearedPayload {
  role?: SessionRole
}

export const dispatchUserSessionCleared = (payload?: UserSessionClearedPayload) => {
  if (typeof window === 'undefined') return
  window.dispatchEvent(
    new CustomEvent(USER_SESSION_CLEARED_EVENT, {
      detail: payload,
    }),
  )
}

export interface UserSessionRefreshedPayload {
  role?: SessionRole
  accessToken: string
  refreshToken?: string
  expireTime?: number
  source?: 'store' | 'request'
}

export const dispatchUserSessionRefreshed = (payload: UserSessionRefreshedPayload) => {
  if (typeof window === 'undefined') return
  window.dispatchEvent(
    new CustomEvent(USER_SESSION_REFRESHED_EVENT, {
      detail: payload,
    }),
  )
}
