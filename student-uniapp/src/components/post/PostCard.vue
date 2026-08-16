<template>
  <view class="post-card" :data-post-id="post.id" @tap="$emit('click')">
    <!-- 作者信息 -->
    <view class="post-card__header">
      <view class="post-card__user" @tap.stop="goUserProfile">
        <UserAvatar :avatar="author?.avatar" :name="authorName" :user-id="author?.id || 0" :size="36" />
        <view class="post-card__user-info">
          <text class="post-card__nickname">{{ authorName }}</text>
          <view class="post-card__meta">
            <text class="post-card__meta-text">{{ timeAgo(post.createdAt) }}</text>
            <text class="post-card__meta-dot">·</text>
            <text class="post-card__meta-text">{{ formatNumber(post.views || 0) }} 浏览</text>
          </view>
        </view>
      </view>
      <view class="post-card__category-tag" :style="{ background: catColor + '18', color: catColor, borderColor: catColor + '40' }">
        {{ categoryLabel }}
      </view>
    </view>

    <!-- 标题 -->
    <text v-if="post.title" class="post-card__title">{{ post.title }}</text>

    <!-- 内容 -->
    <text class="post-card__content">{{ post.content }}</text>

    <!-- 图片 -->
    <view v-if="post.images && post.images.length" class="post-card__images">
      <ImageGrid :images="post.images" :max-show="3" @preview="(idx: number) => $emit('preview', post.images, idx)" />
    </view>

    <!-- 互动栏 -->
    <view class="post-card__footer">
      <view class="post-card__actions">
        <view
          class="post-card__action-btn"
          :class="{ 'is-active': post.isLiked }"
          @tap.stop="$emit('like')"
        >
          <wd-icon :name="post.isLiked ? 'heart-filled' : 'heart'" size="14px" :color="post.isLiked ? '#dc2626' : 'var(--ink-400)'" />
          <text v-if="post.likes" class="post-card__action-count">{{ formatNumber(post.likes) }}</text>
        </view>

        <view class="post-card__action-btn" @tap.stop="$emit('comment')">
          <wd-icon name="chat" size="14px" color="var(--ink-400)" />
          <text v-if="post.comments" class="post-card__action-count">{{ formatNumber(post.comments) }}</text>
        </view>

        <view
          class="post-card__action-btn"
          :class="{ 'is-active': post.isFavorited }"
          @tap.stop="$emit('favorite')"
        >
          <wd-icon :name="post.isFavorited ? 'star-filled' : 'star'" size="14px" :color="post.isFavorited ? '#f59e0b' : 'var(--ink-400)'" />
        </view>
      </view>

      <view class="post-card__share-btn" @tap.stop="$emit('share')">
        <wd-icon name="share" size="16px" color="var(--ink-400)" />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { timeAgo, formatNumber } from '@utils/format'
import ImageGrid from './ImageGrid.vue'
import UserAvatar from '@components/common/UserAvatar.vue'

interface PostAuthor {
  nickname?: string
  avatar?: string
  id?: number
}

interface Post {
  id: number
  title?: string
  content: string
  category?: string
  tags?: string[]
  images?: string[]
  likes?: number
  comments?: number
  views?: number
  createdAt: string
  author?: PostAuthor
  user?: PostAuthor
  isLiked?: boolean
  isFavorited?: boolean
}

const props = defineProps<{
  post: Post
}>()

defineEmits<{
  click: []
  like: []
  comment: []
  favorite: []
  share: []
  preview: [images: string[], index: number]
}>()

const author = computed(() => props.post.author || props.post.user)
const authorName = computed(() => author.value?.nickname || '匿名用户')

const categoryMap: Record<string, { label: string; color: string }> = {
  help: { label: '求助', color: '#f05a28' },
  food: { label: '美食', color: '#ff7a4d' },
  study: { label: '学习', color: '#07C160' },
  life: { label: '生活', color: '#14b88a' },
  other: { label: '其他', color: '#868e96' },
}

const catColor = computed(() => categoryMap[props.post.category || 'other']?.color || '#868e96')
const categoryLabel = computed(() => categoryMap[props.post.category || 'other']?.label || '其他')

function goUserProfile() {
  const userId = author.value?.id
  if (userId) {
    uni.navigateTo({ url: `/pages/user-profile/index?id=${userId}` })
  }
}
</script>

<style scoped>
.post-card {
  padding: 16px;
  background: var(--bg-primary);
  border: 2px solid var(--ink-100);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  transition: all 0.15s ease;
}

.post-card:active {
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

/* ── 头部 ── */
.post-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.post-card__user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.post-card__avatar {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
}

.post-card__avatar-text {
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  font-family: var(--font-display);
}

.post-card__user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.post-card__nickname {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-800);
  line-height: 1.3;
}

.post-card__meta {
  display: flex;
  align-items: center;
  gap: 4px;
}

.post-card__meta-text {
  font-size: 11px;
  color: var(--ink-400);
}

.post-card__meta-dot {
  color: var(--ink-300);
  font-size: 11px;
}

/* ── 分类标签 ── */
.post-card__category-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 600;
  border: 1px solid;
  flex-shrink: 0;
}

/* ── 标题 ── */
.post-card__title {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 700;
  color: var(--ink-900);
  margin-bottom: 8px;
  line-height: 1.35;
  letter-spacing: -0.01em;
}

/* ── 内容 ── */
.post-card__content {
  font-size: 14px;
  color: var(--ink-500);
  line-height: 1.65;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ── 图片 ── */
.post-card__images {
  margin-bottom: 12px;
}

/* ── 互动栏 ── */
.post-card__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid var(--ink-50);
}

.post-card__actions {
  display: flex;
  gap: 4px;
}

.post-card__action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: var(--ink-400);
}

.post-card__action-btn:active {
  background: var(--ink-50);
  transform: scale(0.94);
}
.post-card__action-count {
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 500;
  color: var(--ink-400);
}

.post-card__action-btn.is-active .post-card__action-count {
  color: #dc2626;
}

.post-card__share-btn {
  display: flex;
  align-items: center;
  padding: 6px 6px;
  border-radius: var(--radius-sm);
}

.post-card__share-btn:active {
  background: var(--ink-50);
  transform: scale(0.94);
}
</style>
