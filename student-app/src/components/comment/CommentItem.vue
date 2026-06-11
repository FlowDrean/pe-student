<template>
  <div class="comment-item">
    <div class="avatar avatar-sm" :style="{ background: avatarColor }">
      <span class="avatar-text">{{ comment.user?.nickname?.charAt(0) || '?' }}</span>
    </div>
    <div class="comment-item__body">
      <div class="comment-item__header">
        <span class="comment-item__name">{{ comment.user?.nickname || '匿名' }}</span>
        <span class="comment-item__time">{{ timeAgo(comment.createdAt) }}</span>
      </div>
      <p class="comment-item__content">{{ comment.content }}</p>
      <div class="comment-item__footer">
        <button class="comment-item__action" @click="handleLike">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 00-3-3l-4 9v11h11.28a2 2 0 002-1.7l1.38-9a2 2 0 00-2-2.3H14zM7 22H4a2 2 0 01-2-2v-7a2 2 0 012-2h3"/></svg>
          {{ comment.likes || '' }}
        </button>
        <button class="comment-item__action">回复</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { timeAgo } from '@utils/format'
const props = defineProps({ comment: { type: Object, required: true } })
const avatarColor = computed(() => { const colors = ['#07C160', '#f97316', '#059669', '#f59e0b', '#7c3aed']; return colors[(props.comment.user?.id || 0) % colors.length] })
function handleLike() {}
</script>

<style scoped>
.comment-item { display: flex; gap: var(--space-3); padding: var(--space-3) 0; }
.avatar-text { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; color: #fff; font-family: var(--font-display); font-size: 12px; font-weight: var(--font-bold); border-radius: var(--radius-full); }
.comment-item__body { flex: 1; min-width: 0; }
.comment-item__header { display: flex; align-items: center; gap: var(--space-2); margin-bottom: 3px; }
.comment-item__name { font-size: var(--text-sm); font-weight: var(--font-semibold); color: var(--ink-700); }
.comment-item__time { font-size: var(--text-2xs); color: var(--ink-300); }
.comment-item__content { font-size: var(--text-sm); color: var(--ink-600); line-height: 1.6; word-break: break-word; }
.comment-item__footer { display: flex; gap: var(--space-4); margin-top: var(--space-2); }
.comment-item__action { display: flex; align-items: center; gap: 3px; font-size: var(--text-2xs); color: var(--ink-400); font-weight: var(--font-medium); transition: color var(--transition-fast); }
.comment-item__action:active { color: var(--ink-600); }
</style>
