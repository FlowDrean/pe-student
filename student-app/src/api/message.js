/* ============================================
   消息模块 API
   ============================================ */

import request from '@utils/request'

// 获取会话列表
export function getConversations(params) {
  return request.get('/messages/conversations', { params })
}

// 获取与某用户的聊天记录
export function getMessages(userId, params) {
  return request.get(`/messages/${userId}`, { params })
}

// 发送消息
export function sendMessage(userId, data) {
  return request.post(`/messages/${userId}`, data)
}

// 删除会话
export function deleteConversation(userId) {
  return request.delete(`/messages/${userId}`)
}

// 获取未读消息数
export function getUnreadCount() {
  return request.get('/messages/unread')
}

// 标记消息已读
export function markMessageRead(id) {
  return request.put(`/messages/${id}/read`)
}
