import request from './index'

export const commentApi = {
  getList(params) {
    return request.get('/comments', { params })
  },
  delete(id) {
    return request.delete(`/comments/${id}`)
  },
  update(id, data) {
    return request.put(`/comments/${id}`, data)
  },
}
