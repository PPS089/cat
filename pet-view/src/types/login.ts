// 登录相关类型定义

export interface LoginResponse {
  ok: boolean;
  message: string;
  token: string;
  userId: number;
  username: string;
  expireTime: number;
}

export interface LoginForm {
  username: string;
  password: string;
  rememberMe: boolean;
}
