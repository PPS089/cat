import request from '@/shared/utils/request'

export const authService = {
  login(payload: { username: string; password: string }) {
    return request.post('/user/login', {
      username: payload.username,
      password: payload.password,
    })
  },
  register(payload: { username: string; phone: string; email: string; password: string }) {
    return request.post('/user/register', {
      username: payload.username,
      phone: payload.phone,
      email: payload.email,
      password: payload.password,
    })
  },
  sendResetCode(email: string) {
    return request.post('/user/send-reset-code', { email })
  },
  resetPassword(payload: { email: string; code: string; newPassword: string }) {
    return request.post('/user/forgot-password', payload)
  },
}
