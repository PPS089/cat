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
  
  return genderMap[frontendGender] || '公' // 默认返回'公'
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
  
  return genderMap[backendGender] || 'male' // 默认返回'male'
}

/**
 * 验证性别格式是否有效
 * @param gender 性别值
 * @param type 验证类型 ('frontend' | 'backend')
 * @returns 是否有效
 */
export const isValidGender = (gender: string, type: 'frontend' | 'backend'): boolean => {
  if (type === 'frontend') {
    return gender === 'male' || gender === 'female'
  } else {
    return gender === '公' || gender === '母'
  }
}

/**
 * 安全的性别转换（带错误处理）
 * @param gender 性别值
 * @param fromType 源格式 ('frontend' | 'backend')
 * @param toType 目标格式 ('frontend' | 'backend')
 * @returns 转换后的性别值，如果转换失败则返回默认值
 */
export const safeConvertGender = (
  gender: string, 
  fromType: 'frontend' | 'backend', 
  toType: 'frontend' | 'backend'
): string => {
  try {
    if (fromType === toType) {
      return gender
    }
    
    if (fromType === 'frontend' && toType === 'backend') {
      return convertFrontendToBackendGender(gender as FrontendGender)
    }
    
    if (fromType === 'backend' && toType === 'frontend') {
      return convertBackendToFrontendGender(gender as BackendGender)
    }
    
    return gender
  } catch (error) {
    console.error('性别转换失败:', error)
    // 返回默认值
    return toType === 'frontend' ? 'male' : '公'
  }
}