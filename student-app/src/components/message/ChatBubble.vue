<template>
  <div class="chat-bubble" :class="{ 'chat-bubble--mine': isMine }">
    <!-- 文本消息 -->
    <div v-if="message.type === 1 || !message.type" class="chat-bubble__content chat-bubble__text">
      {{ message.content }}
    </div>
    <!-- 图片消息 -->
    <div v-else-if="message.type === 2" class="chat-bubble__image" @click="previewImage">
      <img :src="message.imageUrl || message.content" alt="图片" loading="lazy" />
    </div>
    <span class="chat-bubble__time">{{ formatTime(message.createdAt) }}</span>
  </div>
</template>

<script setup>
import { formatDate } from '@utils/format'

defineProps({ message: { type: Object, required: true }, isMine: { type: Boolean, default: false } })

function formatTime(d) { return formatDate(d, 'HH:mm') }

function previewImage(e) {
  const img = e.currentTarget.querySelector('img')
  if (!img) return
  // 创建全屏遮罩预览
  const overlay = document.createElement('div')
  overlay.style.cssText = 'position:fixed;inset:0;background:rgba(0,0,0,0.92);z-index:9999;display:flex;align-items:center;justify-content:center;cursor:pointer;'
  const preview = document.createElement('img')
  preview.src = img.src
  preview.style.cssText = 'max-width:95vw;max-height:95vh;object-fit:contain;border-radius:8px;'
  overlay.appendChild(preview)
  overlay.addEventListener('click', () => overlay.remove())
  document.body.appendChild(overlay)
}
</script>

<style scoped>
.chat-bubble { display: flex; flex-direction: column; align-items: flex-start; gap: 4px; max-width: 80%; }
.chat-bubble--mine { align-self: flex-end; align-items: flex-end; }
.chat-bubble__content { padding: 10px 14px; border-radius: 16px; font-size: var(--text-sm); line-height: 1.6; word-break: break-word; }
.chat-bubble:not(.chat-bubble--mine) .chat-bubble__content { background: var(--bg-primary); border: 1.5px solid var(--ink-100); border-bottom-left-radius: 4px; color: var(--ink-800); }
.chat-bubble--mine .chat-bubble__content { background: var(--accent-600); color: #fff; border-bottom-right-radius: 4px; }
.chat-bubble__text { white-space: pre-wrap; }
.chat-bubble__image { padding: 2px; border-radius: 12px; overflow: hidden; cursor: pointer; max-width: 60%; }
.chat-bubble:not(.chat-bubble--mine) .chat-bubble__image { background: var(--bg-primary); border: 1.5px solid var(--ink-100); border-bottom-left-radius: 4px; }
.chat-bubble--mine .chat-bubble__image { background: var(--accent-600); border-bottom-right-radius: 4px; }
.chat-bubble__image img { display: block; max-width: 100%; max-height: 240px; width: auto; height: auto; border-radius: 8px; object-fit: cover; transition: transform var(--transition-fast); }
.chat-bubble__image:active img { transform: scale(0.97); }
.chat-bubble__time { font-family: var(--font-mono); font-size: 10px; color: var(--ink-300); padding: 0 4px; }
</style>
