<template>
  <div class="websocket-reminder-scheduler">
    <!-- 隐藏的调度器组件 -->
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { useWebSocket } from '@/composables/useWebSocket'

const { 
  checkAndSendReminders, 
  resendIncompleteMessages
} = useWebSocket()

let reminderInterval: number | null = null
let incompleteCheckInterval: number | null = null

onMounted(() => {
  // 每10秒检查一次定时提醒（更频繁的检查）
  reminderInterval = window.setInterval(() => {
    checkAndSendReminders()
  }, 10000)
  
  // 每1分钟重新发送未完成的消息（更频繁的检查）
  incompleteCheckInterval = window.setInterval(() => {
    resendIncompleteMessages()
  }, 60000)
  
  // 立即执行一次检查
  setTimeout(() => {
    checkAndSendReminders()
    resendIncompleteMessages()
  }, 1000)
})

onUnmounted(() => {
  if (reminderInterval) {
    clearInterval(reminderInterval)
    reminderInterval = null
  }
  
  if (incompleteCheckInterval) {
    clearInterval(incompleteCheckInterval)
    incompleteCheckInterval = null
  }
})
</script>

<style scoped>
.websocket-reminder-scheduler {
  display: none;
}
</style>