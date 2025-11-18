// 通用 API 响应与分页类型
import type { Pet, MediaFile } from './common'

export interface ApiResult<T = any> {
  code: number
  data: T
  message?: string
}

export interface PageResult<T = any> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export type Id = string | number

// 领养记录分页结果
export type AdoptionPageResult = PageResult<Pet>

// 寄养记录分页结果
export type FosterPageResult = PageResult<FosterRecord>

// 寄养记录
export interface FosterRecord {
  id: number
  pet: {
    pid: number
    name: string
    breed: string
    age: number
    gender: string
    image: string | '/dog.jpg'
  }
  shelter: {
    sid: number
    name: string
    location: string
  }
  startDate: string
  endDate: string | null
  status: 'ongoing' | 'ended'
}

// 事件记录
export interface EventRecord {
  record_id: number
  pid: number
  event_type: string
  description: string
  record_time: string
  mood?: string
  location?: string
  pet_name?: string
  created_at: string
  updated_at?: string
  media_list?: MediaFile[]
}

// 事件记录分页结果
export type EventPageResult = PageResult<EventRecord>