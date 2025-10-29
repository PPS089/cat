// 设置相关类型定义（仅类型，无逻辑）
export interface LoginHistoryItem {
  id: number
  userId: number
  loginTime: string
  ipAddress: string
  device: string
  location?: string
  status: string
}

// 密码表单接口
export interface PasswordForm {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}

// 个人偏好设置接口
export interface Preferences {
  language: string
  theme: 'light' | 'dark' | 'auto'
  primaryColor: string
  fontSize: number
  animations: boolean
  compactMode: boolean
}