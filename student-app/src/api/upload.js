/* ============================================
   文件上传模块 API
   ============================================ */

import request from '@utils/request'

// 上传单张图片
export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload/image', formData)
}

// 批量上传图片（最多9张）
export function uploadImages(files) {
  const formData = new FormData()
  files.forEach((file) => formData.append('files', file))
  return request.post('/upload/images', formData)
}
