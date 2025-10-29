/**
 * 性别显示工具函数
 * 统一处理不同格式的性别字段显示
 */

import { useI18n } from 'vue-i18n'
import { convertBackendToFrontendGender, safeConvertGender } from './genderConverter'

/**
 * 获取性别显示文本 - 统一处理不同格式
 * 支持：公/母、male/female 等不同格式
 * @param gender 性别值（可以是任何格式）
 * @returns 本地化的性别显示文本
 */
export const getDisplayGender = (gender: string): string => {
  const { t } = useI18n()
  
  try {
    // 首先尝试直接转换后端格式（公/母）
    if (gender === '公' || gender === '母') {
      const convertedGender = convertBackendToFrontendGender(gender as '公' | '母')
      return convertedGender === 'male' ? t('message.male') : t('message.female')
    }
    
    // 如果已经是前端格式（male/female），直接转换
    if (gender === 'male') {
      return t('message.male')
    } else if (gender === 'female') {
      return t('message.female')
    }
    
    // 如果格式无法识别，使用安全转换（从后端格式转换到前端格式）
    const convertedGender = safeConvertGender(gender, 'backend', 'frontend')
    return convertedGender === 'male' ? t('message.male') : t('message.female')
  } catch (error) {
    console.warn('性别转换失败:', error)
    return gender || t('message.unknown')
  }
}

/**
 * 批量获取性别显示文本
 * @param genders 性别值数组
 * @returns 本地化的性别显示文本数组
 */
export const getDisplayGenders = (genders: string[]): string[] => {
  return genders.map(gender => getDisplayGender(gender))
}