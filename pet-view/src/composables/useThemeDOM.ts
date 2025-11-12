import type { Ref } from 'vue'
import type { ThemePreferences } from '@/stores/theme'

const adjustColor = (color: string, amount: number): string => {
  if (color.startsWith('#')) {
    const num = parseInt(color.replace('#', ''), 16)
    const r = Math.max(0, Math.min(255, (num >> 16) + amount))
    const g = Math.max(0, Math.min(255, ((num >> 8) & 0x00FF) + amount))
    const b = Math.max(0, Math.min(255, (num & 0x0000FF) + amount))
    return `#${((r << 16) | (g << 8) | b).toString(16).padStart(6, '0')}`
  }
  return color
}

export const useThemeDOM = (preferences: Ref<ThemePreferences>) => {
  const updateHeaderAndSidebarTheme = () => {
    setTimeout(() => {
      const topHeader = document.querySelector('.top-header') as HTMLElement
      if (topHeader) {
        topHeader.style.background = `linear-gradient(135deg, ${preferences.value.primaryColor} 0%, ${adjustColor(preferences.value.primaryColor, -20)} 100%)`
      }

      const sidebar = document.querySelector('.sidebar') as HTMLElement
      if (sidebar) {
        if (preferences.value.theme === 'dark') {
          sidebar.style.background = 'linear-gradient(180deg, #1f1f1f 0%, #141414 100%)'
        } else {
          sidebar.style.background = 'linear-gradient(180deg, #ffffff 0%, #f8f9fa 100%)'
        }
      }

      const activeNavLinks = document.querySelectorAll('.nav-link.active')
      activeNavLinks.forEach(link => {
        (link as HTMLElement).style.background = `linear-gradient(135deg, ${preferences.value.primaryColor} 0%, ${adjustColor(preferences.value.primaryColor, -20)} 100%)`
      })

      const dropdownHeader = document.querySelector('.user-dropdown-header') as HTMLElement
      if (dropdownHeader) {
        dropdownHeader.style.background = `linear-gradient(135deg, ${preferences.value.primaryColor} 0%, ${adjustColor(preferences.value.primaryColor, -20)} 100%)`
      }

      const primaryButtons = document.querySelectorAll('.el-button--primary')
      primaryButtons.forEach(button => {
        (button as HTMLElement).style.backgroundColor = preferences.value.primaryColor
        ;(button as HTMLElement).style.borderColor = preferences.value.primaryColor
      })

      document.documentElement.style.setProperty('--el-color-primary', preferences.value.primaryColor)
      document.documentElement.style.setProperty('--el-color-primary-light-3', adjustColor(preferences.value.primaryColor, 30))
      document.documentElement.style.setProperty('--el-color-primary-light-5', adjustColor(preferences.value.primaryColor, 50))
      document.documentElement.style.setProperty('--el-color-primary-light-7', adjustColor(preferences.value.primaryColor, 70))
      document.documentElement.style.setProperty('--el-color-primary-light-8', adjustColor(preferences.value.primaryColor, 80))
      document.documentElement.style.setProperty('--el-color-primary-light-9', adjustColor(preferences.value.primaryColor, 90))
      document.documentElement.style.setProperty('--el-color-primary-dark-2', adjustColor(preferences.value.primaryColor, -20))
    }, 100)
  }

  const applyThemeDOM = () => {
    document.documentElement.style.setProperty('--el-color-primary', preferences.value.primaryColor)

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
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
      if (prefersDark) {
        if (!htmlHasDark) document.documentElement.classList.add('dark')
        if (htmlHasLight) document.documentElement.classList.remove('light')
      } else {
        if (!htmlHasLight) document.documentElement.classList.add('light')
        if (htmlHasDark) document.documentElement.classList.remove('dark')
      }
    }

    const currentFontSize = document.documentElement.style.fontSize
    const newFontSize = preferences.value.fontSize + 'px'
    if (currentFontSize !== newFontSize) {
      document.documentElement.style.setProperty('--base-font-size', newFontSize)
      document.documentElement.style.fontSize = newFontSize
    }

    const hasAnimations = preferences.value.animations
    const hasNoAnimationsClass = document.documentElement.classList.contains('no-animations')
    if (!hasAnimations && !hasNoAnimationsClass) {
      document.documentElement.style.setProperty('--el-transition-duration', '0s')
      document.documentElement.classList.add('no-animations')
    } else if (hasAnimations && hasNoAnimationsClass) {
      document.documentElement.style.removeProperty('--el-transition-duration')
      document.documentElement.classList.remove('no-animations')
    }

    const hasCompactMode = preferences.value.compactMode
    const hasCompactModeClass = document.documentElement.classList.contains('compact-mode')
    if (hasCompactMode && !hasCompactModeClass) {
      document.documentElement.classList.add('compact-mode')
    } else if (!hasCompactMode && hasCompactModeClass) {
      document.documentElement.classList.remove('compact-mode')
    }

    setTimeout(() => {
      updateHeaderAndSidebarTheme()
    }, 0)
  }

  return { applyThemeDOM, updateHeaderAndSidebarTheme }
}

