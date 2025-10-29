<script setup lang="ts">
import { onMounted, inject, ref, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import WebSocketReminderScheduler from '@/components/WebSocketReminderScheduler.vue'

// 初始化主题
const initTheme = inject<() => Promise<any>>('initTheme')
const { locale } = useI18n()
const themeStoreRef = ref<any>(null)


onMounted(async () => {
  // 延迟初始化主题，确保Pinia已经安装
  if (initTheme) {
    const themeStore = await initTheme()
    themeStoreRef.value = themeStore
    // 设置当前语言
    locale.value = themeStore.preferences.language as any
    
    // 确保语言文件已加载
    await nextTick()
  }
  
})



</script>

<template>
  <router-view/>
  <!-- WebSocket提醒调度器 -->
  <WebSocketReminderScheduler />
</template>

<style scoped>

</style>
