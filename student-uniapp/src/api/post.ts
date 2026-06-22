import http from '@/http'

export const postApi = {
  getList(params?: any) {
    return http.get('/posts', params)
  },
  getDetail(id: number) {
    return http.get(`/posts/${id}`)
  },
  create(data: any) {
    return http.post('/posts', data)
  },
  update(id: number, data: any) {
    return http.put(`/posts/${id}`, data)
  },
  delete(id: number) {
    return http.delete(`/posts/${id}`)
  },
  toggleLike(id: number) {
    return http.post(`/posts/${id}/like`)
  },
  toggleFavorite(id: number) {
    return http.post(`/posts/${id}/favorite`)
  },
  getCategories() {
    return http.get('/posts/categories')
  },
  getHotPosts(params?: any) {
    return http.get('/posts/hot', params)
  },
  search(params?: any) {
    return http.get('/posts/search', params)
  },
}
