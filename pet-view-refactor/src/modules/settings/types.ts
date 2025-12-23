export interface LoginHistoryItem {
  id: number
  userId: number
  loginTime: string
  ipAddress: string
  device: string
  location?: string
  status: 'SUCCESS' | 'FAILED' | string
}

export interface ChangePasswordPayload {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}
