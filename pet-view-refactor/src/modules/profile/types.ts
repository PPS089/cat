export interface UserInfo {
  userId: number
  userName: string
  headPic: string
  email: string
  phone: string
  introduce: string
  role?: string
  adminShelterId?: number | null // 管理员绑定的收容所ID，null表示平台管理员
}
