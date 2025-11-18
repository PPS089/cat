/**
 * 性别字段转换工具函数
 * 处理前端与后端之间的性别数据格式转换
 */

/**
 * 前端性别格式（用于展示）
 */
export type FrontendGender = 'male' | 'female'

/**
 * 后端性别格式（用于存储）
 */
export type BackendGender = '公' | '母'

/**
 * 将前端性别格式转换为后端格式
 * @param frontendGender 前端性别 ('male' | 'female')
 * @returns 后端性别 ('公' | '母')
 */
export const convertFrontendToBackendGender = (frontendGender: FrontendGender): BackendGender => {
  const genderMap: Record<FrontendGender, BackendGender> = {
    'male': '公',
    'female': '母'
  }
  
  return genderMap[frontendGender]
}

/**
 * 将后端性别格式转换为前端格式
 * @param backendGender 后端性别 ('公' | '母')
 * @returns 前端性别 ('male' | 'female')
 */
export const convertBackendToFrontendGender = (backendGender: BackendGender): FrontendGender => {
  const genderMap: Record<BackendGender, FrontendGender> = {
    '公': 'male',
    '母': 'female'
  }
  
  return genderMap[backendGender]
}

/**
 * 安全转换性别值，支持多种格式
 * @param gender 性别值
 * @param from 来源类型 ('frontend' | 'backend')
 * @param to 目标类型 ('frontend' | 'backend')
 * @returns 转换后的性别值
 */
export const safeConvertGender = (
  gender: string, 
  from: 'frontend' | 'backend', 
  to: 'frontend' | 'backend'
): FrontendGender | BackendGender => {
  // 如果来源和目标相同，直接返回
  if (from === to) {
    return gender as FrontendGender | BackendGender
  }
  
  // 从前端转换为后端
  if (from === 'frontend' && to === 'backend') {
    if (gender === 'male' || gender === 'female') {
      return convertFrontendToBackendGender(gender as FrontendGender)
    }
    return '公' // 默认值
  }
  
  // 从后端转换为前端
  if (from === 'backend' && to === 'frontend') {
    if (gender === '公' || gender === '母') {
      return convertBackendToFrontendGender(gender as BackendGender)
    }
    return 'male' // 默认值
  }
  
  // 如果无法识别，根据目标类型返回默认值
  return to === 'backend' ? '公' : 'male'
}

/**
 * 将任意性别值转换为PetGender类型
 * @param gender 任意性别值
 * @returns PetGender类型的值
 */
export const convertToPetGender = (gender: string): 'male' | 'female' | 'unknown' => {
  // 处理前端格式
  if (gender === 'male' || gender === 'female') {
    return gender as 'male' | 'female'
  }
  
  // 处理后端格式
  if (gender === '公') {
    return 'male'
  }
  if (gender === '母') {
    return 'female'
  }
  
  // 无法识别的值
  return 'unknown'
}