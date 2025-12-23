<template>
  <div class="global-notification-center" aria-live="polite" />
</template>

<script setup lang="ts">
import { onBeforeUnmount } from 'vue'
import { ElNotification } from 'element-plus'
import { useNotificationStore } from '@/app/store/modules/notification'

const notificationStore = useNotificationStore()

type NotificationType = 'success' | 'info' | 'warning' | 'error'

const allowedTypes: NotificationType[] = ['success', 'info', 'warning', 'error']

const resolveNotificationType = (rawType?: string): NotificationType => {
  if (!rawType) return 'info'
  const normalized = rawType.toString().toLowerCase()
  if (allowedTypes.includes(normalized as NotificationType)) {
    return normalized as NotificationType
  }
  if (normalized.includes('alert') || normalized.includes('warning')) {
    return 'warning'
  }
  if (normalized.includes('error') || normalized.includes('fail')) {
    return 'error'
  }
  if (normalized.includes('success') || normalized.includes('done')) {
    return 'success'
  }
  return 'info'
}

const APP_NOTIFICATION_EVENT = 'app-notification'

const handleNotificationDetail = (detail: any) => {
  const title = detail.title || 'Notification'
  const message = typeof detail.message === 'string' ? detail.message : ''
  const type = resolveNotificationType(detail.type)

  notificationStore.push({ title, message, type })

  ElNotification({
    title,
    message,
    type,
    duration: detail.duration ?? 5000,
  })
}

const handleNotification = (event: Event) => {
  handleNotificationDetail((event as CustomEvent).detail || {})
}

const flushPendingNotifications = () => {
  const queue = ((globalThis as any).__appNotificationQueue || []) as any[]
  if (!Array.isArray(queue) || queue.length === 0) return
  const pending = queue.splice(0, queue.length)
  pending.forEach(handleNotificationDetail)
}

if (typeof window !== 'undefined') {
  window.addEventListener(APP_NOTIFICATION_EVENT, handleNotification as EventListener)
  ;(globalThis as any).__appNotificationListenerReady = true
  flushPendingNotifications()
}

onBeforeUnmount(() => {
  if (typeof window !== 'undefined') {
    window.removeEventListener(APP_NOTIFICATION_EVENT, handleNotification as EventListener)
    ;(globalThis as any).__appNotificationListenerReady = false
  }
})
</script>

<style scoped>
.global-notification-center {
  position: fixed;
  top: 1rem;
  right: 1rem;
  z-index: 9999;
  pointer-events: none;
}
</style>
