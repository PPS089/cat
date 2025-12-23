import { ref, readonly, computed, watch } from 'vue'
import { useUserStore } from '@/app/store/modules/user'
import { dispatchNavStatsRefresh } from '@/shared/utils/events'

let gWs: WebSocket | null = null
let gTimer: ReturnType<typeof setTimeout> | null = null
let gHandlers: Record<string, (data: any) => void> = {}
let gState: 'connecting' | 'connected' | 'disconnected' | 'error' = 'disconnected'
let gUserId: string | null = null
let gAdminAuthSignature: string | null = null
let retryCount = 0
let heartbeatTimer: ReturnType<typeof setInterval> | null = null
let heartbeatTimeout: ReturnType<typeof setTimeout> | null = null

const MAX_RETRY = 5
const BASE_DELAY = 1000
const MAX_DELAY = 30000
const HEARTBEAT_INTERVAL = 30000
const HEARTBEAT_TIMEOUT = 35000

const getNotifTimeMap = () => ((globalThis as any).__lastNotifTime ||= {})

const resolveWsUrl = (userId: string) => {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const defaultUrl = `${protocol}//${window.location.host}/ws/${encodeURIComponent(userId)}`

  const template =
    (import.meta.env.VITE_WS_URL_TEMPLATE as string | undefined) ||
    (import.meta.env.VITE_APP_WS_URL_TEMPLATE as string | undefined)

  if (!template) return defaultUrl

  const replaceToken = (input: string, token: string, value: string) => input.split(token).join(value)
  return replaceToken(
    replaceToken(
      replaceToken(
        replaceToken(template, '{userId}', encodeURIComponent(userId)),
        '{protocol}',
        protocol,
      ),
      '{host}',
      window.location.host,
    ),
    '{hostname}',
    window.location.hostname,
  )
}

