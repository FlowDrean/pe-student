import http from '@/http'

export const notificationApi = {
  getList(params?: any) {
    return http.get('/notifications', params)
  },
  markRead(id: number) {
    return http.put(`/notifications/${id}/read`)
  },
  markAllRead() {
    return http.put('/notifications/read-all')
  },
  delete(id: number) {
    return http.delete(`/notifications/${id}`)
  },
  getUnreadCount() {
    return http.get<number>('/notifications/unread-count')
  },
}
