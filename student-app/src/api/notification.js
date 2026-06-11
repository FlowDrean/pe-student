/* ============================================
   通知模块 API
   ============================================ */

import request from '@utils/request'

// 获取通知列表
export function getNotifications(params) {
  return request.get('/notifications', { params })
}

// 标记通知已读
export function markNotificationRead(id) {
  return request.put(`/notifications/${id}/read`)
}

// 全部标记已读
export function markAllNotificationsRead() {
  return request.put('/notifications/read-all')
}

// 删除通知
export function deleteNotification(id) {
  return request.delete(`/notifications/${id}`)
}

// 获取未读通知数
export function getNotificationUnreadCount() {
  return request.get('/notifications/unread-count')
}
