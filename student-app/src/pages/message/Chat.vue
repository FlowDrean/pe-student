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
    <!-- Emoji 面板 -->
    <EmojiPicker
      v-if="showEmoji"
      class="chat-emoji-panel"
      @select="insertEmoji"
    />
    <div class="chat-input-bar">
      <!-- Emoji 按钮 -->
      <button class="chat-tool-btn" @click="toggleEmoji" :class="{ active: showEmoji }">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M8 14s1.5 2 4 2 4-2 4-2"/><line x1="9" y1="9" x2="9.01" y2="9"/><line x1="15" y1="9" x2="15.01" y2="9"/></svg>
      </button>
      <!-- 图片按钮 -->
      <button class="chat-tool-btn" @click="triggerImageUpload" :disabled="uploading">
        <svg v-if="!uploading" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
        <span v-else class="upload-spinner"></span>
      </button>
      <input
        v-model="inputText"
        class="chat-input"
        placeholder="输入消息..."
        @keyup.enter="sendMsg"
      />
      <button class="chat-send" :disabled="!inputText.trim()" @click="sendMsg">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" :stroke="inputText.trim() ? 'var(--accent-600)' : 'var(--ink-300)'" stroke-width="2.5"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
      </button>
    </div>
    <!-- 隐藏的图片选择器 -->
    <input
      ref="imageInputRef"
      type="file"
      accept="image/*"
      style="display:none"
      @change="handleImageSelect"
    />
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@stores/user'
import { getMessages, sendMessage } from '@api'
import { uploadImage } from '@api/upload'
import { useWebSocket } from '@composables/useWebSocket'
import ChatBubble from '@components/message/ChatBubble.vue'
import EmojiPicker from '@components/message/EmojiPicker.vue'
import Loading from '@components/common/Loading.vue'

const route = useRoute()
const userStore = useUserStore()
const myId = userStore.userId || 0
const peerId = route.params.userId
const inputText = ref('')
const msgListRef = ref(null)
const imageInputRef = ref(null)
const messages = ref([])
const loading = ref(true)
const showEmoji = ref(false)
const uploading = ref(false)

// ── WebSocket 连接 ──
const { connected, connect, subscribe, disconnect } = useWebSocket()

async function initChat() {
  await fetchMessages()
  loading.value = false

  await nextTick()
  scrollToBottom()

  if (!connected.value) {
    await connect()
  }

  subscribe('/user/queue/message', (msg) => {
    if (msg.senderId == peerId || msg.receiverId == peerId) {
      const exists = messages.value.find((m) => m.id === msg.id)
      if (!exists) {
        messages.value.push({
          id: msg.id,
          senderId: msg.senderId,
          content: msg.content,
          type: msg.type || 1,
          imageUrl: msg.imageUrl,
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
    messages.value = (data.list || []).map(m => ({
      ...m,
      imageUrl: m.imageUrl || null,
    }))
  } catch { messages.value = [] }
}

async function sendMsg() {
  const text = inputText.value.trim()
  if (!text) return
  try {
    const msg = await sendMessage(peerId, { content: text, type: 1 })
    messages.value.push({
      id: msg.id || Date.now(),
      senderId: myId,
      content: text,
      type: 1,
      createdAt: new Date().toISOString(),
    })
    inputText.value = ''
    showEmoji.value = false
    scrollToBottom()
  } catch { /* ignore */ }
}

// ── Emoji ──
function toggleEmoji() {
  showEmoji.value = !showEmoji.value
}

function insertEmoji(emoji) {
  inputText.value += emoji
}

// ── 图片上传 ──
function triggerImageUpload() {
  imageInputRef.value?.click()
}

async function handleImageSelect(e) {
  const file = e.target.files?.[0]
  if (!file) return

  // 校验文件大小 (10MB)
  if (file.size > 10 * 1024 * 1024) {
    alert('图片大小不能超过10MB')
    return
  }

  uploading.value = true
  try {
    const result = await uploadImage(file)
    const imageUrl = result.url
    // 发送图片消息
    const msg = await sendMessage(peerId, { content: '[图片]', type: 2, imageUrl })
    messages.value.push({
      id: msg.id || Date.now(),
      senderId: myId,
      content: '[图片]',
      type: 2,
      imageUrl: imageUrl,
      createdAt: new Date().toISOString(),
    })
    scrollToBottom()
  } catch (err) {
    alert('图片上传失败，请重试')
  } finally {
    uploading.value = false
    // 清除文件选择，以便可以重新选择同一文件
    if (imageInputRef.value) {
      imageInputRef.value.value = ''
    }
  }
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
.chat-emoji-panel { flex-shrink: 0; }
.chat-input-bar { display: flex; align-items: center; gap: var(--space-2); padding: var(--space-2) var(--content-padding); padding-bottom: calc(var(--space-2) + env(safe-area-inset-bottom)); background: var(--bg-primary); border-top: 2px solid var(--ink-100); }
.chat-tool-btn { display: flex; align-items: center; justify-content: center; width: 36px; height: 36px; border-radius: var(--radius-full); border: none; background: transparent; color: var(--ink-500); cursor: pointer; transition: all var(--transition-fast); flex-shrink: 0; }
.chat-tool-btn:active { background: var(--ink-100); transform: scale(0.92); }
.chat-tool-btn.active { color: var(--accent-600); background: var(--accent-50); }
.chat-tool-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.chat-input { flex: 1; padding: 10px 14px; background: var(--bg-tertiary); border: 2px solid var(--ink-200); border-radius: var(--radius-full); font-size: var(--text-sm); color: var(--ink-800); transition: all var(--transition-fast); min-width: 0; }
.chat-input:focus { border-color: var(--accent-500); box-shadow: var(--shadow-glow-blue); }
.chat-input::placeholder { color: var(--ink-300); font-style: italic; }
.chat-send { display: flex; align-items: center; justify-content: center; padding: 8px; border-radius: var(--radius-full); border: none; background: transparent; cursor: pointer; transition: all var(--transition-fast); flex-shrink: 0; }
.chat-send:not(:disabled):active { background: var(--accent-50); transform: scale(0.9); }
.chat-send:disabled { cursor: not-allowed; }
.upload-spinner { width: 18px; height: 18px; border: 2px solid var(--ink-200); border-top-color: var(--accent-600); border-radius: 50%; animation: spin 0.6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
