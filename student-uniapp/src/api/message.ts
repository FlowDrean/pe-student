import http from '@/http'

export const messageApi = {
  getConversations(params?: any) {
    return http.get('/messages/conversations', params)
  },
  getMessages(userId: number, params?: any) {
    return http.get(`/messages/${userId}`, params)
  },
  sendMessage(userId: number, data: { content: string; type: number; imageUrl?: string }) {
    return http.post(`/messages/${userId}`, data)
  },
  deleteConversation(userId: number) {
    return http.delete(`/messages/${userId}`)
  },
  getUnreadCount() {
    return http.get<number>('/messages/unread')
  },
  markRead(id: number) {
    return http.put(`/messages/${id}/read`)
  },
}
