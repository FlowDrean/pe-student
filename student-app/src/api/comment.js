/* ============================================
   评论模块 API
   ============================================ */

import request from '@utils/request'

// 获取评论列表
export function getComments(postId, params) {
  return request.get(`/posts/${postId}/comments`, { params })
}

// 发表评论
export function createComment(postId, data) {
  return request.post(`/posts/${postId}/comments`, data)
}

// 删除评论
export function deleteComment(id) {
  return request.delete(`/comments/${id}`)
}

// 点赞评论
export function toggleCommentLike(id) {
  return request.post(`/comments/${id}/like`)
}
