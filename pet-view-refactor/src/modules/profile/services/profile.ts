import request from '@/shared/utils/request'
import type { UserInfo } from '@/modules/profile/types'

interface UpdateProfileResponse {
  code: number
  data?: UserInfo
  msg?: string
}

export async function updateProfile(formData: FormData): Promise<UserInfo> {
  const response = (await request.put('/user/profile', formData)) as UpdateProfileResponse
  if (!response.data) {
    throw new Error(response.msg || '更新个人信息失败')
  }
  return response.data
}

export async function uploadAvatar(file: File): Promise<UserInfo> {
  const formData = new FormData()
  formData.append('avatar', file)
  return updateProfile(formData)
}
