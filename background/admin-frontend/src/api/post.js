import request from './index'

export const postApi = {
  getList(params) {
    return request.get('/posts', { params })
  },
  getDetail(id) {
    return request.get(`/posts/${id}`)
  },
  updateStatus(id, status) {
    return request.put(`/posts/${id}/status`, { status })
  },
  delete(id) {
    return request.delete(`/posts/${id}`)
  },
  update(id, data) {
    return request.put(`/posts/${id}`, data)
  },
}
