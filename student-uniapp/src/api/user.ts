import http from '@/http'
import { useUserStore } from '@stores/user'
import { API_BASE_URL } from '@/config'

export const userApi = {
  getProfile() {
    return http.get('/user/profile')
  },
  updateProfile(data: any) {
    return http.put('/user/profile', data)
  },
  uploadAvatar(filePath: string) {
    return new Promise((resolve, reject) => {
      const userStore = useUserStore()
      const header: Record<string, string> = {}
      if (userStore.token) {
        header['Authorization'] = `Bearer ${userStore.token}`
      }
      uni.uploadFile({
        url: `${API_BASE_URL}/user/avatar`,
        filePath,
        name: 'file',
        header,
        success: (res) => {
          try {
            const data = JSON.parse(res.data)
            if (data.code === 200) resolve(data.data)
            else reject(new Error(data.message))
          } catch { reject(new Error('上传失败')) }
        },
        fail: reject,
      })
    })
  },
  getUserById(id: number) {
    return http.get(`/user/${id}`)
  },
  getUserPosts(id: number, params?: any) {
    return http.get(`/user/${id}/posts`, params)
  },
  getUserTrades(id: number, params?: any) {
    return http.get(`/user/${id}/trades`, params)
  },
  followUser(id: number) {
    return http.post(`/user/follow/${id}`)
  },
  unfollowUser(id: number) {
    return http.delete(`/user/follow/${id}`)
  },
  getFollowingList(id: number, params?: any) {
    return http.get(`/user/${id}/follows`, params)
  },
  getFansList(id: number, params?: any) {
    return http.get(`/user/${id}/fans`, params)
  },
  getFavorites(params?: any) {
    return http.get('/user/favorites', params)
  },
}
