<template>
  <view class="chat-page">

    <!-- Message List -->
    <scroll-view
      class="chat-page__messages"
      :style="{ paddingBottom: messagesPaddingBottom + 'px' }"
      scroll-y
      :scroll-into-view="scrollToId"
      :scroll-with-animation="true"
    >
      <view v-if="loading" class="chat-page__loading">
        <text class="chat-page__loading-text">加载中...</text>
      </view>
      <view v-else-if="messages.length === 0" class="chat-page__empty">
        <wd-icon name="chat" size="48px" color="var(--ink-200)" />
        <text class="chat-page__empty-text">发送第一条消息吧</text>
      </view>
      <view v-else class="chat-page__msg-list">
        <template v-for="msg in messages" :key="msg.id">
          <!-- 对方的消息：头像在左，气泡在右 -->
          <view v-if="msg.senderId !== userStore.userId" :id="`msg-${msg.id}`" class="chat-page__msg-row">
            <UserAvatar :avatar="peerAvatar" :name="peerName" :user-id="peerId" :size="36" />
            <view
              class="chat-page__bubble"
              :class="{
                'chat-page__bubble--other': true,
                'chat-page__bubble--image': msg.type === 2,
              }"
            >
              <image
                v-if="msg.type === 2 && msg.imageUrl"
                :src="resolveAssetUrl(msg.imageUrl)"
                mode="widthFix"
                class="chat-page__bubble-img"
                @tap="previewImage(msg.imageUrl)"
              />
              <text v-else class="chat-page__bubble-text">{{ msg.content }}</text>
            </view>
          </view>
          <!-- 自己的消息：气泡在左，头像在右 -->
          <view v-else :id="`msg-${msg.id}`" class="chat-page__msg-row chat-page__msg-row--self">
            <view
              class="chat-page__bubble"
              :class="{
                'chat-page__bubble--self': true,
                'chat-page__bubble--image': msg.type === 2,
              }"
            >
              <image
                v-if="msg.type === 2 && msg.imageUrl"
                :src="resolveAssetUrl(msg.imageUrl)"
                mode="widthFix"
                class="chat-page__bubble-img"
                @tap="previewImage(msg.imageUrl)"
              />
              <text v-else class="chat-page__bubble-text">{{ msg.content }}</text>
            </view>
            <UserAvatar :avatar="selfAvatarUrl" :name="userStore.nickname" :user-id="userStore.userId || 0" :size="36" />
          </view>
        </template>
        <view id="chat-bottom" class="chat-page__bottom-anchor" />
      </view>
    </scroll-view>

    <!-- Bottom Fixed Area (Emoji + Input) -->
    <view class="chat-page__bottom-bar" :style="{ paddingBottom: safeAreaBottom + 'px' }">
      <!-- Emoji Picker -->
      <view v-if="showEmoji" class="chat-page__emoji-panel">
        <view class="chat-page__emoji-grid">
          <text
            v-for="emoji in emojis"
            :key="emoji"
            class="chat-page__emoji-item"
            @tap="insertEmoji(emoji)"
          >{{ emoji }}</text>
        </view>
      </view>

      <!-- Input Bar -->
      <view class="chat-page__input-bar">
        <view class="chat-page__input-btn" @tap="toggleEmoji">
          <text>{{ showEmoji ? '😊' : '😐' }}</text>
        </view>
        <view class="chat-page__input-btn" @tap="chooseImage">
          <wd-icon name="picture" size="22px" />
        </view>
        <input
          v-model="inputText"
          class="chat-page__input"
          type="text"
          placeholder="输入消息..."
          :confirm-type="'send'"
          @confirm="sendText"
        />
        <view
          class="chat-page__send-btn"
          :class="{ 'chat-page__send-btn--disabled': !inputText.trim() }"
          @tap="sendText"
        >
          <text class="chat-page__send-btn-text">发送</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onUnmounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { messageApi } from '@api/message'
