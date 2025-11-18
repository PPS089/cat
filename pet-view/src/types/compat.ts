// 兼容性类型定义 - 用于处理API响应与前端组件之间的数据映射

// API响应中的宠物信息（可能字段名不同）
export interface ApiPet {
  pid?: number
  id?: number
  name: string
  species?: string
  breed: string
  age: number
  gender: string
  status?: string
  petStatus?: string
  image?: string
  imgUrl?: string
  shelterName?: string
  sname?: string
  shelterAddress?: string
  location?: string
}

// API响应中的收容所信息（可能字段名不同）
export interface ApiShelter {
  sid?: number
  id?: number
  shelterName?: string
  name?: string
  shelterAddress?: string
  location?: string
}

// API响应中的领养记录
export interface ApiAdoption {
  id?: number
  aid?: number
  adoptDate?: string
  adoptionDate?: string
  foster_status?: string
  pet: ApiPet
  shelter: ApiShelter
}

// API响应中的寄养记录
export interface ApiFoster {
  id?: number
  fid?: number
  startDate: string
  endDate: string | null
  pet: ApiPet
  shelter: ApiShelter
}