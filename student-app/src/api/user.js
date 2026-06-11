/* ============================================
   用户模块 API
   ============================================ */

import request from '@utils/request'

// 注册
export function register(data) {
  return request.post('/auth/register', data)
}

// 登录
export function login(data) {
  return request.post('/auth/login', data)
}

// 登出
export function logout() {
  return request.post('/auth/logout')
}

// 刷新 Token
export function refreshToken(refreshToken) {
  return request.post('/auth/refresh', { refreshToken })
}

// 获取当前用户信息
export function getUserProfile() {
  return request.get('/user/profile')
}

// 更新用户信息
export function updateUserProfile(data) {
  return request.put('/user/profile', data)
}

// 上传头像
export function uploadAvatar(formData) {
  return request.post('/user/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// 获取用户主页
export function getUserById(id) {
  return request.get(`/user/${id}`)
}

// 获取用户帖子列表
export function getUserPosts(id, params) {
  return request.get(`/user/${id}/posts`, { params })
}

// 获取用户商品列表
export function getUserTrades(id, params) {
  return request.get(`/user/${id}/trades`, { params })
}

// 关注用户
export function followUser(id) {
  return request.post(`/user/follow/${id}`)
}

// 取消关注
export function unfollowUser(id) {
  return request.delete(`/user/follow/${id}`)
}

// 获取用户收藏
export function getUserFavorites(params) {
  return request.get('/user/favorites', { params })
}
