<template>
  <div class="msg-preview" :class="{ unread: conversation.unread > 0 }" @click="$emit('click')">
    <div class="avatar avatar-md" :style="{ background: color }"><span class="avatar-text">{{ conversation.nickname?.charAt(0) }}</span></div>
    <div class="msg-preview__body">
      <div class="msg-preview__header">
        <span class="msg-preview__name">{{ conversation.nickname }}</span>
        <span class="msg-preview__time">{{ timeAgo(conversation.lastTime) }}</span>
      </div>
      <div class="msg-preview__footer">
        <span class="msg-preview__msg text-ellipsis">{{ conversation.lastMessage }}</span>
        <span v-if="conversation.unread" class="msg-preview__badge">{{ conversation.unread > 99 ? '99+' : conversation.unread }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { timeAgo } from '@utils/format'
const props = defineProps({ conversation: { type: Object, required: true } })
defineEmits(['click'])
const color = computed(() => ['var(--accent-600)','var(--coral-500)','var(--success)','var(--amber-500)'][(props.conversation.userId || 0) % 4])
</script>

<style scoped>
.msg-preview { display: flex; align-items: center; gap: var(--space-3); padding: var(--space-4) var(--content-padding); background: var(--bg-primary); border-bottom: 1px solid var(--ink-50); cursor: pointer; transition: background var(--transition-fast); }
.msg-preview:active { background: var(--ink-50); }
.msg-preview.unread { border-left: 3px solid var(--accent-500); }
.avatar-text { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; color: #fff; font-family: var(--font-display); font-weight: var(--font-bold); border-radius: var(--radius-full); font-size: var(--text-base); }
.msg-preview__body { flex: 1; min-width: 0; }
.msg-preview__header { display: flex; justify-content: space-between; margin-bottom: 3px; }
.msg-preview__name { font-size: var(--text-base); font-weight: var(--font-semibold); color: var(--ink-800); }
.msg-preview__time { font-family: var(--font-mono); font-size: var(--text-2xs); color: var(--ink-300); }
.msg-preview__footer { display: flex; justify-content: space-between; align-items: center; }
.msg-preview__msg { font-size: var(--text-sm); color: var(--ink-400); flex: 1; }
.msg-preview__badge { min-width: 18px; height: 18px; padding: 0 5px; border-radius: 4px; background: var(--error); color: #fff; font-family: var(--font-mono); font-size: 10px; font-weight: var(--font-bold); line-height: 18px; text-align: center; box-shadow: 0 2px 0 0 rgba(220,38,38,0.2); }
</style>
