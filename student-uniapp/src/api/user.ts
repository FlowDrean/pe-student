import http from '@/http'

export const userApi = {
  getProfile() {
    return http.get('/user/profile')
  },
  updateProfile(data: any) {
    return http.put('/user/profile', data)
  },
  uploadAvatar(filePath: string) {
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: 'http://localhost:8080/api/v1/user/avatar',
        filePath,
        name: 'file',
        header: {
          Authorization: `Bearer ${uni.getStorageSync('student_app_token') || ''}`,
        },
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
  getFavorites(params?: any) {
    return http.get('/user/favorites', params)
  },
}
