// 设置相关类型定义（仅类型，无逻辑）

import type { ApiResult } from './api'

// 登录历史项
export interface LoginHistoryItem {
  id: number
  userId: number
  loginTime: string
  ipAddress: string
  device: string
  location?: string
  status: string
}

// 登录历史结果
export type LoginHistoryResult = ApiResult<LoginHistoryItem[]>

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

// 偏好设置结果
export type PreferencesResult = ApiResult<Preferences>