import request from '@/shared/utils/request'
import type { ChangePasswordPayload, LoginHistoryItem } from '@/modules/settings/types'

interface ApiResponse<T> {
  code: number
  data?: T
  message?: string
}

export async function changePassword(payload: ChangePasswordPayload) {
  const body = {
    currentPassword: payload.currentPassword,
    newPassword: payload.newPassword,
  }
  return request.put('/user/update-password', body)
}

export async function fetchLoginHistory(): Promise<LoginHistoryItem[]> {
  const response = (await request.get('/login-history')) as ApiResponse<LoginHistoryItem[]>
  return response.data ?? []
}

export async function clearLoginHistory() {
  await request.delete('/login-history/clear')
}
