// Pet interface for type checking and validation
export interface Pet {
  pid: number
  name: string
  species: string
  breed: string
  age: number
  gender: string
  status: string
  petStatus?: string  // 后端返回的寄养状态字段
  isFostering?: boolean  // 后端返回的是否寄养中字段
  shelterName: string
  shelterAddress: string
  adopt_date?: string
  adoptionDate?: string  // 后端返回的领养日期字段
  shelter?: {
    name: string
    location: string
  }
  sname?: string  // 收容所名称（简化字段）
  location?: string  // 收容所地址（简化字段）
  image: string
  type?: 'adoption' | 'foster'
  sid?: number  // 收容所ID
}




export interface Shelter {
  sid: number
  shelterName: string
  shelterAddress: string
}


export interface EndFosterResponse {
  code: number
  data: any[]
  message: string
}


import type { PageResult } from './api'
export type AdoptionResponse = PageResult<Pet>
