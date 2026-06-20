import request from './index'

export const userApi = {
  getList(params) {
    return request.get('/users', { params })
  },
  getDetail(id) {
    return request.get(`/users/${id}`)
  },
  updateStatus(id, status) {
    return request.put(`/users/${id}/status`, { status })
  },
  updateRole(id, role) {
    return request.put(`/users/${id}/role`, { role })
  },
  update(id, data) {
    return request.put(`/users/${id}`, data)
  },
  resetPassword(id, password) {
    return request.put(`/users/${id}/password`, { password })
  },
}
