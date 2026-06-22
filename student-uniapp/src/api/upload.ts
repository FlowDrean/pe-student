import { useUserStore } from '@stores/user'

/**
 * 图片上传 — 使用 uni.uploadFile
 * 后端接受 POST /api/v1/upload/image (单张)
 */
export function uploadImage(filePath: string): Promise<{ url: string }> {
  return new Promise((resolve, reject) => {
    const userStore = useUserStore()
    const header: Record<string, string> = {}
    if (userStore.token) {
      header['Authorization'] = `Bearer ${userStore.token}`
    }

    uni.uploadFile({
      url: 'http://localhost:8080/api/v1/upload/image',
      filePath,
      name: 'file',
      header,
      success: (res) => {
        try {
          const data = JSON.parse(res.data)
          if (data.code === 200) resolve(data.data)
          else reject(new Error(data.message || '上传失败'))
        } catch {
          reject(new Error('上传失败'))
        }
      },
      fail: (err) => {
        uni.showToast({ title: '图片上传失败', icon: 'none' })
        reject(err)
      },
    })
  })
}

/**
 * 批量上传图片 — POST /api/v1/upload/images (最多9张)
 */
export function uploadImages(filePaths: string[]): Promise<string[]> {
  return new Promise((resolve, reject) => {
    const userStore = useUserStore()
    const header: Record<string, string> = {}
    if (userStore.token) {
      header['Authorization'] = `Bearer ${userStore.token}`
    }

    uni.uploadFile({
      url: 'http://localhost:8080/api/v1/upload/images',
      filePath: filePaths[0],
      name: 'files',
      header,
      success: (res) => {
        try {
          const data = JSON.parse(res.data)
          if (data.code === 200) resolve(data.data)
          else reject(new Error(data.message || '上传失败'))
        } catch {
          reject(new Error('上传失败'))
        }
      },
      fail: reject,
    })
  })
}
