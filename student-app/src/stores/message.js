/* ============================================
   消息状态管理
   ============================================ */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useMessageStore = defineStore('message', () => {
  // ── 状态 ──
  const conversations = ref([])
  const currentChat = ref(null)
  const messages = ref([])
  const unreadCount = ref(0)
  const notifications = ref([])
  const notiUnreadCount = ref(0)

  // ── 计算属性 ──
  const totalUnread = computed(() => unreadCount.value + notiUnreadCount.value)

  // ── 方法 ──
  function setConversations(list) {
    conversations.value = list
    unreadCount.value = list.reduce((sum, c) => sum + (c.unread || 0), 0)
  }

  function setMessages(list) {
    messages.value = list
  }

  function addMessage(msg) {
    messages.value.push(msg)
    // 更新会话列表中的最后一条消息
    const conv = conversations.value.find(
      (c) => c.userId === msg.senderId || c.userId === msg.receiverId
    )
    if (conv) {
      // 图片消息在会话列表中显示 [图片]
      conv.lastMessage = msg.type === 2 ? '[图片]' : msg.content
      conv.lastTime = msg.createdAt
    }
  }

  function setNotifications(list) {
    notifications.value = list
    notiUnreadCount.value = list.filter((n) => !n.isRead).length
  }

  function markRead(notiId) {
    const noti = notifications.value.find((n) => n.id === notiId)
    if (noti && !noti.isRead) {
      noti.isRead = true
      notiUnreadCount.value = Math.max(0, notiUnreadCount.value - 1)
    }
  }

  function markAllRead() {
    notifications.value.forEach((n) => {
      n.isRead = true
    })
    notiUnreadCount.value = 0
  }

  function setUnreadCount(count) {
    unreadCount.value = count
  }

  return {
    conversations,
    currentChat,
    messages,
    unreadCount,
    notifications,
    notiUnreadCount,
    totalUnread,
    setConversations,
    setMessages,
    addMessage,
    setNotifications,
    markRead,
    markAllRead,
    setUnreadCount,
  }
})
