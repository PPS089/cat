import type { App } from 'vue'
import i18n from '@/locales'

export default {
  install(app: App) {
    // 延迟初始化，确保Pinia已经安装
    app.config.globalProperties.$initTheme = async () => {
      const { useThemeStore } = await import('@/stores/theme')
      const themeStore = useThemeStore()
      
      // 初始化主题设置
      themeStore.loadPreferences()
      themeStore.applyTheme()
      
      // 设置语言
      console.log('初始化主题插件，当前语言:', themeStore.preferences.language)
      if (i18n.global.locale.value !== themeStore.preferences.language) {
        console.log('更新i18n语言为:', themeStore.preferences.language)
        i18n.global.locale.value = themeStore.preferences.language as any
      }
      
      // 提供全局访问主题存储的方法
      app.config.globalProperties.$theme = themeStore
      
      // 确保主题在多个时间点应用
      setTimeout(() => {
        themeStore.applyTheme()
      }, 100)
      
      setTimeout(() => {
        themeStore.applyTheme()
      }, 500)
      
      setTimeout(() => {
        themeStore.applyTheme()
      }, 1000)
      
      return themeStore
    }
    
    // 全局属性，方便在模板中访问
    app.provide('initTheme', app.config.globalProperties.$initTheme)
    
    // 立即初始化主题（在应用挂载后）
    setTimeout(() => {
      app.config.globalProperties.$initTheme()
    }, 0)
  }
}