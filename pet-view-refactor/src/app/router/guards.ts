// @ts-ignore
import type { Router } from 'vue-router'
import { useUserStore } from '@/app/store/modules/user'

// 定义路由名称集合，方便管理
const USER_GUEST_ROUTES = new Set(['login', 'register', 'forgot-password'])
const ADMIN_GUEST_ROUTES = new Set(['admin-login'])

type SessionRole = 'ADMIN' | 'USER'

// 保持你的辅助函数不变，用于在 Store 初始化前检查本地缓存
const hasStoredSession = (role: SessionRole) => {
  const slug = role === 'ADMIN' ? 'admin' : 'user'
  const token = localStorage.getItem(`${slug}_jwt_token`)
  const expireAtRaw = localStorage.getItem(`${slug}_jwt_expire_at`)
  const userId = Number(localStorage.getItem(`${slug}_userId`) || 0)

  if (!token || userId <= 0) {
    return false
  }

  if (expireAtRaw) {
    const expireAt = Number(expireAtRaw)
    if (!Number.isNaN(expireAt) && expireAt > 0 && Date.now() >= expireAt) {
      return false
    }
  }

  return true
}

export function setupRouterGuards(router: Router) {
  router.beforeEach(async (to, _from, next) => {
    const userStore = useUserStore()
    
    // 1. 获取当前状态（优先 Store，降级查 LocalStorage）
    // 注意：这里假设 userStore.isAdmin 在刷新页面后可能还没恢复，所以也需要查 LocalStorage
    // 用户登录态：必须存在 USER token + userId（避免管理员会话被误判成“用户已登录”）
    const isUserLoggedIn = hasStoredSession('USER') || (!userStore.isAdmin && userStore.hasValidSession)
    // 管理员登录态：必须存在 ADMIN token + userId（仅 isAdmin 不能代表已登录）
    const isAdminLoggedIn = hasStoredSession('ADMIN') || (userStore.isAdmin && userStore.hasValidSession)

    const requiresAuth = !to.meta.public
    const requiresAdmin = Boolean(to.meta.requiresAdmin)
    const routeName = String(to.name || '')
    const targetRole: SessionRole | undefined = to.path.startsWith('/admin')
      ? 'ADMIN'
      : to.path.startsWith('/user')
        ? 'USER'
        : undefined

    // 关键点：如果 LocalStorage 有会话，但 Pinia 仍停留在另一种角色，需要先切换角色
    if (targetRole === 'ADMIN' && isAdminLoggedIn && !userStore.isAdmin) {
      userStore.setActiveRole('ADMIN')
    }
    if (targetRole === 'USER' && isUserLoggedIn && userStore.isAdmin) {
      userStore.setActiveRole('USER')
    }

    // --- 场景 A: 访问受保护页面 (必须登录) ---
    if (requiresAuth) {
      // 没有任何登录状态 -> 去登录页
      if (!isUserLoggedIn && !isAdminLoggedIn) {
        const loginPath = targetRole === 'ADMIN' ? '/admin/login' : '/login'
        return next({ path: loginPath, query: { redirect: to.fullPath } })
      }

      // 如果 Store 里没数据但 LocalStorage 有（页面刷新），等待拉取用户信息
      if (!userStore.profileLoaded && (isUserLoggedIn || isAdminLoggedIn)) {
        // 按目标路由角色拉取 profile，避免默认 USER 导致管理员信息为空
        const profileRole = targetRole ?? (isAdminLoggedIn ? 'ADMIN' : 'USER')
        try {
          await userStore.fetchProfile(profileRole)
        } catch (error) {
          console.error('[RouterGuard] fetchProfile failed', error)
        }
        // 说明：userStore.fetchProfile 内部可能吞掉异常，因此用 profileLoaded 兜底判断“拉取失败”
        if (!userStore.profileLoaded) {
          const loginPath = targetRole === 'ADMIN' ? '/admin/login' : '/login'
          return next({ path: loginPath, query: { redirect: to.fullPath } })
        }
      }

      // 权限检查：如果是管理员页面但没有管理员身份
      if (requiresAdmin && !(userStore.isAdmin || isAdminLoggedIn)) {
        return next({ path: '/user' })
      }
      
      return next()
    }

    // --- 场景 B: 访问公共页面 (登录后互斥) ---
    // 1. 如果普通用户已登录，且试图访问 [登录, 注册, 忘记密码]
    if (isUserLoggedIn && USER_GUEST_ROUTES.has(routeName)) {
      return next('/user')
    }

    // 2. 如果管理员已登录，且试图访问 [管理员登录]
    if (isAdminLoggedIn && ADMIN_GUEST_ROUTES.has(routeName)) {
      if (!userStore.isAdmin) {
        userStore.setActiveRole('ADMIN')
      }
      return next('/admin')
    }
    // 放行其他所有情况
    return next()
  })
}
