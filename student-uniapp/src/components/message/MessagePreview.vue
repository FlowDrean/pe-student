<template>
  <view
    class="message-preview"
    :class="{ 'message-preview--unread': conversation.unread > 0 }"
    @tap="$emit('click')"
  >
    <!-- 头像 -->
    <UserAvatar :avatar="conversation.avatar" :name="conversation.nickname" :user-id="conversation.userId" :size="44" />

    <!-- 中间内容 -->
    <view class="message-preview__body">
      <view class="message-preview__top">
        <text class="message-preview__nickname">{{ conversation.nickname || '用户' }}</text>
        <text class="message-preview__time">{{ timeAgo(conversation.lastTime) }}</text>
      </view>
      <view class="message-preview__bottom">
        <text class="message-preview__last-msg">{{ conversation.lastMessage || '' }}</text>
        <view v-if="conversation.unread > 0" class="message-preview__badge">
          <text class="message-preview__badge-text">{{ unreadText }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { timeAgo } from '@utils/format'
import UserAvatar from '@components/common/UserAvatar.vue'

interface Conversation {
  userId: number
  nickname?: string
  avatar?: string
  lastMessage?: string
  lastTime: string
  unread: number
}

const props = defineProps<{
  conversation: Conversation
}>()

defineEmits<{
  click: []
}>()

const unreadText = computed(() => {
  const n = props.conversation.unread
  if (n > 99) return '99+'
  return String(n)
})
</script>

<style scoped>
.message-preview {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: var(--bg-primary);
  border-left: 3px solid transparent;
  border-bottom: 1px solid var(--ink-50);
  transition: all 0.15s ease;
}

.message-preview:active {
  background: var(--bg-secondary);
}

.message-preview--unread {
  border-left-color: var(--accent-500);
}

/* ── 头像 ── */
.message-preview__avatar {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message-preview__avatar-text {
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  font-family: var(--font-display);
}

/* ── 内容 ── */
.message-preview__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.message-preview__top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.message-preview__nickname {
  font-size: 15px;
  font-weight: 600;
  color: var(--ink-800);
}

.message-preview__time {
  font-size: 11px;
  color: var(--ink-300);
  flex-shrink: 0;
  margin-left: 8px;
}

.message-preview__bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.message-preview__last-msg {
  font-size: 13px;
  color: var(--ink-400);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}

.message-preview__badge {
  background: #dc2626;
  border-radius: var(--radius-full);
  min-width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
  flex-shrink: 0;
  margin-left: 8px;
}

.message-preview__badge-text {
  color: #fff;
  font-size: 11px;
  font-weight: 600;
}
</style>
