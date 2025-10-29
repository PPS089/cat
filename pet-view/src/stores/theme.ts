import { defineStore } from 'pinia'
import { ref, readonly } from 'vue'

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
      console.error('保存设置到localStorage失败:', error)
    }
  }

  /**
   * 根据当前偏好设置更新整个页面的样式，包括：
   * - 主题色（CSS 变量）主题模式（深色/浅色） 字体大小动画效果 紧凑模式
   */
  const applyTheme = () => {
    try {
      // 设置主题色 CSS 变量
      document.documentElement.style.setProperty('--el-color-primary', preferences.value.primaryColor)
      
      // 设置主题模式 - 只在变化时操作 DOM，避免不必要的重绘
      const currentTheme = preferences.value.theme
      const htmlHasDark = document.documentElement.classList.contains('dark')
      const htmlHasLight = document.documentElement.classList.contains('light')
      
      if (currentTheme === 'dark') {
        if (!htmlHasDark) document.documentElement.classList.add('dark')
        if (htmlHasLight) document.documentElement.classList.remove('light')
      } else if (currentTheme === 'light') {
        if (!htmlHasLight) document.documentElement.classList.add('light')
        if (htmlHasDark) document.documentElement.classList.remove('dark')
      } else {
        // 跟随系统主题 - 使用 matchMedia 检测系统偏好
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
        if (prefersDark) {
          if (!htmlHasDark) document.documentElement.classList.add('dark')
          if (htmlHasLight) document.documentElement.classList.remove('light')
        } else {
          if (!htmlHasLight) document.documentElement.classList.add('light')
          if (htmlHasDark) document.documentElement.classList.remove('dark')
        }
      }
      
      // 设置字体大小 - 只在变化时更新，避免重复设置
      const currentFontSize = document.documentElement.style.fontSize
      const newFontSize = preferences.value.fontSize + 'px'
      if (currentFontSize !== newFontSize) {
        document.documentElement.style.setProperty('--base-font-size', newFontSize)
        document.documentElement.style.fontSize = newFontSize
      }
      
      // 应用动画设置 - 通过添加/移除 CSS 类来控制
      const hasAnimations = preferences.value.animations
      const hasNoAnimationsClass = document.documentElement.classList.contains('no-animations')
      if (!hasAnimations && !hasNoAnimationsClass) {
        // 禁用动画：设置过渡时间为 0 并添加禁用类
        document.documentElement.style.setProperty('--el-transition-duration', '0s')
        document.documentElement.classList.add('no-animations')
      } else if (hasAnimations && hasNoAnimationsClass) {
        // 启用动画：移除自定义设置和禁用类
        document.documentElement.style.removeProperty('--el-transition-duration')
        document.documentElement.classList.remove('no-animations')
      }
      
      // 应用紧凑模式 - 通过 CSS 类控制间距和尺寸
      const hasCompactMode = preferences.value.compactMode
      const hasCompactModeClass = document.documentElement.classList.contains('compact-mode')
      if (hasCompactMode && !hasCompactModeClass) {
        document.documentElement.classList.add('compact-mode')
      } else if (!hasCompactMode && hasCompactModeClass) {
        document.documentElement.classList.remove('compact-mode')
      }
      
      // 延迟更新顶部栏和侧边栏主题，避免阻塞主界面渲染
      // 使用 setTimeout 将非关键更新放到下一个事件循环
      setTimeout(() => {
        updateHeaderAndSidebarTheme()
      }, 0)
    } catch (error) {
      console.error('应用主题失败:', error)
    }
  }
  
  /**
   * 更新顶部栏和侧边栏的主题样式
   * 根据当前主题设置更新导航栏、侧边栏等组件的样式
   */
  const updateHeaderAndSidebarTheme = () => {
    // 延迟执行以确保 DOM 完全加载，特别是异步组件
    setTimeout(() => {
      // 更新顶部栏背景色为渐变效果
      const topHeader = document.querySelector('.top-header') as HTMLElement
      if (topHeader) {
        topHeader.style.background = `linear-gradient(135deg, ${preferences.value.primaryColor} 0%, ${adjustColor(preferences.value.primaryColor, -20)} 100%)`
      }
      
      // 更新侧边栏背景色 - 根据主题模式设置不同颜色
      const sidebar = document.querySelector('.sidebar') as HTMLElement
      if (sidebar) {
        if (preferences.value.theme === 'dark') {
          // 深色主题：深灰到黑色的渐变
          sidebar.style.background = 'linear-gradient(180deg, #1f1f1f 0%, #141414 100%)'
        } else {
          // 浅色主题：白色到浅灰的渐变
          sidebar.style.background = 'linear-gradient(180deg, #ffffff 0%, #f8f9fa 100%)'
        }
      }
      
      // 更新活跃导航项的背景色
      const activeNavLinks = document.querySelectorAll('.nav-link.active')
      activeNavLinks.forEach(link => {
        (link as HTMLElement).style.background = `linear-gradient(135deg, ${preferences.value.primaryColor} 0%, ${adjustColor(preferences.value.primaryColor, -20)} 100%)`
      })
      
      // 更新用户下拉菜单头部背景色
      const dropdownHeader = document.querySelector('.user-dropdown-header') as HTMLElement
      if (dropdownHeader) {
        dropdownHeader.style.background = `linear-gradient(135deg, ${preferences.value.primaryColor} 0%, ${adjustColor(preferences.value.primaryColor, -20)} 100%)`
      }
      
      // 更新所有主要按钮和元素的主题色
      const primaryButtons = document.querySelectorAll('.el-button--primary')
      primaryButtons.forEach(button => {
        (button as HTMLElement).style.backgroundColor = preferences.value.primaryColor
        ;(button as HTMLElement).style.borderColor = preferences.value.primaryColor
      })
      
      // 更新 Element Plus 组件的主题色 CSS 变量
      // 这些变量会被 Element Plus 组件自动使用
      document.documentElement.style.setProperty('--el-color-primary', preferences.value.primaryColor)
      document.documentElement.style.setProperty('--el-color-primary-light-3', adjustColor(preferences.value.primaryColor, 30))
      document.documentElement.style.setProperty('--el-color-primary-light-5', adjustColor(preferences.value.primaryColor, 50))
      document.documentElement.style.setProperty('--el-color-primary-light-7', adjustColor(preferences.value.primaryColor, 70))
      document.documentElement.style.setProperty('--el-color-primary-light-8', adjustColor(preferences.value.primaryColor, 80))
      document.documentElement.style.setProperty('--el-color-primary-light-9', adjustColor(preferences.value.primaryColor, 90))
      document.documentElement.style.setProperty('--el-color-primary-dark-2', adjustColor(preferences.value.primaryColor, -20))
    }, 100) // 100ms 延迟确保 DOM 更新完成
  }
  
  /**
   * 辅助函数：调整颜色亮度
   * 根据给定的十六进制颜色和亮度调整量，返回新的颜色值
   */
  const adjustColor = (color: string, amount: number): string => {
    // 简化的颜色调整函数 - 解析十六进制颜色并调整 RGB 分量
    if (color.startsWith('#')) {
      const num = parseInt(color.replace('#', ''), 16)
      const r = Math.max(0, Math.min(255, (num >> 16) + amount))  // 红色分量
      const g = Math.max(0, Math.min(255, ((num >> 8) & 0x00FF) + amount))  // 绿色分量
      const b = Math.max(0, Math.min(255, (num & 0x0000FF) + amount))  // 蓝色分量
      return `#${((r << 16) | (g << 8) | b).toString(16).padStart(6, '0')}`
    }
    return color
  }


    /**
   * 从 localStorage 加载用户保存的设置
   */
  const loadPreferences = () => {
    try {
      // 加载语言设置 
      const savedLanguage = localStorage.getItem('language')
      console.log('从localStorage加载语言:', savedLanguage)
      if (savedLanguage) {
        preferences.value.language = savedLanguage
      } else {
        console.log('未找到保存的语言，使用默认:', preferences.value.language)
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
          console.error('加载偏好设置失败:', error)
        }
      }
    } catch (error) {
      console.error('加载设置失败:', error)
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