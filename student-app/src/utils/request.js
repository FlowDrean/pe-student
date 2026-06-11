/* ============================================
   Axios 请求封装 — 含 Token 自动刷新
   ============================================ */

import axios from 'axios'
import { showToast } from 'vant'
import storage from './storage'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 15000,
})

// 防止并发刷新
let isRefreshing = false
let refreshQueue = []

function onRefreshed(newToken) {
  refreshQueue.forEach((cb) => cb(newToken))
  refreshQueue = []
}

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = storage.get('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const { data } = response
    if (data.code === 200) {
      return data.data
    }

    if (data.code === 401) {
      return handleAuthError()
    }

    showToast(data.message || '请求失败')
    return Promise.reject(new Error(data.message))
  },
  async (error) => {
    if (error.code === 'ECONNABORTED') {
      showToast('请求超时，请重试')
      return Promise.reject(error)
    }
    if (!error.response) {
      showToast('网络异常，请检查网络')
      return Promise.reject(error)
    }

    const status = error.response.status
    const serverMsg = error.response.data?.message

    // 401 → 尝试刷新 token
    if (status === 401) {
      return handleAuthError(error.config)
    }

    // 403 → 可能是 token 过期但被 Spring Security 拦截
    if (status === 403) {
      const token = storage.get('token')
      if (token) {
        // 有 token 但 403，尝试刷新
        return handleAuthError(error.config)
      }
    }

    const messages = { 401: '登录已过期', 403: '没有权限', 404: '请求的资源不存在', 500: '服务器错误' }
    // 有服务端错误消息时优先显示，否则使用默认消息
    showToast(serverMsg || messages[status] || '请求失败')
    return Promise.reject(error)
  }
)

// Token 刷新逻辑
async function handleAuthError(retryConfig) {
  const refreshToken = storage.get('refreshToken')
  if (!refreshToken) {
    clearAndRedirect()
    return Promise.reject(new Error('登录已过期'))
  }

  if (isRefreshing) {
    // 已有刷新在进行中，排队等待
    return new Promise((resolve) => {
      refreshQueue.push((newToken) => {
        if (retryConfig) {
          retryConfig.headers.Authorization = `Bearer ${newToken}`
          resolve(request(retryConfig))
        } else {
          resolve(Promise.reject(new Error('登录已过期')))
        }
      })
    })
  }

  isRefreshing = true
  try {
    const data = await axios.post('/api/v1/auth/refresh', { refreshToken })
    if (data.data?.data?.token) {
      const newToken = data.data.data.token
      storage.set('token', newToken)
      onRefreshed(newToken)

      if (retryConfig) {
        retryConfig.headers.Authorization = `Bearer ${newToken}`
        return request(retryConfig)
      }
      return Promise.resolve()
    }
    clearAndRedirect()
    return Promise.reject(new Error('登录已过期'))
  } catch {
    clearAndRedirect()
    return Promise.reject(new Error('登录已过期'))
  } finally {
    isRefreshing = false
  }
}

function clearAndRedirect() {
  storage.remove('token')
  storage.remove('refreshToken')
  storage.remove('userInfo')
  window.location.hash = '#/auth/login'
}

export default request