import { userApi } from '@api/user'
import { uploadImage } from '@api/upload'
import { resolveAssetUrl } from '@/http'
import { useUserStore } from '@stores/user'
import { useMessageStore } from '@stores/message'
import { useSocket } from '@composables/useSocket'
import UserAvatar from '@components/common/UserAvatar.vue'

const userStore = useUserStore()
const messageStore = useMessageStore()
const { connect, subscribeTo, connected } = useSocket()

// 获取系统信息用于计算固定底部栏高度
const systemInfo = uni.getSystemInfoSync()
const safeAreaBottom = systemInfo.safeAreaInsets?.bottom || 0
// input bar 基础高度 + emoji panel 高度（展开时约200px）
const INPUT_BAR_HEIGHT = 48
const EMOJI_PANEL_HEIGHT = 200

const peerId = ref<number>(0)
const peerName = ref('用户')
const peerAvatar = ref('')
const messages = ref<any[]>([])
const inputText = ref('')
const showEmoji = ref(false)
const scrollToId = ref('')
const loading = ref(true)
const sending = ref(false)

// 动态计算消息区域底部内边距，防止内容被固定底部栏遮挡
const messagesPaddingBottom = computed(() => {
  const inputH = INPUT_BAR_HEIGHT + 16 // input bar + padding
  const emojiH = showEmoji.value ? EMOJI_PANEL_HEIGHT : 0
  return inputH + emojiH + safeAreaBottom
})

let pollTimer: number | null = null
let unsubWs: (() => void) | null = null

const emojis = ['😀','😂','🤣','😍','🥰','😘','😜','😎','🤩','😇','🤔','😅','😢','😡','👍','👎','🙏','💪','👏','🔥','❤️','💔','🎉','🌟','🍕','☕','🎵','📚','✏️','💻','🌞','🌈','🌸','🐱','🐶','🎮','⚽']

const selfAvatarUrl = computed(() => userStore.userInfo?.avatar || '')

// 使用 ref 管理已去重 ID，确保跨会话正确重置
const messageIds = ref<Set<number>>(new Set())

/** 去重添加消息，返回 true 表示新增 */
function addMessageDedup(msg: any): boolean {
  const id = Number(msg.id)
  // 有效 ID 且已存在 → 跳过
  if (id && messageIds.value.has(id)) return false

  // 无效 ID 时使用 senderId + content + createdAt 组合去重
  if (!id) {
    const dupKey = `${msg.senderId}_${msg.content}_${msg.createdAt}`
    const exists = messages.value.some(
      (m: any) => `${m.senderId}_${m.content}_${m.createdAt}` === dupKey
    )
    if (exists) return false
  }

  // 二次确认：ID 有效时也检查 messages 数组中是否已存在（防御性去重）
  if (id && messages.value.some((m: any) => Number(m.id) === id)) return false

  if (id) messageIds.value.add(id)
  messages.value.push(msg)
  // 同步更新 store（按 id 去重）
  if (id && !messageStore.messages.some((m: any) => Number(m.id) === id)) {
    messageStore.addMessage(msg)
  } else if (!id) {
    messageStore.addMessage(msg)
  }
  scrollToBottom()
  return true
}

onLoad((options: any) => {
  peerId.value = Number(options.peerId) || 0
  peerName.value = decodeURIComponent(options.peerName || '用户')
  uni.setNavigationBarTitle({ title: peerName.value })

  // 拉取对方头像（并兜底补全昵称）
  if (peerId.value) {
    userApi.getUserById(peerId.value).then((u: any) => {
      if (!u) return
      peerAvatar.value = u.avatar || ''
      if (!options.peerName || options.peerName === 'undefined') {
        peerName.value = u.nickname || u.username || peerName.value
        uni.setNavigationBarTitle({ title: peerName.value })
      }
    }).catch(() => {})
  }

  // 进入新会话时重置消息列表和去重集合
  messages.value = []
  messageIds.value = new Set()

  fetchMessages()
  connect()
  // 只接收对方发来的消息，自己发的已在 sendText/chooseImage 中处理
  const handler = (msg: any) => {
    if (msg.senderId === userStore.userId) return
    if (msg.senderId === peerId.value && msg.receiverId === userStore.userId) {
      addMessageDedup(msg)
    }
  }
  // 订阅服务端推送（发送 STOMP SUBSCRIBE 帧）
	  unsubWs = subscribeTo('/user/queue/message', handler)
  startPolling()
})

