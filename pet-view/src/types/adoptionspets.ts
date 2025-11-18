// 宠物信息接口（领养宠物列表专用）
export interface Pet {
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