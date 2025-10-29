export { default as request } from './request'
import { useI18n } from 'vue-i18n'

/**
 * 时间格式化工具函数
 * @param date - 日期对象
 * @param fmt - 格式字符串，支持 yyyy-MM-dd HH:mm:ss 等格式
 * @returns 格式化后的时间字符串
 */
export function formatTime(date: Date, fmt: string): string {
  const o: { [key: string]: number } = {
    'M+': date.getMonth() + 1, // 月份
    'd+': date.getDate(), // 日
    'H+': date.getHours(), // 小时
    'h+': date.getHours(), // 小时
    'm+': date.getMinutes(), // 分
    's+': date.getSeconds(), // 秒
    'q+': Math.floor((date.getMonth() + 3) / 3), // 季度
    'S': date.getMilliseconds() // 毫秒
  };

  if (/(y+)/.test(fmt)) {
    fmt = fmt.replace(RegExp.$1, (date.getFullYear() + '').substr(4 - RegExp.$1.length));
  }

  for (let k in o) {
    if (new RegExp('(' + k + ')').test(fmt)) {
      fmt = fmt.replace(
        RegExp.$1,
        RegExp.$1.length === 1 ? o[k] + '' : ('00' + o[k]).substr(('' + o[k]).length)
      );
    }
  }

  return fmt;
}





/**
 * 日期格式化函数
 */
export const useFormatDate = () => {
  const { locale, t } = useI18n()
  
  const formatDate = (dateString: string) => {
    const lang = locale.value || 'zh-CN'
    const options: Intl.DateTimeFormatOptions = {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    }
    
    if (lang === 'en-US') {
      options.month = 'long'
    }
    
    // 处理时间戳（纯数字字符串）
    if (/^\d+$/.test(dateString)) {
      const timestamp = parseInt(dateString)
      // 判断是秒级还是毫秒级时间戳
      const date = timestamp > 9999999999 ? new Date(timestamp) : new Date(timestamp * 1000)
      return date.toLocaleDateString(lang, options)
    }
    
    // 处理无效日期
    if (!dateString) {
      return t('common.noDate')
    }
    
    // 处理中文字符串日期格式（如"2024年1月15日"）
    if (dateString.includes(t('year')) && dateString.includes(t('month')) && dateString.includes(t('day'))) {
      const match = dateString.match(new RegExp(`(\\d{4})${t('year')}(\\d{1,2})${t('month')}(\\d{1,2})${t('day')}`))
      if (match) {
        const year = parseInt(match[1])
        const month = parseInt(match[2]) - 1
        const day = parseInt(match[3])
        const date = new Date(year, month, day)
        return date.toLocaleDateString(lang, options)
      }
    }
    
    // 处理标准日期格式
    const date = new Date(dateString)
    if (isNaN(date.getTime())) {
      return dateString
    }
    
    return date.toLocaleDateString(lang, options)
  }
  
  return { formatDate }
}