<template>
  <view class="comment-item">
    <!-- 头像 -->
    <view class="comment-item__avatar" :style="{ background: avatarColor }">
      <text class="comment-item__avatar-text">{{ firstChar }}</text>
    </view>

    <!-- 内容区 -->
    <view class="comment-item__body">
      <view class="comment-item__header">
        <text class="comment-item__nickname">{{ comment.user?.nickname || '匿名用户' }}</text>
        <text class="comment-item__time">{{ timeAgo(comment.createdAt) }}</text>
      </view>
      <text class="comment-item__content">{{ comment.content }}</text>
    </view>

    <!-- 点赞 -->
    <view
      class="comment-item__like"
      :class="{ 'is-active': comment.isLiked }"
      @tap.stop="$emit('like')"
    >
      <wd-icon :name="comment.isLiked ? 'heart-filled' : 'heart'" size="15px" :color="comment.isLiked ? '#dc2626' : 'var(--ink-400)'" />
      <text v-if="comment.likes" class="comment-item__like-count">{{ comment.likes }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { timeAgo } from '@utils/format'

interface CommentUser {
  nickname?: string
  avatar?: string
  id?: number
}

interface Comment {
  id: number
  content: string
  user?: CommentUser
  createdAt: string
  likes?: number
  isLiked?: boolean
}

const props = defineProps<{
  comment: Comment
}>()

defineEmits<{
  like: []
  reply: []
}>()

const firstChar = computed(() => (props.comment.user?.nickname || '?').charAt(0))

const avatarColor = computed(() => {
  const colors = ['#07C160', '#f97316', '#059669', '#f59e0b', '#dc2626', '#7c3aed']
  const idx = (props.comment.user?.id || 0) % colors.length
  return colors[idx]
})
</script>

<style scoped>
.comment-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  background: var(--bg-primary);
  border-bottom: 1px solid var(--ink-50);
}

.comment-item:last-child {
  border-bottom: none;
}

/* ── 头像 ── */
.comment-item__avatar {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.comment-item__avatar-text {
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  font-family: var(--font-display);
}

/* ── 内容 ── */
.comment-item__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.comment-item__header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.comment-item__nickname {
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-700);
}

.comment-item__time {
  font-size: 11px;
  color: var(--ink-400);
}

.comment-item__content {
  font-size: 14px;
  color: var(--ink-600);
  line-height: 1.55;
  word-break: break-word;
}

/* ── 点赞 ── */
.comment-item__like {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 4px 6px;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}

.comment-item__like:active {
  background: var(--ink-50);
  transform: scale(0.92);
}
.comment-item__like-count {
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--ink-400);
}

.comment-item__like.is-active .comment-item__like-count {
  color: #dc2626;
}
</style>
