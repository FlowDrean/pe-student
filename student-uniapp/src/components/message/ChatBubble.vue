<template>
  <view class="chat-bubble" :class="{ 'chat-bubble--mine': isMine }">
    <!-- 文本消息 -->
    <view v-if="message.type === 1 || !message.type" class="chat-bubble__content chat-bubble__text">
      <text>{{ message.content }}</text>
    </view>

    <!-- 图片消息 -->
    <view v-else-if="message.type === 2" class="chat-bubble__content chat-bubble__image-wrap" @tap="previewImage">
      <image
        :src="resolveAssetUrl(message.imageUrl || message.content)"
        mode="widthFix"
        class="chat-bubble__image"
      />
    </view>

    <!-- 时间 -->
    <text class="chat-bubble__time">{{ formatTime(message.createdAt) }}</text>
  </view>
</template>

<script setup lang="ts">
import { formatDate } from '@utils/format'
import { resolveAssetUrl } from '@/http'

interface ChatMessage {
  id: number | string
  content: string
  type?: number
  imageUrl?: string
  createdAt: string
  senderId?: number
}

defineProps<{
  message: ChatMessage
  isMine?: boolean
}>()

const emit = defineEmits<{
  previewImage: [url: string]
}>()

function formatTime(dateStr: string): string {
  return formatDate(dateStr, 'HH:mm')
}

function previewImage(message: ChatMessage) {
  const url = message.imageUrl || message.content
  if (url) {
    emit('previewImage', url)
  }
}
</script>

<style scoped>
.chat-bubble {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  max-width: 80%;
}

.chat-bubble--mine {
  align-self: flex-end;
  align-items: flex-end;
}

/* ── 气泡内容 ── */
.chat-bubble__content {
  padding: 10px 14px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.chat-bubble:not(.chat-bubble--mine) .chat-bubble__content {
  background: var(--bg-primary);
  border: 1.5px solid var(--ink-100);
  border-bottom-left-radius: 4px;
  color: var(--ink-800);
}

.chat-bubble--mine .chat-bubble__content {
  background: var(--accent-500);
  color: #fff;
  border-bottom-right-radius: 4px;
}

/* ── 文本 ── */
.chat-bubble__text {
  white-space: pre-wrap;
}

/* ── 图片 ── */
.chat-bubble__image-wrap {
  padding: 2px;
  border-radius: 12px;
  overflow: hidden;
  max-width: 60%;
  cursor: pointer;
}

.chat-bubble:not(.chat-bubble--mine) .chat-bubble__image-wrap {
  background: var(--bg-primary);
  border: 1.5px solid var(--ink-100);
  border-bottom-left-radius: 4px;
}

.chat-bubble--mine .chat-bubble__image-wrap {
  background: var(--accent-500);
  border-bottom-right-radius: 4px;
}

.chat-bubble__image {
  display: block;
  max-width: 100%;
  max-height: 240px;
  width: auto;
  border-radius: 8px;
}

.chat-bubble__image-wrap:active .chat-bubble__image {
  transform: scale(0.97);
}

/* ── 时间 ── */
.chat-bubble__time {
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--ink-300);
  padding: 0 4px;
}
</style>
