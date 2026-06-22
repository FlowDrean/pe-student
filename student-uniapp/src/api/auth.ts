import http from '@/http'

export const authApi = {
  login(data: { username: string; password: string }) {
    return http.post<{ token: string; refreshToken: string; userId: string }>('/auth/login', data)
  },
  register(data: { username: string; password: string; phone: string; school?: string }) {
    return http.post('/auth/register', data)
  },
  refreshToken(refreshToken: string) {
    return http.post<{ token: string; refreshToken: string }>('/auth/refresh', { refreshToken })
  },
  logout() {
    return http.post('/auth/logout')
  },
}
