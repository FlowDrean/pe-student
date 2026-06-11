<template>
  <div class="chat-page">
    <Loading v-if="loading" variant="skeleton" :count="5" />
    <div v-else class="chat-messages" ref="msgListRef">
      <ChatBubble
        v-for="msg in messages"
        :key="msg.id"
        :message="msg"
        :is-mine="msg.senderId === myId"
      />
    </div>
    <div class="chat-input-bar">
      <input v-model="inputText" class="chat-input" placeholder="输入消息..." @keyup.enter="sendMsg" />
      <button class="chat-send" :disabled="!inputText.trim()" @click="sendMsg">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" :stroke="inputText.trim() ? 'var(--accent-600)' : 'var(--ink-300)'" stroke-width="2.5"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@stores/user'
import { getMessages, sendMessage } from '@api'
import { useWebSocket } from '@composables/useWebSocket'
import ChatBubble from '@components/message/ChatBubble.vue'
import Loading from '@components/common/Loading.vue'

const route = useRoute()
const userStore = useUserStore()
const myId = userStore.userId || 0
const peerId = route.params.userId
const inputText = ref('')
const msgListRef = ref(null)
const messages = ref([])
const loading = ref(true)

// ── WebSocket 连接 ──
const { connected, connect, subscribe, disconnect } = useWebSocket()

async function initChat() {
  // 1. 先获取历史消息
  await fetchMessages()
  loading.value = false

  // 2. 建立 WebSocket 连接
  if (!connected.value) {
    await connect()
  }

  // 3. 订阅新消息推送
  subscribe('/user/queue/message', (msg) => {
    // 只处理当前聊天对象的消息
    if (msg.senderId == peerId || msg.receiverId == peerId) {
      // 去重（防止和发送的回显重复）
      const exists = messages.value.find((m) => m.id === msg.id)
      if (!exists) {
        messages.value.push({
          id: msg.id,
          senderId: msg.senderId,
          content: msg.content,
          type: msg.type || 1,
          createdAt: msg.createdAt || new Date().toISOString(),
        })
        scrollToBottom()
      }
    }
  })
}

async function fetchMessages() {
  try {
    const data = await getMessages(peerId, { page: 1, size: 50 })
    messages.value = data.list || []
  } catch { messages.value = [] }
}

async function sendMsg() {
  if (!inputText.value.trim()) return
  try {
    const msg = await sendMessage(peerId, { content: inputText.value, type: 1 })
    // 乐观更新：立即显示发送的消息
    messages.value.push({
      id: msg.id || Date.now(),
      senderId: myId,
      content: inputText.value,
      type: 1,
      createdAt: new Date().toISOString(),
    })
    inputText.value = ''
    scrollToBottom()
  } catch { /* ignore */ }
}

function scrollToBottom() {
  nextTick(() => {
    const el = msgListRef.value
    if (el) {
      el.scrollTo({ top: el.scrollHeight, behavior: 'smooth' })
    }
  })
}

onMounted(() => {
  initChat()
})
</script>

<style scoped>
.chat-page { display: flex; flex-direction: column; height: calc(100vh - var(--header-height)); background: var(--bg-secondary); }
.chat-messages { flex: 1; overflow-y: auto; padding: var(--space-4) var(--content-padding); display: flex; flex-direction: column; gap: var(--space-3); }
.chat-input-bar { display: flex; align-items: center; gap: var(--space-3); padding: var(--space-3) var(--content-padding); padding-bottom: calc(var(--space-3) + env(safe-area-inset-bottom)); background: var(--bg-primary); border-top: 2px solid var(--ink-100); }
.chat-input { flex: 1; padding: 10px 16px; background: var(--bg-tertiary); border: 2px solid var(--ink-200); border-radius: var(--radius-full); font-size: var(--text-sm); color: var(--ink-800); transition: all var(--transition-fast); }
.chat-input:focus { border-color: var(--accent-500); box-shadow: var(--shadow-glow-blue); }
.chat-input::placeholder { color: var(--ink-300); font-style: italic; }
.chat-send { display: flex; align-items: center; justify-content: center; padding: 8px; border-radius: var(--radius-full); transition: all var(--transition-fast); }
.chat-send:not(:disabled):active { background: var(--accent-50); transform: scale(0.9); }
</style>
