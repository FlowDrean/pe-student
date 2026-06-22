import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAppStore = defineStore('app', () => {
  const loading = ref(false)
  const networkStatus = ref<'online' | 'offline' | 'slow'>('online')
  const showTabBar = ref(true)
  const unreadCount = ref(0)

  const isOnline = computed(() => networkStatus.value !== 'offline')

  function init() {
    uni.onNetworkStatusChange((res) => {
      networkStatus.value = res.isConnected ? 'online' : 'offline'
    })
    uni.getNetworkType({
      success: (res) => {
        networkStatus.value = res.networkType === 'none' ? 'offline' : 'online'
      },
    })
  }

  function onShow() { showTabBar.value = true }
  function onHide() { showTabBar.value = false }
  function setLoading(val: boolean) { loading.value = val }
  function setShowTabBar(val: boolean) { showTabBar.value = val }
  function setUnreadCount(count: number) { unreadCount.value = count }

  return { loading, networkStatus, showTabBar, unreadCount, isOnline, init, onShow, onHide, setLoading, setShowTabBar, setUnreadCount }
}, { persist: true })
