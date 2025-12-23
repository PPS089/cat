import { computed, onMounted } from 'vue'
import { useThemeStore, type ThemeMode } from '@/app/store/modules/theme'

export function useTheme() {
  const themeStore = useThemeStore()
  const isDark = computed(() => themeStore.isDark)

  onMounted(() => {
    themeStore.applyTheme()
  })

  return {
    mode: computed(() => themeStore.mode),
    isDark,
    toggle: () => themeStore.toggle(),
    setMode: (mode: ThemeMode) => themeStore.setMode(mode),
  }
}
