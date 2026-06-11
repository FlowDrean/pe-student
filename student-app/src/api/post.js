/* ============================================
   帖子模块 API
   ============================================ */

import request from '@utils/request'

// 获取帖子列表
export function getPosts(params) {
  return request.get('/posts', { params })
}

// 获取帖子详情
export function getPostDetail(id) {
  return request.get(`/posts/${id}`)
}

// 发布帖子
export function createPost(data) {
  return request.post('/posts', data)
}

// 编辑帖子
export function updatePost(id, data) {
  return request.put(`/posts/${id}`, data)
}

// 删除帖子
export function deletePost(id) {
  return request.delete(`/posts/${id}`)
}

// 点赞/取消点赞
export function togglePostLike(id) {
  return request.post(`/posts/${id}/like`)
}

// 收藏/取消收藏
export function togglePostFavorite(id) {
  return request.post(`/posts/${id}/favorite`)
}

// 获取分类列表
export function getPostCategories() {
  return request.get('/posts/categories')
}

// 获取热门帖子
export function getHotPosts(params) {
  return request.get('/posts/hot', { params })
}

// 搜索帖子
export function searchPosts(params) {
  return request.get('/posts/search', { params })
}
