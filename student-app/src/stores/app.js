/* ============================================
   全局状态管理
   ============================================ */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAppStore = defineStore('app', () => {
  // ── 状态 ──
  const loading = ref(false)
  const networkStatus = ref('online') // online | offline | slow
  const theme = ref('light')
  const showTabBar = ref(true)
  const unreadCount = ref(0)

  // ── 计算属性 ──
  const isOnline = computed(() => networkStatus.value === 'online')
  const safeTop = computed(() => 'env(safe-area-inset-top, 0px)')
  const safeBottom = computed(() => 'env(safe-area-inset-bottom, 0px)')

  // ── 方法 ──
  function init() {
    // 监听网络状态
    window.addEventListener('online', () => {
      networkStatus.value = 'online'
    })
    window.addEventListener('offline', () => {
      networkStatus.value = 'offline'
    })
  }

  function setLoading(val) {
    loading.value = val
  }

  function setShowTabBar(val) {
    showTabBar.value = val
  }

  function setUnreadCount(count) {
    unreadCount.value = count
  }

  return {
    loading,
    networkStatus,
    theme,
    showTabBar,
    unreadCount,
    isOnline,
    safeTop,
    safeBottom,
    init,
    setLoading,
    setShowTabBar,
    setUnreadCount,
  }
})
