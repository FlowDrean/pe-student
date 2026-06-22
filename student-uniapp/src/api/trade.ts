import http from '@/http'

export const tradeApi = {
  getList(params?: any) {
    return http.get('/trades', params)
  },
  getDetail(id: number) {
    return http.get(`/trades/${id}`)
  },
  create(data: any) {
    return http.post('/trades', data)
  },
  update(id: number, data: any) {
    return http.put(`/trades/${id}`, data)
  },
  delete(id: number) {
    return http.delete(`/trades/${id}`)
  },
  toggleFavorite(id: number) {
    return http.post(`/trades/${id}/favorite`)
  },
  getCategories() {
    return http.get('/trades/categories')
  },
  search(params?: any) {
    return http.get('/trades/search', params)
  },
}
