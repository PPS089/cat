import { ref, onUnmounted, computed } from 'vue'
import { ElNotification } from 'element-plus'
import { useI18n } from 'vue-i18n'
import type { HealthAlert } from '@/types/health'

/**
 * WebSocket连接管理
 */
export const useWebSocket = () => {
  const { t } = useI18n()
  const ws = ref<WebSocket | null>(null)
  const isConnected = ref(false)
  const reconnectTimer = ref<NodeJS.Timeout | null>(null)
  const reconnectAttempts = ref(0)
  const maxReconnectAttempts = 5
  
  // WebSocket连接配置
  const wsUrl = ref('')
  const userId = ref('')
  
  // 连接状态
  const connectionStatus = computed(() => {
    if (isConnected.value) return 'connected'
    if (reconnectAttempts.value > 0) return 'reconnecting'
    return 'disconnected'
  })
  
  /**
   * 创建WebSocket连接
   */
  const createWebSocketConnection = (id: string) => {
    if (ws.value && ws.value.readyState === WebSocket.OPEN) {
      console.log('WebSocket已连接，无需重复连接')
      return
    }
    
    userId.value = id
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    
    // 动态端口适配：如果当前端口不是5174，使用当前端口
    const currentPort = window.location.port
    const targetPort = currentPort && currentPort !== '5174' ? currentPort : '5174'
    const targetHost = targetPort ? `${window.location.hostname}:${targetPort}` : window.location.host
    
    // 使用/api前缀通过Vite代理连接WebSocket
    wsUrl.value = `${protocol}//${targetHost}/api/ws/${id}`
    console.log(`WebSocket连接URL: ${wsUrl.value}`)
    
    try {
      ws.value = new WebSocket(wsUrl.value)
      
      ws.value.onopen = () => {
        console.log('WebSocket连接已建立')
        isConnected.value = true
        reconnectAttempts.value = 0
        if (reconnectTimer.value) {
          clearTimeout(reconnectTimer.value)
          reconnectTimer.value = null
        }
      }
      
      ws.value.onmessage = (event) => {
        console.log('收到WebSocket消息:', event.data)
        handleWebSocketMessage(event.data)
      }
      
      ws.value.onclose = () => {
        console.log('WebSocket连接已关闭')
        isConnected.value = false
        ws.value = null
        // 尝试重连
        attemptReconnect()
      }
      
      ws.value.onerror = (error) => {
        console.error('WebSocket连接错误:', error)
        isConnected.value = false
      }
      
    } catch (error) {
      console.error('创建WebSocket连接失败:', error)
      attemptReconnect()
    }
  }
  
  /**
   * 处理WebSocket消息
   */
  const handleWebSocketMessage = (message: string) => {
    console.log('处理WebSocket消息:', message)
    
    // 检查是否为健康提醒相关消息
    if (message.includes('健康提醒') || message.includes('健康告警')) {
      showHealthAlertNotification(message)
      // 存储健康提醒消息到本地存储
      storeHealthAlertMessage(message)
    } else if (message.includes('寄养') || message.includes('领养')) {
      // 处理寄养/领养相关消息
      showPetCareNotification(message)
    } else {
      // 其他消息显示普通通知
      ElNotification.info({
        title: t('common.notification'),
        message: message,
        duration: 5000,
        position: 'top-right'
      })
    }
    
    // 存储所有消息到本地存储用于历史记录
    storeWebSocketMessage(message)
  }
  
  /**
   * 显示健康提醒通知
   */
  const showHealthAlertNotification = (message: string) => {
    ElNotification.warning({
      title: '健康提醒',
      message: message,
      duration: 0, // 不自动关闭，需要用户手动关闭
      position: 'top-right',
      showClose: true,
      customClass: 'health-alert-notification',
      onClose: () => {
        console.log('健康提醒通知已关闭')
        // 标记消息为已读
        markHealthAlertAsRead(message)
      }
    })
  }
  
  /**
   * 显示宠物护理通知（寄养/领养）
   */
  const showPetCareNotification = (message: string) => {
    ElNotification.info({
      title: '宠物护理提醒',
      message: message,
      duration: 8000, // 8秒后自动关闭
      position: 'top-right',
      showClose: true,
      customClass: 'pet-care-notification'
    })
  }
  
  /**
   * 存储WebSocket消息到本地存储
   */
  const storeWebSocketMessage = (message: string) => {
    try {
      const messages = getStoredMessages()
      const messageData = {
        id: Date.now(),
        message,
        timestamp: new Date().toISOString(),
        type: getMessageType(message),
        read: false
      }
      messages.unshift(messageData) // 添加到开头
      
      // 只保留最近100条消息
      if (messages.length > 100) {
        messages.splice(100)
      }
      
      localStorage.setItem('websocket-messages', JSON.stringify(messages))
    } catch (error) {
      console.error('存储WebSocket消息失败:', error)
    }
  }
  
  /**
   * 存储健康提醒消息
   */
  const storeHealthAlertMessage = (message: string) => {
    try {
      const healthAlerts = getStoredHealthAlerts()
      const alertData = {
        hid: Date.now(),
        pid: extractPetIdFromMessage(message) || 0,
        alertType: extractAlertTypeFromMessage(message) || 'GENERAL',
        time: new Date().toISOString(),
        content: message,
        status: 'pending'
      }
      healthAlerts.unshift(alertData)
      
      // 只保留最近50条健康提醒
      if (healthAlerts.length > 50) {
        healthAlerts.splice(50)
      }
      
      localStorage.setItem('health_alerts', JSON.stringify(healthAlerts))
    } catch (error) {
      console.error('存储健康提醒消息失败:', error)
    }
  }
  
  /**
   * 获取存储的消息
   */
  const getStoredMessages = () => {
    try {
      const stored = localStorage.getItem('websocket-messages')
      return stored ? JSON.parse(stored) : []
    } catch (error) {
      console.error('获取存储的消息失败:', error)
      return []
    }
  }
  
  /**
   * 获取存储的健康提醒
   */
  const getStoredHealthAlerts = () => {
    try {
      const stored = localStorage.getItem('health_alerts')
      return stored ? JSON.parse(stored) : []
    } catch (error) {
      console.error('获取存储的健康提醒失败:', error)
      return []
    }
  }

  /**
   * 同步健康提醒到本地存储（从后端数据同步）
   * 重要：不可修改原始数据的字段名，必须保留所有不同种类的时间字段
   */
  const syncHealthAlertsToStorage = (healthAlerts: any[]) => {
    try {
      console.log('同步健康提醒到本地存储，原始数据:', healthAlerts)
      
      // 直接存储原始数据，不修改字段名
      // 因为 getScheduledReminders 会查找 reminderTime, checkDate, time 等字段
      const alertsToStore = healthAlerts.map((alert, index) => {
        console.log(`\n处理第 ${index + 1} 条记录:`, {
          healthId: alert.healthId,
          reminderTime: alert.reminderTime,
          checkDate: alert.checkDate,
          status: alert.status,
          description: alert.description
        })
        return {
          // 保留所有原始字段，确保 getScheduledReminders 能找到时间信息
          healthId: alert.healthId,
          hid: alert.hid || alert.healthId,
          pid: alert.pid,
          checkDate: alert.checkDate,      // 检查日期
          healthType: alert.healthType,
          description: alert.description,
          reminderTime: alert.reminderTime, // 关键：提醒时间
          status: alert.status,
          createdAt: alert.createdAt,
          updatedAt: alert.updatedAt,
          // 为了兼容旧字段
          time: alert.checkDate || alert.time,
          content: alert.description || alert.content,
          alertType: alert.healthType || alert.alertType
        }
      })
      
      console.log('储存的健康提醒格式:', alertsToStore)
      localStorage.setItem('health_alerts', JSON.stringify(alertsToStore))
      return alertsToStore
    } catch (error) {
      console.error('同步健康提醒到本地存储失败:', error)
      return []
    }
  }
  
  /**
   * 标记健康提醒为已读
   */
  const markHealthAlertAsRead = (message: string) => {
    try {
      const healthAlerts = getStoredHealthAlerts()
      const alert = healthAlerts.find((h: HealthAlert) => h.content === message)
      if (alert) {
        alert.status = 'completed'
      }
      localStorage.setItem('health_alerts', JSON.stringify(healthAlerts))
    } catch (error) {
      console.error('标记健康提醒为已读失败:', error)
    }
  }
  
  /**
   * 获取消息类型
   */
  const getMessageType = (message: string): string => {
    if (message.includes('健康提醒') || message.includes('健康告警')) {
      return 'health'
    } else if (message.includes('寄养')) {
      return 'foster'
    } else if (message.includes('领养')) {
      return 'adoption'
    } else {
      return 'general'
    }
  }
  
  /**
   * 从消息中提取宠物ID
   */
  const extractPetIdFromMessage = (message: string): number | null => {
    const match = message.match(/宠物\s*(\d+)/)
    return match ? parseInt(match[1]) : null
  }
  
  /**
   * 从消息中提取提醒类型
   */
  const extractAlertTypeFromMessage = (message: string): string => {
    if (message.includes('疫苗')) return 'VACCINE'
    if (message.includes('体检')) return 'CHECKUP'
    if (message.includes('手术')) return 'SURGERY'
    if (message.includes('疾病')) return 'DISEASE'
    return 'GENERAL'
  }
  
  /**
   * 尝试重新连接
   */
  const attemptReconnect = () => {
    if (reconnectAttempts.value >= maxReconnectAttempts) {
      console.log('WebSocket重连次数已达上限，停止重连')
      return
    }
    
    reconnectAttempts.value++
    const delay = Math.min(1000 * Math.pow(2, reconnectAttempts.value), 30000) // 指数退避，最大30秒
    
    console.log(`WebSocket将在 ${delay}ms 后尝试第 ${reconnectAttempts.value} 次重连`)
    
    reconnectTimer.value = setTimeout(() => {
      if (userId.value) {
        createWebSocketConnection(userId.value)
      }
    }, delay)
  }
  
  /**
   * 获取未完成的消息（未读的健康提醒）
   */
  const getIncompleteMessages = () => {
    try {
      const healthAlerts = getStoredHealthAlerts()
      // 这里需要根据实际的数据结构来判断是否已读，假设status为pending表示未读
      const incompleteAlerts = healthAlerts.filter((alert: HealthAlert) => alert.status === 'pending')
      console.log(`找到 ${incompleteAlerts.length} 条未完成的健康提醒`)
      console.log('所有健康提醒状态:', healthAlerts.map((alert: HealthAlert) => ({hid: alert.hid, status: alert.status, time: alert.time})))
      return incompleteAlerts
    } catch (error) {
      console.error('获取未完成消息失败:', error)
      return []
    }
  }
  
  /**
   * 解析不同的时间格式
   */
  const parseDateTime = (dateString: string): Date => {
    if (!dateString) return new Date(NaN)
    // 支持：yyyy-MM-dd HH:mm 和 2025-10-27T02:29:31 格式
    const normalized = dateString.replace(' ', 'T')
    return new Date(normalized)
  }
  
  /**
   * 获取定时提醒消息（即将到期的提醒）
   * 关键：提醒应该在 reminderTime 到达时触发
   */
  const getScheduledReminders = () => {
    try {
      const healthAlerts = getStoredHealthAlerts()
      const now = new Date()
      const reminderThreshold = 24 * 60 * 60 * 1000 // 24小时
      
      console.log('\n===== 定时提醒检查 =====')
      console.log('当前时间:', now.toISOString())
      console.log('存储的提醒数量:', healthAlerts.length)
      
      const upcomingReminders = healthAlerts.filter((alert: any) => {
        // 关键：使用 reminderTime（提醒时间）来判断是否应该触发
        const reminderTimeStr = alert.reminderTime
        if (!reminderTimeStr) {
          console.log(`跳过: "${alert.description}" - 没有设置提醒时间`)
          return false
        }
        
        const alertTime = parseDateTime(reminderTimeStr)
        if (isNaN(alertTime.getTime())) {
          console.log(`跳过: "${alert.description}" - 时间格式错误`)
          return false
        }
        
        const timeDiff = alertTime.getTime() - now.getTime()
        const minutesDiff = Math.round(timeDiff / 1000 / 60)
        
        // 逻辑：提醒时间已经过期（timeDiff <= 0），且在最近24小时内（timeDiff > -24h）
        // 包括normal、pending和critical状态的提醒都应该触发
        const shouldRemind = timeDiff <= 0 && timeDiff > -reminderThreshold && 
                           (alert.status === 'normal' || alert.status === 'pending' || alert.status === 'critical')
        
        console.log(`检查: "${alert.description}"`, {
          reminderTime: reminderTimeStr,
          timeDiff: `${minutesDiff}分钟`,
          shouldRemind: shouldRemind ? '✓ 应触发' : '✗ 不触发',
          status: alert.status
        })
        
        return shouldRemind
      })
      
      console.log(`\n找到 ${upcomingReminders.length} 条应触发的提醒\n`)
      return upcomingReminders
    } catch (error) {
      console.error('获取定时提醒失败:', error)
      return []
    }
  }
  
  /**
   * 发送定时提醒消息
   */
  const sendScheduledReminder = (alert: HealthAlert) => {
    try {
      const message = `⏰ 定时提醒: ${alert.description || alert.content || ''}`
      showHealthAlertNotification(message)
      
      // 记录已发送的提醒
      const sentReminders = JSON.parse(localStorage.getItem('sent-reminders') || '[]')
      sentReminders.push({
        alertId: alert.hid,
        sentAt: new Date().toISOString()
      })
      localStorage.setItem('sent-reminders', JSON.stringify(sentReminders))
      
      console.log('已发送定时提醒:', message)
    } catch (error) {
      console.error('发送定时提醒失败:', error)
    }
  }
  
  /**
   * 清理过期的已发送提醒记录（超过7天的记录）
   */
  const cleanupOldSentReminders = () => {
    try {
      const sentReminders = JSON.parse(localStorage.getItem('sent-reminders') || '[]')
      const now = new Date()
      const sevenDaysAgo = now.getTime() - (7 * 24 * 60 * 60 * 1000) // 7天前
      
      // 过滤掉超过7天的记录
      const filteredReminders = sentReminders.filter((sent: any) => {
        const sentTime = new Date(sent.sentAt).getTime()
        return sentTime > sevenDaysAgo
      })
      
      // 如果有记录被清理，更新localStorage
      if (filteredReminders.length < sentReminders.length) {
        localStorage.setItem('sent-reminders', JSON.stringify(filteredReminders))
        console.log(`清理了 ${sentReminders.length - filteredReminders.length} 条过期的已发送提醒记录`)
      }
    } catch (error) {
      console.error('清理过期提醒记录失败:', error)
    }
  }

  /**
   * 检查并发送定时提醒
   */
  const checkAndSendReminders = () => {
    try {
      // 定期清理过期记录
      cleanupOldSentReminders()
      
      const reminders = getScheduledReminders()
      const sentReminders = JSON.parse(localStorage.getItem('sent-reminders') || '[]')
      const now = new Date()
      
      reminders.forEach((alert: HealthAlert) => {
        // 检查是否已经发送过此提醒
        const alreadySent = sentReminders.some((sent: any) => {
          // 如果提醒ID匹配且发送时间在24小时内，则认为已发送
          if (sent.alertId === alert.hid) {
            const sentTime = new Date(sent.sentAt)
            const hoursSinceSent = (now.getTime() - sentTime.getTime()) / (1000 * 60 * 60)
            return hoursSinceSent < 24 // 24小时内的提醒视为已发送
          }
          return false
        })
        
        // 添加调试日志
        if (alreadySent) {
          console.log(`提醒 "${alert.description}" 已在24小时内发送过，跳过`)
        }
        
        if (!alreadySent) {
          sendScheduledReminder(alert)
        }
      })
    } catch (error) {
      console.error('检查并发送定时提醒失败:', error)
    }
  }
  
  /**
   * 重新发送未完成的消息
   */
  const resendIncompleteMessages = () => {
    try {
      const incompleteMessages = getIncompleteMessages()
      console.log(`重新发送 ${incompleteMessages.length} 条未完成的消息`)
      
      incompleteMessages.forEach((alert: HealthAlert) => {
        showHealthAlertNotification(alert.description || alert.content || '')
      })
    } catch (error) {
      console.error('重新发送未完成消息失败:', error)
    }
  }
  
  /**
   * 关闭WebSocket连接
   */
  const closeWebSocketConnection = () => {
    if (reconnectTimer.value) {
      clearTimeout(reconnectTimer.value)
      reconnectTimer.value = null
    }
    
    if (ws.value) {
      ws.value.close()
      ws.value = null
    }
    
    isConnected.value = false
    reconnectAttempts.value = 0
  }
  
  /**
   * 发送消息到服务器
   */
  const sendMessage = (message: string) => {
    if (ws.value && ws.value.readyState === WebSocket.OPEN) {
      ws.value.send(message)
      console.log('发送消息到服务器:', message)
    } else {
      console.error('WebSocket未连接，无法发送消息')
    }
  }
  
  // 组件卸载时关闭连接
  onUnmounted(() => {
    closeWebSocketConnection()
  })
  
  

 return {
    ws,
    isConnected,
    connectionStatus,
    createWebSocketConnection,
    closeWebSocketConnection,
    sendMessage,
    // 新增的消息管理函数
    getIncompleteMessages,
    getScheduledReminders,
    checkAndSendReminders,
    resendIncompleteMessages,
    sendScheduledReminder,
    getStoredMessages,
    getStoredHealthAlerts,
    markHealthAlertAsRead,
    syncHealthAlertsToStorage,
    cleanupOldSentReminders
  }
}