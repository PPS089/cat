import { createI18n } from 'vue-i18n'
import zhCN from './zh-CN'
import enUS from './en-US'
import { watch } from 'vue'

// 步骤1.1：读取localStorage中保存的语言（键为'app-locale'）
// 若未保存过，默认使用'zh-CN'
const savedLocale = localStorage.getItem('app-locale') || 'zh-CN'
console.log('从localStorage加载语言:', savedLocale)

const messages = {
  'zh-CN': zhCN,
  'en-US': enUS
}

const i18n = createI18n({
  legacy: false,
  locale: savedLocale, // 初始语言 = 本地存储的语言（或默认）
  fallbackLocale: 'zh-CN',
  messages,
  missingWarn: true,
  fallbackWarn: true
})

// 监听语言变化并保存到localStorage
watch(() => i18n.global.locale, (newLocale) => {
  localStorage.setItem('app-locale', newLocale.value)
  console.log('语言已切换为:', newLocale.value)
})

export default i18n