<template>
  <article
    class="post-card"
    :data-post-id="post.id"
    @click="$emit('click')"
  >
    <!-- 作者信息 -->
    <div class="post-card__header">
      <div class="post-card__user">
        <div class="avatar avatar-sm" :style="{ background: avatarColor }" @click.stop="goUserProfile">
          <span class="avatar-text">{{ post.user?.nickname?.charAt(0) || '?' }}</span>
        </div>
        <div class="post-card__user-info">
          <span class="post-card__nickname">{{ post.user?.nickname || '匿名用户' }}</span>
          <span class="post-card__meta">
            <span>{{ timeAgo(post.createdAt) }}</span>
            <span class="post-card__dot">·</span>
            <span>{{ formatNumber(post.views) }} 浏览</span>
          </span>
        </div>
      </div>
      <span class="tag tag-primary">{{ categoryLabel }}</span>
    </div>

    <!-- 标题 -->
    <h3 v-if="post.title" class="post-card__title">{{ post.title }}</h3>

    <!-- 内容 -->
    <p class="post-card__content text-clamp-3">{{ post.content }}</p>

    <!-- 图片 -->
    <div v-if="post.images && post.images.length" class="post-card__images">
      <ImageGrid :images="post.images" :max-show="3" @preview="(idx) => $emit('preview', post.images, idx)" />
    </div>

    <!-- 互动栏 -->
    <div class="post-card__footer">
      <div class="post-card__actions">
        <button
          class="post-card__action-btn"
          :class="{ 'is-active': post.isLiked }"
          @click.stop="$emit('like')"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" :fill="post.isLiked ? 'var(--coral-500)' : 'none'" :stroke="post.isLiked ? 'var(--coral-500)' : 'var(--ink-400)'" stroke-width="2">
            <path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/>
          </svg>
          <span v-if="post.likes">{{ formatNumber(post.likes) }}</span>
        </button>

        <button class="post-card__action-btn" @click.stop>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--ink-400)" stroke-width="2">
            <path d="M21 11.5a8.38 8.38 0 01-.9 3.8 8.5 8.5 0 01-7.6 4.7 8.38 8.38 0 01-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 01-.9-3.8 8.5 8.5 0 014.7-7.6 8.38 8.38 0 013.8-.9h.5a8.48 8.48 0 018 8v.5z"/>
          </svg>
          <span v-if="post.comments">{{ formatNumber(post.comments) }}</span>
        </button>

        <button
          class="post-card__action-btn"
          :class="{ 'is-active': post.isFavorited }"
          @click.stop="$emit('favorite')"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" :fill="post.isFavorited ? 'var(--amber-500)' : 'none'" :stroke="post.isFavorited ? 'var(--amber-500)' : 'var(--ink-400)'" stroke-width="2">
            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
          </svg>
        </button>

      </div>

      <button class="post-card__share-btn" @click.stop>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--ink-400)" stroke-width="2">
          <circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/>
          <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
        </svg>
      </button>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { timeAgo, formatNumber } from '@utils/format'
import { POST_CATEGORIES } from '@utils/constants'
import ImageGrid from './ImageGrid.vue'

const props = defineProps({
  post: { type: Object, required: true },
})

const router = useRouter()
defineEmits(['click', 'like', 'favorite', 'preview'])

function goUserProfile() {
  const userId = props.post.user?.id
  if (userId) router.push(`/user-profile/${userId}`)
}

const categoryLabel = computed(() => {
  const cat = POST_CATEGORIES.find((c) => c.key === props.post.category)
  return cat?.label || '其他'
})

const avatarColor = computed(() => {
  const colors = ['#07C160', '#f97316', '#059669', '#f59e0b', '#dc2626', '#7c3aed']
  const idx = (props.post.user?.id || 0) % colors.length
  return colors[idx]
})
</script>

<style scoped>
.post-card {
  padding: var(--space-4);
  background: var(--bg-primary);
  border: 2px solid var(--ink-100);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  transition: all var(--transition-fast);
  cursor: pointer;
}

.post-card:active {
  box-shadow: var(--shadow-card-hover);
  transform: translateY(-1px);
}

/* ── 头部 ── */
.post-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-3);
}

.post-card__user {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.avatar {
  cursor: pointer;
}

.avatar-text {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #fff;
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  font-family: var(--font-display);
  border-radius: var(--radius-full);
}

.post-card__user-info {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.post-card__nickname {
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--ink-800);
  line-height: 1.3;
}

.post-card__meta {
  font-size: var(--text-2xs);
  color: var(--ink-400);
  display: flex;
  align-items: center;
  gap: 2px;
}

.post-card__dot {
  color: var(--ink-300);
}

/* ── 标题 ── */
.post-card__title {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--ink-900);
  margin-bottom: var(--space-2);
  line-height: 1.35;
  letter-spacing: -0.01em;
}

/* ── 内容 ── */
.post-card__content {
  font-size: var(--text-sm);
  color: var(--ink-500);
  line-height: 1.65;
  margin-bottom: var(--space-3);
}

/* ── 图片 ── */
.post-card__images {
  margin-bottom: var(--space-3);
}

/* ── 互动栏 ── */
.post-card__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: var(--space-2);
  border-top: 1px solid var(--ink-50);
}

.post-card__actions {
  display: flex;
  gap: 2px;
}

.post-card__action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 5px 8px;
  border-radius: var(--radius-sm);
  font-family: var(--font-mono);
  font-size: var(--text-3xs);
  font-weight: var(--font-medium);
  color: var(--ink-400);
  transition: all var(--transition-fast);
}

.post-card__action-btn:active {
  background: var(--ink-50);
  transform: scale(0.94);
}

.post-card__action-btn.is-active {
  color: var(--coral-500);
}

.post-card__share-btn {
  display: flex;
  align-items: center;
  padding: 5px 4px;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.post-card__share-btn:active {
  background: var(--ink-50);
  transform: scale(0.94);
}
</style>
