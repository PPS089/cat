import { defineStore } from 'pinia'

export interface AppNotification {
  id: string
  title: string
  message: string
  type?: 'success' | 'warning' | 'info' | 'error'
  timestamp: number
}

export const useNotificationStore = defineStore('notifications', {
  state: () => ({
    items: [] as AppNotification[],
  }),
  getters: {
    latest: state => state.items.slice(0, 5),
  },
  actions: {
    push(notification: Omit<AppNotification, 'id' | 'timestamp'>) {
      this.items.unshift({
        id: crypto.randomUUID(),
        timestamp: Date.now(),
        ...notification,
      })
      if (this.items.length > 20) {
        this.items.pop()
      }
    },
  },
})
