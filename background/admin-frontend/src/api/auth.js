import request from './index'

export const authApi = {
  login(username, password) {
    return request.post('/auth/login', { username, password })
  },
  me() {
    return request.get('/auth/me')
  },
}
