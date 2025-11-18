// 我的宠物相关类型定义

import type { Pet as CommonPet, Shelter as CommonShelter } from './common'
import type { PageResult } from './api'

// 导出重新定义的类型，保持向后兼容
export type Pet = CommonPet
export type Shelter = CommonShelter

// 结束寄养响应
export interface EndFosterResponse {
  code: number
  data: any[]
  message: string
}

// 领养响应
export type AdoptionResponse = PageResult<Pet>