export const SUPPORTED_LOCALES = ['zh-CN', 'en-US'] as const

export type SupportedLocale = (typeof SUPPORTED_LOCALES)[number]

export function normalizeLocale(value?: string | null): SupportedLocale {
  if (!value) {
    return 'zh-CN'
  }
  const lower = value.toLowerCase()
  if (lower === 'zh' || lower === 'zh-cn' || lower === 'zh_cn') {
    return 'zh-CN'
  }
  if (lower === 'en' || lower === 'en-us' || lower === 'en_us') {
    return 'en-US'
  }
  return SUPPORTED_LOCALES.includes(value as SupportedLocale) ? (value as SupportedLocale) : 'zh-CN'
}
