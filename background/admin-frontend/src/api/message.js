import request from './index'

export const messageApi = {
  getList(params) {
    return request.get('/messages', { params })
  },
  getDetail(id) {
    return request.get(`/messages/${id}`)
  },
  delete(id) {
    return request.delete(`/messages/${id}`)
  },
  deleteConversation(userId, peerId) {
    return request.delete('/messages/conversation', { params: { userId, peerId } })
  },
}