onUnmounted(() => {
  stopPolling()
  if (unsubWs) {
    unsubWs()
    unsubWs = null
  }
})

async function fetchMessages() {
  try {
    const data = await messageApi.getMessages(peerId.value)
    const list = Array.isArray(data) ? data : (data?.list || data?.records || [])
    // 用 addMessageDedup 逐条合并，避免直接赋值覆盖 WebSocket 已收到的消息
    for (const msg of list) {
      addMessageDedup(msg)
    }
    messageStore.setMessages(messages.value)
    await nextTick()
    scrollToBottom()
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function scrollToBottom() {
  scrollToId.value = ''
  nextTick(() => {
    scrollToId.value = 'chat-bottom'
  })
}

function toggleEmoji() {
  showEmoji.value = !showEmoji.value
}

function insertEmoji(emoji: string) {
  inputText.value += emoji
}

async function sendText() {
  if (sending.value) return
  const content = inputText.value.trim()
  if (!content) return
  sending.value = true
  inputText.value = ''
  showEmoji.value = false

  // 乐观更新：先生成临时消息立即渲染
  const tempId = -Date.now()
  const tempMsg: any = {
    id: tempId,
    senderId: userStore.userId,
    receiverId: peerId.value,
    content,
    type: 1,
    imageUrl: null,
    isRead: 0,
    createdAt: new Date().toISOString(),
  }
  addMessageDedup(tempMsg)

  try {
    const msg = await messageApi.sendMessage(peerId.value, { content, type: 1 })
    // 用服务端返回的真实消息替换临时消息
    const idx = messages.value.findIndex((m: any) => m.id === tempId)
    if (idx >= 0) {
      messages.value.splice(idx, 1, msg)
      messageIds.value.delete(tempId)
      messageIds.value.add(msg.id)
    }
  } catch {
    // 发送失败：移除临时消息并恢复输入
    const idx = messages.value.findIndex((m: any) => m.id === tempId)
    if (idx >= 0) messages.value.splice(idx, 1)
    messageIds.value.delete(tempId)
    inputText.value = content
    uni.showToast({ title: '发送失败，请重试', icon: 'none' })
  } finally {
    sending.value = false
  }
}

async function chooseImage() {
  if (sending.value) return
  try {
    const res = await uni.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
    })
    const filePath = res.tempFilePaths[0]
    sending.value = true
    uni.showLoading({ title: '发送中...' })

    // 乐观更新：先用本地临时路径渲染图片
    const tempId = -Date.now()
    const tempMsg: any = {
      id: tempId,
      senderId: userStore.userId,
      receiverId: peerId.value,
      content: '[图片]',
      type: 2,
      imageUrl: filePath,
      isRead: 0,
      createdAt: new Date().toISOString(),
    }
    addMessageDedup(tempMsg)

    const uploadResult = await uploadImage(filePath)
    const msg = await messageApi.sendMessage(peerId.value, {
      content: '[图片]',
      type: 2,
      imageUrl: (uploadResult as any).url || uploadResult,
    } as any)
    // 用服务端返回的真实消息替换临时消息
    const idx = messages.value.findIndex((m: any) => m.id === tempId)
    if (idx >= 0) {
      messages.value.splice(idx, 1, msg)
      messageIds.value.delete(tempId)
      messageIds.value.add(msg.id)
    }
  } catch (e: any) {
    if (e?.errMsg?.includes('cancel')) return
    // 移除临时消息
    const tempId = messages.value.find((m: any) => m.id < 0)?.id
    if (tempId) {
      const idx = messages.value.findIndex((m: any) => m.id === tempId)
      if (idx >= 0) messages.value.splice(idx, 1)
      messageIds.value.delete(tempId)
    }
    uni.showToast({ title: '发送失败', icon: 'none' })
  } finally {
    sending.value = false
    uni.hideLoading()
  }
}

