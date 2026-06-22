import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface Conversation {
  id: number
  userId: number
  userName?: string
  userAvatar?: string
  lastMessage?: string
  lastTime?: string
  unread: number
  [key: string]: any
}

export interface Message {
  id: number
  senderId: number
  receiverId: number
  type: number // 1=文本, 2=图片, 3=系统
  content: string
  imageUrl?: string
  createdAt: string
  isRead: boolean
  [key: string]: any
}

export interface Notification {
  id: number
  type: number
  title: string
  content: string
  isRead: boolean
  targetId?: number
  createdAt: string
  [key: string]: any
}

export const useMessageStore = defineStore('message', () => {
  // ── 状态 ──
  const conversations = ref<Conversation[]>([])
  const currentChat = ref<Conversation | null>(null)
  const messages = ref<Message[]>([])
  const unreadCount = ref(0)
  const notifications = ref<Notification[]>([])
  const notiUnreadCount = ref(0)

  // ── 计算属性 ──
  const totalUnread = computed(() => unreadCount.value + notiUnreadCount.value)

  // ── 方法 ──
  function setConversations(list: any) {
    const arr = Array.isArray(list) ? list : (list?.list || list?.records || [])
    conversations.value = arr
    unreadCount.value = arr.reduce((sum, c) => sum + (c.unread || 0), 0)
  }

  function setMessages(list: any) {
    if (Array.isArray(list)) {
      messages.value = list
    } else if (list?.list) {
      messages.value = list.list
    } else if (list?.records) {
      messages.value = list.records
    } else {
      messages.value = []
    }
  }

  function addMessage(msg: Message) {
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

  function setNotifications(list: Notification[]) {
    notifications.value = list
    notiUnreadCount.value = list.filter((n) => !n.isRead).length
  }

  function markRead(notiId: number) {
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

  function setUnreadCount(count: number) {
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
