<template>
  <div class="chat-bubble" :class="{ 'chat-bubble--mine': isMine }">
    <div class="chat-bubble__content">
      {{ message.content }}
    </div>
    <span class="chat-bubble__time">{{ formatTime(message.createdAt) }}</span>
  </div>
</template>

<script setup>
import { formatDate } from '@utils/format'
defineProps({ message: { type: Object, required: true }, isMine: { type: Boolean, default: false } })
function formatTime(d) { return formatDate(d, 'HH:mm') }
</script>

<style scoped>
.chat-bubble { display: flex; flex-direction: column; align-items: flex-start; gap: 4px; max-width: 80%; }
.chat-bubble--mine { align-self: flex-end; align-items: flex-end; }
.chat-bubble__content { padding: 10px 14px; border-radius: 16px; font-size: var(--text-sm); line-height: 1.6; word-break: break-word; }
.chat-bubble:not(.chat-bubble--mine) .chat-bubble__content { background: var(--bg-primary); border: 1.5px solid var(--ink-100); border-bottom-left-radius: 4px; color: var(--ink-800); }
.chat-bubble--mine .chat-bubble__content { background: var(--accent-600); color: #fff; border-bottom-right-radius: 4px; }
.chat-bubble__time { font-family: var(--font-mono); font-size: 10px; color: var(--ink-300); padding: 0 4px; }
</style>
