export interface SpeakOptions {
  lang?: string
  rate?: number
  pitch?: number
  volume?: number
}

interface QueuedSpeechItem {
  text: string
  options: SpeakOptions
}

export const isSpeechSupported = () => {
  if (typeof window === 'undefined') return false
  return 'speechSynthesis' in window && 'SpeechSynthesisUtterance' in window
}

const getSpeechQueue = () => ((globalThis as any).__speechQueue ||= [] as QueuedSpeechItem[])

const flushSpeechQueue = () => {
  if (typeof window === 'undefined') return
  const queue = getSpeechQueue()
  if (!Array.isArray(queue) || queue.length === 0) return
  const last = queue[queue.length - 1]
  queue.splice(0, queue.length)
  speakCore(last.text, last.options)
}

const ensureSpeechUnlockListeners = () => {
  if (typeof window === 'undefined' || typeof document === 'undefined') return
  if ((globalThis as any).__speechUnlockListenerInstalled) return
  ;(globalThis as any).__speechUnlockListenerInstalled = true

  const unlock = () => {
    ;(globalThis as any).__speechUnlocked = true
    try {
      window.speechSynthesis.resume?.()
    } catch {
      // ignore
    }
    flushSpeechQueue()
  }

  document.addEventListener('click', unlock, { once: true, capture: true })
  document.addEventListener('keydown', unlock, { once: true, capture: true })
  document.addEventListener('touchstart', unlock, { once: true, capture: true })
}

const pickPreferredVoice = (langPrefix: string) => {
  const voices = window.speechSynthesis.getVoices?.() ?? []
  if (!voices.length) return undefined
  return voices.find(v => (v.lang || '').toLowerCase().startsWith(langPrefix))
}

const speakCore = (text: string, options: SpeakOptions = {}) => {
  if (!isSpeechSupported()) return false
  const normalized = (text || '').trim()
  if (!normalized) return false

  const utterance = new SpeechSynthesisUtterance(normalized)
  utterance.lang = options.lang ?? 'zh-CN'
  utterance.rate = options.rate ?? 1
  utterance.pitch = options.pitch ?? 1
  utterance.volume = options.volume ?? 1

  try {
    const preferred = pickPreferredVoice('zh')
    if (preferred) utterance.voice = preferred
  } catch {
    // ignore
  }

  try {
    window.speechSynthesis.cancel()
    window.speechSynthesis.resume?.()
    window.speechSynthesis.speak(utterance)
    return true
  } catch {
    return false
  }
}

const enqueueSpeech = (text: string, options: SpeakOptions) => {
  const normalized = (text || '').trim()
  if (!normalized) return
  const queue = getSpeechQueue()
  queue.push({ text: normalized, options })
  if (queue.length > 3) {
    queue.splice(0, queue.length - 3)
  }
}

export const speak = (text: string, options: SpeakOptions = {}) => {
  ensureSpeechUnlockListeners()
  const ok = speakCore(text, options)
  if (ok) {
    ;(globalThis as any).__speechUnlocked = true
    return true
  }
  enqueueSpeech(text, options)
  return false
}
