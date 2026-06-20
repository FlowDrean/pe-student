import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import { storage } from '@/utils/storage'

export const useAdminStore = defineStore('admin', () => {
  const token = ref(storage.get('admin_token') || '')
  const refreshToken = ref(storage.get('admin_refresh_token') || '')
  const userInfo = ref(null)

  const isLoggedIn = computed(() => !!token.value)

  async function login(username, password) {
    const res = await authApi.login(username, password)
    token.value = res.data.token
    refreshToken.value = res.data.refreshToken
    storage.set('admin_token', res.data.token)
    storage.set('admin_refresh_token', res.data.refreshToken)
    await fetchUserInfo()
    return res
  }

  async function fetchUserInfo() {
    const res = await authApi.me()
    userInfo.value = res.data
    return res
  }

  function logout() {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    storage.remove('admin_token')
    storage.remove('admin_refresh_token')
  }

  return {
    token,
    refreshToken,
    userInfo,
    isLoggedIn,
    login,
    fetchUserInfo,
    logout,
  }
})
