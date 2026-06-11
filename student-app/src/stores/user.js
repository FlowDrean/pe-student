/* ============================================
   用户状态管理
   ============================================ */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import storage from '@utils/storage'

export const useUserStore = defineStore('user', () => {
  // ── 状态 ──
  const token = ref(storage.get('token') || '')
  const refreshToken = ref(storage.get('refreshToken') || '')
  const userInfo = ref(storage.get('userInfo') || null)

  // ── 计算属性 ──
  const isLogin = computed(() => !!token.value)
  const userId = computed(() => userInfo.value?.id)
  const nickname = computed(() => userInfo.value?.nickname || userInfo.value?.username || '未登录')
  const avatar = computed(() => userInfo.value?.avatar || '')

  // ── 方法 ──
  function setToken(accessToken, refresh) {
    token.value = accessToken
    storage.set('token', accessToken)
    if (refresh) {
      refreshToken.value = refresh
      storage.set('refreshToken', refresh)
    }
  }

  function setUserInfo(info) {
    userInfo.value = { ...userInfo.value, ...info }
    storage.set('userInfo', userInfo.value)
  }

  function logout() {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    storage.remove('token')
    storage.remove('refreshToken')
    storage.remove('userInfo')
  }

  function updateField(key, value) {
    if (userInfo.value) {
      userInfo.value[key] = value
      storage.set('userInfo', userInfo.value)
    }
  }

  return {
    token,
    refreshToken,
    userInfo,
    isLogin,
    userId,
    nickname,
    avatar,
    setToken,
    setUserInfo,
    logout,
    updateField,
  }
})
