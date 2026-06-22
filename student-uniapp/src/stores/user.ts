import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface UserInfo {
  id: number
  username: string
  nickname?: string
  avatar?: string
  phone?: string
  email?: string
  studentId?: string
  gender?: number
  bio?: string
  [key: string]: any
}

export const useUserStore = defineStore('user', () => {
  // ── 状态 ──
  const token = ref('')
  const refreshToken = ref('')
  const userInfo = ref<UserInfo | null>(null)

  // ── 计算属性 ──
  const isLogin = computed(() => !!token.value)
  const userId = computed(() => userInfo.value?.id)
  const nickname = computed(() => userInfo.value?.nickname || userInfo.value?.username || '未登录')
  const avatar = computed(() => userInfo.value?.avatar || '')

  // ── 方法 ──
  function setToken(accessToken: string, refresh?: string) {
    token.value = accessToken
    if (refresh) {
      refreshToken.value = refresh
    }
  }

  function setUserInfo(info: Partial<UserInfo>) {
    userInfo.value = { ...userInfo.value, ...info } as UserInfo
  }

  function logout() {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
  }

  function updateField(key: string, value: any) {
    if (userInfo.value) {
      ;(userInfo.value as Record<string, any>)[key] = value
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
}, { persist: true })
