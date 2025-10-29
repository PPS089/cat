import { useThemeStore } from '@/stores/theme'
import type { ThemePreferences } from '@/stores/theme'

/**
 * 全局主题工具函数
 */

// 获取当前主题设置
export const useTheme = () => {
  const themeStore = useThemeStore()
  return themeStore.preferences
}

// 更新主题设置
export const updateTheme = (preferences: Partial<ThemePreferences>) => {
  const themeStore = useThemeStore()
  themeStore.updatePreferences(preferences)
}

// 重置主题到默认设置
export const resetTheme = () => {
  const themeStore = useThemeStore()
  themeStore.resetToDefault()
}

// 应用主题到DOM（通常不需要手动调用，store会自动处理）
export const applyTheme = () => {
  const themeStore = useThemeStore()
  themeStore.applyTheme()
}

// 获取主题色
export const getPrimaryColor = () => {
  const themeStore = useThemeStore()
  return themeStore.preferences.primaryColor
}

// 获取当前主题模式
export const getThemeMode = () => {
  const themeStore = useThemeStore()
  return themeStore.preferences.theme
}

// 判断是否为深色主题
export const isDarkTheme = () => {
  const themeStore = useThemeStore()
  const { theme } = themeStore.preferences
  
  if (theme === 'dark') return true
  if (theme === 'light') return false
  
  // 如果是auto模式，根据系统设置判断
  return window.matchMedia('(prefers-color-scheme: dark)').matches
}

// 判断是否为紧凑模式
export const isCompactMode = () => {
  const themeStore = useThemeStore()
  return themeStore.preferences.compactMode
}

// 获取字体大小
export const getFontSize = () => {
  const themeStore = useThemeStore()
  return themeStore.preferences.fontSize
}

// 获取语言设置
export const getLanguage = () => {
  const themeStore = useThemeStore()
  return themeStore.preferences.language
}

// 主题工具类
export class ThemeUtils {
  private themeStore = useThemeStore()
  
  get preferences() {
    return this.themeStore.preferences
  }
  
  updatePreferences(preferences: Partial<ThemePreferences>) {
    this.themeStore.updatePreferences(preferences)
  }
  
  updatePreference<K extends keyof ThemePreferences>(
    key: K,
    value: ThemePreferences[K]
  ) {
    this.themeStore.updatePreference(key, value)
  }
  
  resetToDefault() {
    this.themeStore.resetToDefault()
  }
  
  applyTheme() {
    this.themeStore.applyTheme()
  }
  
  isDarkTheme() {
    return isDarkTheme()
  }
  
  isCompactMode() {
    return this.themeStore.preferences.compactMode
  }
}

export default {
  useTheme,
  updateTheme,
  resetTheme,
  applyTheme,
  getPrimaryColor,
  getThemeMode,
  isDarkTheme,
  isCompactMode,
  getFontSize,
  getLanguage,
  ThemeUtils
}