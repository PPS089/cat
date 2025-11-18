// 寄养相关类型定义

import type { PageResult } from './api'

// 寄养记录接口
export interface Foster {
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

// 寄养数据接口
export interface FosterData {
  records: Array<{
    fid: number
    startDate: string
    endDate: string | null
    pid: number
    name: string
    breed: string
    age: number
    gender: string
    image: string | null
    sid: number
    sname: string
    location: string
  }>
  total: number
  size: number
  current: number
  pages: number
}

// 寄养记录分页结果
export type FosterPageResult = PageResult<Foster>
