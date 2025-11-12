import { defineStore } from 'pinia'
import { ref, readonly } from 'vue'
import { useThemeDOM } from '@/composables/useThemeDOM'

/**
 * 主题偏好设置接口
 * 定义了用户可以自定义的所有界面设置选项
 */
export interface ThemePreferences {
  language: string          // 界面语言，如 'zh-CN', 'en-US'
  theme: 'light' | 'dark' | 'auto'  // 主题模式：浅色、深色、跟随系统
  primaryColor: string    // 主题主色调，十六进制颜色值
  fontSize: number        // 字体大小，单位 px
  animations: boolean      // 是否启用动画效果
  compactMode: boolean    // 是否启用紧凑模式（减少间距）
}


export const useThemeStore = defineStore('theme', () => {
  // 默认配置 - 应用首次加载时的默认设置
  const defaultPreferences: ThemePreferences = {
    language: 'zh-CN',      // 默认中文简体
    theme: 'light',         // 默认浅色主题
    primaryColor: '#409EFF', // Element Plus 默认蓝色
    fontSize: 14,           // 默认 14px 字体大小
    animations: true,       // 默认启用动画
    compactMode: false      // 默认不启用紧凑模式
  }

  // 当前偏好设置 - 使用 ref 创建响应式对象 初始值为默认配置的副本，
  const preferences = ref<ThemePreferences>({ ...defaultPreferences })
  
  /**
   * 保存当前设置到 localStorage
   */
  const savePreferences = () => {
    try {
      // 保存各项单独设置
      localStorage.setItem('language', preferences.value.language)
      localStorage.setItem('theme', preferences.value.theme)
      localStorage.setItem('primaryColor', preferences.value.primaryColor)
      
      // 保存完整的偏好设置对象（不含语言、主题、主题色，因为这些已经单独保存）
      localStorage.setItem('preferences', JSON.stringify({
        fontSize: preferences.value.fontSize,
        animations: preferences.value.animations,
        compactMode: preferences.value.compactMode
      }))

    } catch (error) {
      
    }
  }

  const { applyThemeDOM } = useThemeDOM(preferences)
  const applyTheme = () => {
    applyThemeDOM()
  }
  
  
  
  


    /**
   * 从 localStorage 加载用户保存的设置
   */
  const loadPreferences = () => {
    try {
      // 加载语言设置 
      const savedLanguage = localStorage.getItem('language')
      if (savedLanguage) {
        preferences.value.language = savedLanguage
      } else {
        
      }
      
      // 加载主题设置
      const savedTheme = localStorage.getItem('theme')
      if (savedTheme && ['light', 'dark', 'auto'].includes(savedTheme)) {
        preferences.value.theme = savedTheme as 'light' | 'dark' | 'auto'
      }
      
      // 加载主题色
      const savedPrimaryColor = localStorage.getItem('primaryColor')
      if (savedPrimaryColor) {
        preferences.value.primaryColor = savedPrimaryColor
      }
      
      // 加载其他偏好设置（字体大小、动画、紧凑模式）
      // 这些设置以 JSON 字符串形式存储在 localStorage 中
      const savedPreferences = localStorage.getItem('preferences')
      if (savedPreferences) {
        try {
          const parsed = JSON.parse(savedPreferences)
          Object.assign(preferences.value, parsed)
        } catch (error) {
          
        }
      }
    } catch (error) {
      
    }
  }


  /**
   * 更新单个设置项
   * 用于更新某个特定的偏好设置，如只改变语言或只改变主题色
   */
  const updatePreference = <K extends keyof ThemePreferences>(
    key: K,
    value: ThemePreferences[K]
  ) => {
    preferences.value[key] = value
    savePreferences()  // 保存到 localStorage
    applyTheme()       // 应用新的设置到页面
  }

  /**
   * 批量更新多个设置项
   * 用于一次性更新多个偏好设置，如从设置页面保存所有更改
   */
  const updatePreferences = (newPreferences: Partial<ThemePreferences>) => {
    Object.assign(preferences.value, newPreferences)  // 合并新设置
    savePreferences()  // 保存到 localStorage
    applyTheme()       // 应用所有更改到页面
  }

  /**
   * 重置所有设置为默认值
   */
  const resetToDefault = () => {
    preferences.value = { ...defaultPreferences }
    savePreferences()
    applyTheme()
  }

  // 移除自动监听，改为手动控制更新 

  // 返回所有可供外部使用的属性和方法
  return {
    preferences: readonly(preferences),  // 使用 readonly 防止外部直接修改
    loadPreferences,                     // 加载保存的设置
    updatePreference,                    // 更新单个设置项
    updatePreferences,                   // 批量更新设置
    applyTheme,                          // 应用主题设置
    resetToDefault,                      // 重置为默认设置
  }
})
