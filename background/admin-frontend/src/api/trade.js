import request from './index'

export const tradeApi = {
  getList(params) {
    return request.get('/trades', { params })
  },
  getDetail(id) {
    return request.get(`/trades/${id}`)
  },
  updateStatus(id, status) {
    return request.put(`/trades/${id}/status`, { status })
  },
  delete(id) {
    return request.delete(`/trades/${id}`)
  },
  update(id, data) {
    return request.put(`/trades/${id}`, data)
  },
}
