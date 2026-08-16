import { useUserStore } from '@stores/user'
import { API_BASE_URL } from '@/config'

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
      url: `${API_BASE_URL}/upload/image`,
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
 * 批量上传图片 — 后端仅提供单张接口，这里循环逐张上传
 */
export async function uploadImages(filePaths: string[]): Promise<string[]> {
  const urls: string[] = []
  for (const filePath of filePaths) {
    const result = await uploadImage(filePath)
    urls.push(result.url)
  }
  return urls
}
