import type { PetGender } from './common'
import type { ApiResult } from './api'

/**
 * 宠物信息接口
 * 用于定义宠物对象的数据结构
 */
export interface Pet {
  /** 宠物唯一标识ID */
  pid: number
  /** 宠物名称 */
  name: string
  /** 宠物种类 */
  species: string
  /** 宠物品种 */
  breed: string
  /** 宠物年龄 */
  age: number
  /** 宠物性别 */
  gender: PetGender
}

/**
 * 宠物表单数据接口
 * 用于编辑宠物页面的表单数据结构
 */
export interface PetFormData {
  /** 宠物名称 */
  name: string
  /** 宠物种类 */
  species: string
  /** 宠物品种 */
  breed: string
  /** 宠物年龄 */
  age: number
  /** 宠物性别 - 前端使用 male/female 格式 */
  gender: PetGender
}

/**
 * 更新宠物响应接口
 * 用于定义更新宠物API的响应数据结构
 */
export interface UpdatePetResponse {
  /** 响应状态码 */
  code: number
  /** 响应消息 */
  message: string
  /** 响应数据 */
  data: Pet | null
}

// 宠物表单数据结果
export type PetFormResult = ApiResult<Pet>