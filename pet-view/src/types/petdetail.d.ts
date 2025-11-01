
export type PetStatus = 'available' | 'adopted' | 'pending' | 'fostered' | 'quarantine'

export type PetGender = 'male' | 'female' | 'unknown'


export interface petDetailResponse {
  pid: number // 宠物ID
  name: string // 宠物名称
  breed: string // 宠物品种
  age: number // 宠物年龄
  species: string // 宠物物种
  status: PetStatus // 宠物状态
  gender: PetGender // 宠物性别（公/母）
  imgUrl: string | null // 宠物图片URL
  shelterName: string // 收容所名称
  shelterAddress: string // 收容所地址
}

