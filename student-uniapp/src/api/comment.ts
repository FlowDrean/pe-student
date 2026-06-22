import http from '@/http'

export const commentApi = {
  getList(postId: number, params?: any) {
    return http.get(`/posts/${postId}/comments`, params)
  },
  create(postId: number, data: { content: string; parentId?: number }) {
    return http.post(`/posts/${postId}/comments`, data)
  },
  delete(id: number) {
    return http.delete(`/comments/${id}`)
  },
  toggleLike(id: number) {
    return http.post(`/comments/${id}/like`)
  },
}
