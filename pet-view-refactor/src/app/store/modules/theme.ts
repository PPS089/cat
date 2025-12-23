import { defineStore } from 'pinia'

export type ThemeMode = 'light' | 'warm' | 'dark'

const CLASS_MAP: Record<ThemeMode, string> = {
  light: 'light',
  warm: 'warm',
  dark: 'dark',
}

const ORDERED_MODES: ThemeMode[] = ['light', 'warm', 'dark']

const LEGACY_CLASS_ALIASES = ['warm-sunrise', 'warm-dusk']
const LEGACY_MODE_ALIASES = ['warmsunrise', 'warmdusk', 'warm-sunrise', 'warm-dusk']

const normalizeMode = (raw: unknown): ThemeMode => {
  const value = (raw ?? '').toString().trim()
  if (!value) return 'light'
  const normalized = value.toLowerCase()
  if (normalized === 'auto') {
    if (typeof window !== 'undefined') {
      return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
    }
    return 'light'
  }
  if (normalized === 'dark') return 'dark'
  if (normalized === 'warm' || LEGACY_MODE_ALIASES.includes(normalized)) return 'warm'
  return 'light'
}

export const useThemeStore = defineStore('theme', {
  state: () => ({
    mode: normalizeMode(localStorage.getItem('theme-mode')),
  }),
  getters: {
    resolvedMode(state): ThemeMode {
      return state.mode
    },
    isDark(): boolean {
      return this.resolvedMode === 'dark'
    },
  },
  actions: {
    applyTheme() {
      const root = document.documentElement
      const resolved = normalizeMode(this.resolvedMode)
      const allClasses = Array.from(
        new Set([...Object.values(CLASS_MAP), ...LEGACY_CLASS_ALIASES]),
      )
      allClasses.forEach(cls => root.classList.remove(cls))
      root.classList.add(CLASS_MAP[resolved])
      localStorage.setItem('theme-mode', this.mode)
    },
    setMode(mode: ThemeMode) {
      this.mode = normalizeMode(mode)
      this.applyTheme()
    },
    toggle() {
      const currentIndex = ORDERED_MODES.indexOf(normalizeMode(this.mode))
      const nextMode = ORDERED_MODES[(currentIndex + 1) % ORDERED_MODES.length] ?? 'light'
      this.setMode(nextMode)
    },
  },
})
