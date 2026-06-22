<template>
  <div class="message-page page">
    <div class="message-page__header">
      <h1 class="message-page__title">消息</h1>
      <router-link to="/notification" class="message-page__noti">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="var(--ink-600)" stroke-width="2"><path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 01-3.46 0"/></svg>
        <span v-if="notiUnread" class="dot"></span>
      </router-link>
    </div>

    <Loading v-if="loading" variant="skeleton" :count="3" />
    <div v-else-if="conversations.length" class="conversation-list">
      <MessagePreview
        v-for="conv in conversations"
        :key="conv.userId"
        :conversation="conv"
        @click="goChat(conv.userId)"
      />
    </div>
    <EmptyState v-else title="暂无消息" description="去逛逛帖子，和同学们互动吧" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@stores/user'
import { useMessageStore } from '@stores/message'
import { getConversations } from '@api'
import { getNotificationUnreadCount } from '@api/notification'
import { useWebSocket } from '@composables/useWebSocket'
import MessagePreview from '@components/message/MessagePreview.vue'
import Loading from '@components/common/Loading.vue'
import EmptyState from '@components/common/EmptyState.vue'

const router = useRouter()
const userStore = useUserStore()
const messageStore = useMessageStore()
const myId = userStore.userId || 0
const notiUnread = computed(() => messageStore.notiUnreadCount)
const loading = ref(true)
const conversations = ref([])

const { connected, connect, subscribe } = useWebSocket()

async function fetchConversations() {
  loading.value = true
  try {
    const data = await getConversations()
    conversations.value = data || []
    messageStore.setConversations(conversations.value)
  } catch { conversations.value = [] }
  finally { loading.value = false }
}

function goChat(userId) { router.push(`/chat/${userId}`) }

async function fetchNotiUnread() {
  try { const count = await getNotificationUnreadCount(); messageStore.notiUnreadCount = count || 0 } catch { /* ignore */ }
}

/** 处理 WebSocket 收到的消息 —— 实时更新会话列表 */
function onMessageReceived(msg) {
  // 过滤掉自己发的消息（自己发的不会出现在自己列表里）
  if (msg.senderId == myId) return

  const senderId = msg.senderId
  const existing = conversations.value.find((c) => c.userId == senderId)
  // 图片消息在会话列表中显示 [图片]
  const displayContent = msg.type === 2 ? '[图片]' : msg.content

  if (existing) {
    // 更新已有会话：刷新最后一条消息 & 时间 & 未读数
    existing.lastMessage = displayContent
    existing.lastTime = msg.createdAt || new Date().toISOString()
    existing.unread = (existing.unread || 0) + 1
    // 置顶
    conversations.value = [
      existing,
      ...conversations.value.filter((c) => c.userId != senderId),
    ]
  } else {
    // 新会话：插入到列表顶部
    conversations.value.unshift({
      userId: senderId,
      nickname: msg.senderName || '用户' + senderId,
      lastMessage: displayContent,
      lastTime: msg.createdAt || new Date().toISOString(),
      unread: 1,
      avatar: null,
    })
  }

  // 同步到 store
  messageStore.setConversations(conversations.value)
}

async function initPage() {
  await fetchConversations()
  fetchNotiUnread()

  // 建立 WebSocket 并订阅消息
  if (!connected.value) {
    try { await connect() } catch { /* 连接失败不影响列表展示 */ }
  }
  if (connected.value) {
    subscribe('/user/queue/message', onMessageReceived)
  }
}

onMounted(() => { initPage() })
</script>

<style scoped>
.message-page { background: var(--bg-secondary); }
.message-page__header {
  display: flex; justify-content: space-between; align-items: center;
  padding: var(--space-4) var(--content-padding);
  padding-top: calc(env(safe-area-inset-top) + var(--space-4));
  background: var(--bg-primary);
  border-bottom: 2px solid var(--ink-100);
}
.message-page__title { font-family: var(--font-display); font-size: var(--text-2xl); font-weight: var(--font-black); color: var(--ink-900); letter-spacing: -0.02em; }
.message-page__noti { position: relative; padding: 6px; border-radius: var(--radius-md); transition: all var(--transition-fast); }
.message-page__noti:active { background: var(--ink-50); }
.dot { position: absolute; top: 4px; right: 4px; width: 8px; height: 8px; background: var(--error); border-radius: 2px; box-shadow: 0 1px 0 0 rgba(220, 38, 38, 0.3); }
.conversation-list { background: var(--bg-primary); }
</style>
