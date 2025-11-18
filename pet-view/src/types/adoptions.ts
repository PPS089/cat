// 领养相关类型定义

import type { PageResult } from './api'

// 领养记录时间线项
export interface AdoptionTimelineItem {
  id: number
  petId: number
  petName: string
  petBreed: string
  action: 'adopted' | 'fostered' | 'foster_ended' | 'returned'
  actionDate: string
  description: string
  shelterName?: string
  status: string
}

// 领养记录时间线响应 - 匹配后端Result格式
export interface AdoptionTimelineResponse {
  timeline: AdoptionTimelineItem[]
  total: number
  petName?: string
  petBreed?: string
}

// 宠物信息接口（领养专用）
export interface AdoptionPetInfo {
  pid: number
  name: string
  species: string
  breed: string
  age: number
  gender: string
  image: string
  petStatus: string
}

// 收容所信息接口（领养专用）
export interface AdoptionShelterInfo {
  sid: number
  sname: string
  location: string
}

// 领养记录接口
export interface Adoption {
  id: number
  adoptDate: string
  foster_status: string
  pet: AdoptionPetInfo
  shelter: AdoptionShelterInfo
}

// 领养记录分页响应
export type AdoptionPageResult = PageResult<Adoption>

// 领养宠物信息
export interface AdoptionPet {
  pid: number
  name: string
  species: string
  breed: string
  age: number
  gender: string
  status: string
  shelterName: string
  shelterAddress: string
}