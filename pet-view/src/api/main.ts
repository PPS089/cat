import { onMounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRoute } from 'vue-router'
import { useThemeStore } from '@/stores/theme'

// 强制重新计算导航状态
export const forceUpdateNavState = (themeStore: any) => {
  // 只需要确保主题正确应用，不需要强制重新渲染
  themeStore.applyTheme()
}

// 初始化用户布局
export const useUserLayout = () => {
  const userStore = useUserStore()
  const route = useRoute()
  const themeStore = useThemeStore()

  onMounted(async () => {
    // 加载用户数据
    await userStore.fetchProfile()
    
    // 应用主题
    themeStore.applyTheme()
    
    // 监听主题变化事件，重新应用主题
    window.addEventListener('app-rerender', () => {
      forceUpdateNavState(themeStore)
    })
    
    // 路由变化时重新应用主题和导航状态
    watch(() => route.path, () => {
      forceUpdateNavState(themeStore)
    }, { immediate: true })
  })

  return {
    userStore,
    route,
    themeStore,
    forceUpdateNavState: () => forceUpdateNavState(themeStore)
  }
}