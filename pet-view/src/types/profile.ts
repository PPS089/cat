// 用户信息接口

import type { ApiResult } from './api'

export interface UserInfo {
  userName: string
  headPic: string
  userId: number
  email: string
  phone: string
  introduce: string
}

// 用户信息结果
export type UserInfoResult = ApiResult<UserInfo>