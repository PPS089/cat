const ABSOLUTE_URL = /^(https?:|blob:|data:)/i

const getAssetBase = () => {
  return (
    (import.meta.env.VITE_UPLOAD_BASE as string | undefined) ||
    (import.meta.env.VITE_ASSET_BASE_URL as string | undefined) ||
    (import.meta.env.VITE_FILE_BASE_URL as string | undefined) ||
    (import.meta.env.VITE_API_BASE as string | undefined) ||
    (import.meta.env.VITE_APP_BASE_API as string | undefined) ||
    ''
  )
}

const normalizeBase = (base: string) => {
  if (!base) return ''
  if (ABSOLUTE_URL.test(base)) {
    return base.replace(/\/$/, '')
  }
  const normalized = base.startsWith('/') ? base : `/${base}`
  if (typeof window !== 'undefined' && window.location?.origin) {
    return `${window.location.origin}${normalized === '/' ? '' : normalized}`
  }
  return normalized
}

export const resolveAssetUrl = (input?: string | null): string => {
  if (!input) return ''
  if (ABSOLUTE_URL.test(input)) return input
  const normalizedPath = input.startsWith('/') ? input : `/${input}`
  const base = normalizeBase(getAssetBase())
  if (!base) return normalizedPath
  return `${base}${normalizedPath}`
}