export function useWebSocket() {
  const userStore = useUserStore()
  const wsRef = ref(gWs)
  const stateRef = ref(gState)

  const updateState = (newState: typeof gState) => {
    gState = newState
    stateRef.value = newState
  }

  const connect = async (userId: string): Promise<boolean> => {
    if (!userId) return false
    if (gWs?.readyState === WebSocket.OPEN) {
      if (gUserId === userId) return true
      disconnect()
    }
    if (gState === 'connecting') return false
    updateState('connecting')
    gUserId = userId
    clearRetryTimer()

    try {
      const url = resolveWsUrl(userId)
      gWs = new WebSocket(url)
      wsRef.value = gWs

      gWs.onopen = () => {
        updateState('connected')
        retryCount = 0
        clearRetryTimer()
        startHeartbeat()
        sendAdminAuthIfNeeded()
      }

      gWs.onclose = e => {
        updateState('disconnected')
        stopHeartbeat()
        clearRetryTimer()
        if (e.code !== 1000 && e.code !== 1001) scheduleRetry()
      }

      gWs.onerror = () => {
        updateState('error')
        stopHeartbeat()
        clearRetryTimer()
      }

      gWs.onmessage = e => {
        if (e.data === 'pong') {
          handlePong()
          return
        }
        let data: any
        try {
          data = JSON.parse(e.data)
        } catch {
          data = { type: 'plain_text', content: e.data, timestamp: new Date().toISOString() }
        }
        handlePong()
        handleBusinessLogic(data)
        gHandlers[data.type]?.(data)
      }

      return true
    } catch (e) {
      updateState('error')
      scheduleRetry()
      return false
    }
  }

  const scheduleRetry = () => {
    if (retryCount >= MAX_RETRY || gTimer) return
    const delay = Math.min(BASE_DELAY * Math.pow(2, retryCount++), MAX_DELAY)
    gTimer = setTimeout(() => {
      gTimer = null
      if (gUserId) connect(gUserId)
    }, delay)
  }

  const emitNotification = (detail: any) => {
    window.dispatchEvent(new CustomEvent('app-notification', { detail }))
  }

  const handleBusinessLogic = (data: any) => {
    switch (data.type) {
      case 'welcome': {
        // const message = typeof data.message === 'string' ? data.message : ''
        // if (!message) return
        emitNotification({
          type: 'success',
          title: '欢迎回来',
          // message,
          duration: 4000,
        })
        return
      }
      case 'health_reminder': {
        triggerHealthAlert(data)
        window.dispatchEvent(new CustomEvent('health-reminder', { detail: data }))
        dispatchNavStatsRefresh({ reason: 'health-reminder' })
        return
      }
      case 'new_application': {
        // 只有管理员才显示新的申请通知
        if (!userStore.isAdmin) {
          return; // 非管理员不处理此类通知
        }
        
        // 处理新的领养或寄养申请通知
        const title = data.title || '新申请';
        const message = data.message || '您有一条新的申请需要处理';
        
        // 显示桌面通知
        if (typeof Notification !== 'undefined' && window.isSecureContext) {
          if (Notification.permission === 'granted') {
            new Notification(title, { body: message, icon: '/title.png' });
          } else if (Notification.permission !== 'denied') {
            Notification.requestPermission()
              .then(p => p === 'granted' && new Notification(title, { body: message, icon: '/title.png' }))
              .catch(() => {});
          }
        }
        
        // 显示应用内通知
        emitNotification({
          type: 'info',
          title,
          message,
          payload: data.data || {},
          duration: 0, // 不自动消失
        });
        
        // 触发自定义事件，让相关组件可以监听
        window.dispatchEvent(new CustomEvent('new-application', { detail: data }));
        
        // 刷新导航栏统计数据
        dispatchNavStatsRefresh({ reason: 'new-application' });
        return;
      }
      default:
    }
  }

  const triggerHealthAlert = (data: any) => {
    const msg = data.description || data.content || '健康提醒'
    const key = `${data.healthId || data.petId}_${msg}`
    const now = Date.now()
    const timeMap = getNotifTimeMap()
    if (timeMap[key] && now - timeMap[key] < 30000) return
    timeMap[key] = now

    if (typeof Notification !== 'undefined' && window.isSecureContext) {
      const notify = () => new Notification('健康提醒', { body: msg, icon: '/title.png' })
      if (Notification.permission === 'granted') {
        notify()
      } else if (Notification.permission !== 'denied') {
        Notification.requestPermission()
          .then(p => p === 'granted' && notify())
          .catch(() => {})
      }
    }

    emitNotification({
      type: 'health_alert',
      title: data.petName ? `${data.petName}` : '健康提醒',
      message: msg,
      payload: data,
      duration: 0,
    })
  }

  const send = (msg: any) => {
    if (gWs?.readyState !== WebSocket.OPEN) return false
    gWs.send(typeof msg === 'string' ? msg : JSON.stringify(msg))
    return true
  }

  const sendAdminAuthIfNeeded = () => {
    if (!gWs || gWs.readyState !== WebSocket.OPEN) return
    if (!gUserId) return
    const activeUserId = userStore.info.userId ? String(userStore.info.userId) : ''
    if (activeUserId && activeUserId !== gUserId) return
    if (!userStore.isAdmin) return

    const adminShelterId = userStore.info.adminShelterId ?? null
    const signature = `${gUserId}:${adminShelterId === null ? 'platform' : String(adminShelterId)}`
    if (gAdminAuthSignature === signature) return
    gAdminAuthSignature = signature
    send({ type: 'auth', adminShelterId })
  }

  const clearRetryTimer = () => {
    if (!gTimer) return
    clearTimeout(gTimer)
    gTimer = null
  }

  const disconnect = () => {
    clearRetryTimer()
    stopHeartbeat()
    retryCount = 0
    gWs?.close(1000, 'User Disconnect')
    gWs = wsRef.value = null
    updateState('disconnected')
    gUserId = null
    gAdminAuthSignature = null
  }

  const closeTemp = () => {
    clearRetryTimer()
    stopHeartbeat()
    if (gWs) {
      gWs.close(1000, 'Temp Close')
      gWs = wsRef.value = null
    }
    updateState('disconnected')
    gAdminAuthSignature = null
  }

  const startHeartbeat = () => {
    stopHeartbeat()
    if (gWs?.readyState !== WebSocket.OPEN) return
    heartbeatTimer = setInterval(() => {
      if (gWs?.readyState === WebSocket.OPEN) {
        send('ping')
        scheduleHeartbeatTimeout()
      }
    }, HEARTBEAT_INTERVAL)
    scheduleHeartbeatTimeout()
  }

  const scheduleHeartbeatTimeout = () => {
    if (heartbeatTimeout) clearTimeout(heartbeatTimeout)
    heartbeatTimeout = setTimeout(() => {
      closeTemp()
      scheduleRetry()
    }, HEARTBEAT_TIMEOUT)
  }

  const handlePong = () => {
    if (heartbeatTimeout) {
      clearTimeout(heartbeatTimeout)
      heartbeatTimeout = null
    }
  }

  const stopHeartbeat = () => {
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer)
      heartbeatTimer = null
    }
    if (heartbeatTimeout) {
      clearTimeout(heartbeatTimeout)
      heartbeatTimeout = null
    }
  }

  watch(
    () => userStore.info.userId,
    newId => {
      newId ? connect(newId.toString()) : disconnect()
    },
    { immediate: true },
  )

  watch(
    () => [userStore.isAdmin, userStore.info.adminShelterId],
    () => {
      sendAdminAuthIfNeeded()
    },
    { immediate: true },
  )

  return {
    ws: computed(() => gWs),
    connectionState: readonly(stateRef),
    connect,
    disconnect,
    closeTemp,
    send,
  }
}
