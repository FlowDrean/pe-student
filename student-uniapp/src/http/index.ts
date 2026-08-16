/**
 * HTTP 请求封装 — 基于 uni.request
 * 自动注入 JWT Token，统一错误处理
 */
import { useUserStore } from '@stores/user'
import { API_BASE_URL, SERVER_BASE_URL } from '@/config'

const BASE_URL = API_BASE_URL

/** 静态资源基础地址（图片等） */
const ASSET_BASE = SERVER_BASE_URL

/**
 * 解析静态资源完整 URL
 * 后端返回的相对路径（如 /uploads/xxx）需要拼接服务器地址
 */
export function resolveAssetUrl(url: string): string {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  return ASSET_BASE + url
}

interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: any
  params?: Record<string, any>
  header?: Record<string, string>
}

function request<T = any>(options: RequestOptions): Promise<T> {
  return new Promise((resolve, reject) => {
    const userStore = useUserStore()
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...options.header,
    }
    if (userStore.token) {
      headers['Authorization'] = `Bearer ${userStore.token}`
    }

    // Build URL with query params
    let url = BASE_URL + options.url
    if (options.params) {
      const qs = Object.entries(options.params)
        .filter(([_, v]) => v !== undefined && v !== null && v !== '')
        .map(([k, v]) => `${k}=${encodeURIComponent(String(v))}`)
        .join('&')
      if (qs) url += '?' + qs
    }

    uni.request({
      url,
      method: options.method || 'GET',
      data: options.data,
      header: headers,
      success: (res: any) => {
        if (res.statusCode === 401) {
          userStore.logout()
          uni.reLaunch({ url: '/pages/auth/login' })
          return reject(new Error('未授权'))
        }
        if (res.statusCode === 403) {
          uni.showToast({ title: '没有权限', icon: 'none' })
          return reject(new Error('没有权限'))
        }
        const data = res.data
        if (data && data.code === 200) {
          resolve(data.data)
        } else {
          uni.showToast({ title: data?.message || '请求失败', icon: 'none' })
          reject(new Error(data?.message || '请求失败'))
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络异常', icon: 'none' })
        reject(err)
      },
    })
  })
}

export const http = {
  get<T = any>(url: string, params?: any): Promise<T> {
    return request<T>({ url, method: 'GET', params })
  },
  post<T = any>(url: string, data?: any): Promise<T> {
    return request<T>({ url, method: 'POST', data })
  },
  put<T = any>(url: string, data?: any): Promise<T> {
    return request<T>({ url, method: 'PUT', data })
  },
  delete<T = any>(url: string, params?: any): Promise<T> {
    return request<T>({ url, method: 'DELETE', params })
  },
}

export default http
