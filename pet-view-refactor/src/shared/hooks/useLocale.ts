import { useI18n } from 'vue-i18n'
import { computed } from 'vue'
import { normalizeLocale } from '@/locales/utils'
import type { SupportedLocale } from '@/locales/utils'

export function useLocale() {
  const { locale } = useI18n()
  const currentLocale = computed<SupportedLocale>({
    get: () => normalizeLocale(locale.value),
    set: value => {
      locale.value = value
      localStorage.setItem('app-locale', value)
    },
  })

  return {
    currentLocale,
    setLocale: (value: string) => {
      currentLocale.value = normalizeLocale(value)
    },
  }
}
