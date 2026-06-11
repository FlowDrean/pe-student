/* ============================================
   二手交易模块 API
   ============================================ */

import request from '@utils/request'

// 获取商品列表
export function getTrades(params) {
  return request.get('/trades', { params })
}

// 获取商品详情
export function getTradeDetail(id) {
  return request.get(`/trades/${id}`)
}

// 发布商品
export function createTrade(data) {
  return request.post('/trades', data)
}

// 编辑商品
export function updateTrade(id, data) {
  return request.put(`/trades/${id}`, data)
}

// 删除商品
export function deleteTrade(id) {
  return request.delete(`/trades/${id}`)
}

// 收藏商品
export function toggleTradeFavorite(id) {
  return request.post(`/trades/${id}/favorite`)
}

// 获取商品分类
export function getTradeCategories() {
  return request.get('/trades/categories')
}

// 搜索商品
export function searchTrades(params) {
  return request.get('/trades/search', { params })
}
