import type { App } from 'vue'
import { createI18n } from 'vue-i18n'
import zhCN from '@/locales/zh-CN'
import enUS from '@/locales/en-US'
import { normalizeLocale } from '@/locales/utils'

const messages = {
  'zh-CN': zhCN,
  zh: zhCN,
  'en-US': enUS,
  en: enUS,
}

export function setupI18n(app: App) {
  const savedValue = localStorage.getItem('app-locale')
  const normalized = normalizeLocale(savedValue)
  if (savedValue !== normalized) {
    localStorage.setItem('app-locale', normalized)
  }
  const i18n = createI18n({
    legacy: false,
    locale: normalized,
    fallbackLocale: 'zh-CN',
    messages,
  })
  app.use(i18n)
  return Promise.resolve()
}