function previewImage(url: string) {
  uni.previewImage({ urls: [resolveAssetUrl(url)] })
}

// Polling fallback
function startPolling() {
  pollTimer = setInterval(async () => {
    if (connected.value) return
    try {
      const data = await messageApi.getMessages(peerId.value)
      const list = Array.isArray(data) ? data : (data?.list || data?.records || [])
      let added = false
      for (const msg of list) {
        if (addMessageDedup(msg)) added = true
      }
      if (added) scrollToBottom()
    } catch {
      // silent
    }
  }, 5000) as unknown as number
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}
</script>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--bg-secondary);
  overflow: hidden;
}

/* ── Messages ── */
.chat-page__messages {
  flex: 1;
  min-height: 0;
  padding: 12px 16px;
}

.chat-page__loading,
.chat-page__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
}

.chat-page__loading-text {
  font-size: 14px;
  color: var(--ink-300);
}

.chat-page__empty-text {
  font-size: 14px;
  color: var(--ink-300);
}

.chat-page__msg-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-page__msg-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

/* 自己的消息靠右 */
.chat-page__msg-row--self {
  justify-content: flex-end;
}

.chat-page__msg-avatar {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.chat-page__msg-avatar-text {
  color: #fff;
  font-size: 14px;
  font-weight: 700;
}

.chat-page__bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: var(--radius-lg);
  word-break: break-all;
}

.chat-page__bubble--self {
  background: var(--accent-500);
  border-bottom-right-radius: 4px;
}

.chat-page__bubble--self .chat-page__bubble-text {
  color: #fff;
}

.chat-page__bubble--other {
  background: var(--bg-primary);
  border: 2px solid var(--ink-100);
  border-bottom-left-radius: 4px;
}

.chat-page__bubble--image {
  padding: 4px;
  border-radius: var(--radius-md);
}

.chat-page__bubble-text {
  font-size: 15px;
  color: var(--ink-800);
  line-height: 1.5;
}

.chat-page__bubble-img {
  max-width: 200px;
  border-radius: var(--radius-sm);
  display: block;
}

.chat-page__bottom-anchor {
  height: 1px;
}

/* ── Bottom Bar (Emoji + Input, fixed) ── */
.chat-page__bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 200;
  background: var(--bg-primary);
  flex-shrink: 0;
}

/* ── Emoji Panel ── */
.chat-page__emoji-panel {
  background: var(--bg-primary);
  border-top: 2px solid var(--ink-100);
  padding: 12px 16px;
  max-height: 200px;
  overflow-y: auto;
}

.chat-page__emoji-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-start;
}

.chat-page__emoji-item {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  border-radius: var(--radius-sm);
}

.chat-page__emoji-item:active {
  background: var(--ink-50);
}

/* ── Input Bar ── */
.chat-page__input-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--bg-primary);
  border-top: 2px solid var(--ink-100);
}

.chat-page__input-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  flex-shrink: 0;
}

.chat-page__input-btn:active {
  background: var(--ink-50);
}

.chat-page__input-btn text {
  font-size: 18px;
}

.chat-page__input {
  flex: 1;
  height: 36px;
  padding: 0 12px;
  background: var(--bg-tertiary);
  border: 2px solid var(--ink-200);
  border-radius: 18px;
  font-size: 14px;
  color: var(--ink-800);
}

.chat-page__send-btn {
  padding: 8px 16px;
  background: var(--accent-500);
  border-radius: 18px;
  flex-shrink: 0;
}

.chat-page__send-btn:active {
  background: var(--accent-600);
  transform: scale(0.96);
}

.chat-page__send-btn--disabled {
  background: var(--ink-200);
}

.chat-page__send-btn-text {
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}
</style>
