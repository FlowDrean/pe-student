<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { postApi } from '@api/post'
import { resolveAssetUrl } from '@/http'
import { POST_CATEGORIES } from '@utils/constants'
import { timeAgo, formatNumber } from '@utils/format'

const categories = POST_CATEGORIES as unknown as Array<{ key: string; label: string; color: string }>

const categoryKey = ref('')
const categoryLabel = ref('分类')
const categoryColor = ref('#868e96')
const posts = ref<any[]>([])
const page = ref(1)
const hasMore = ref(true)
const initialLoading = ref(true)
const refreshing = ref(false)
const loadingMore = ref(false)

// ── 接收页面参数 ──
onLoad((options: any) => {
  if (options?.type) {
    categoryKey.value = options.type
    const found = categories.find((c) => c.key === options.type)
    if (found) {
      categoryLabel.value = found.label
      categoryColor.value = found.color
    }
    uni.setNavigationBarTitle({ title: categoryLabel.value })
  }
})

// ── 获取帖子 ──
async function fetchPosts(append = false) {
  if (!append) {
    page.value = 1
  }
  try {
    const res: any = await postApi.getList({
      page: page.value,
      size: 10,
      category: categoryKey.value,
    })
    const list = res.list || res.records || []
    if (append) {
      posts.value = [...posts.value, ...list]
    } else {
      posts.value = list
    }
    hasMore.value = list.length >= 10
    if (append && list.length > 0) {
      page.value++
    }
  } catch {
    // handled by http layer
  }
}

// ── 下拉刷新 ──
async function onRefresh() {
  refreshing.value = true
  page.value = 1
  hasMore.value = true
  await fetchPosts(false)
  refreshing.value = false
}

// ── 加载更多 ──
async function onLoadMore() {
  if (!hasMore.value || loadingMore.value) return
  loadingMore.value = true
  const nextPage = page.value + 1
  try {
    const res: any = await postApi.getList({
      page: nextPage,
      size: 10,
      category: categoryKey.value,
    })
    const list = res.list || res.records || []
    if (list.length > 0) {
      posts.value = [...posts.value, ...list]
      page.value = nextPage
    }
    hasMore.value = list.length >= 10
  } catch {
    // handled by http layer
  } finally {
    loadingMore.value = false
  }
}

// ── 跳转详情 ──
function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/post/detail?id=${id}` })
}

// ── 辅助函数 ──
function getCategoryColor(key: string): string {
  const found = categories.find((c) => c.key === key)
  return found?.color || '#868e96'
}

function getCategoryLabel(key: string): string {
  const found = categories.find((c) => c.key === key)
  return found?.label || key
}

function truncateContent(content: string, maxLen = 120): string {
  if (!content) return ''
  return content.length > maxLen ? content.slice(0, maxLen) + '...' : content
}

// ── 初始化 ──
onMounted(async () => {
  if (categoryKey.value) {
    await fetchPosts(false)
  }
  initialLoading.value = false
})
</script>

<template>
  <view class="category-page">
    <!-- 分类标题栏 -->
    <view class="category-header" :style="{ borderColor: categoryColor }">
      <view class="category-header-dot" :style="{ background: categoryColor }" />
      <text class="category-header-title">{{ categoryLabel }}</text>
      <text v-if="posts.length" class="category-header-count">{{ posts.length }} 篇帖子</text>
    </view>

    <!-- 帖子列表 -->
    <scroll-view
      class="feed-scroll"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
      :show-scrollbar="false"
    >
      <!-- 骨架屏 -->
      <view v-if="initialLoading" class="skeleton-list">
        <view v-for="i in 3" :key="i" class="skeleton-card">
          <view class="skeleton-line skeleton-line--short" />
          <view class="skeleton-line skeleton-line--long" />
          <view class="skeleton-line" />
          <view class="skeleton-line skeleton-line--medium" />
        </view>
      </view>

      <!-- 帖子列表 -->
      <template v-else-if="posts.length">
        <view
          v-for="post in posts"
          :key="post.id"
          class="post-card"
          @tap="goDetail(post.id)"
        >
          <!-- 头部 -->
          <view class="post-header">
            <image
              class="post-avatar"
              :src="post.user?.avatar || '/static/default-avatar.png'"
              mode="aspectFill"
            />
            <view class="post-header-info">
              <text class="post-username">{{ post.user?.nickname || '用户' }}</text>
              <text class="post-time">{{ timeAgo(post.createdAt) }}</text>
            </view>
            <view
              v-if="post.category"
              class="post-category-tag"
              :style="{ background: getCategoryColor(post.category) }"
            >
              <text class="post-category-text">{{ getCategoryLabel(post.category) }}</text>
            </view>
          </view>

          <text class="post-title-text">{{ post.title }}</text>

          <text v-if="post.content" class="post-content-preview">{{ truncateContent(post.content) }}</text>

          <!-- 图片网格 -->
          <view v-if="post.images && post.images.length" class="post-images">
            <image
              v-for="(img, idx) in post.images.slice(0, 3)"
              :key="idx"
              class="post-image"
              :class="{ 'post-image--single': post.images.length === 1 }"
              :src="resolveAssetUrl(img)"
              mode="aspectFill"
            />
            <view v-if="post.images.length > 3" class="post-image post-image--more">
              <image class="post-image" :src="resolveAssetUrl(post.images[3])" mode="aspectFill" />
              <view class="image-more-overlay">
                <text class="image-more-text">+{{ post.images.length - 3 }}</text>
              </view>
            </view>
          </view>

          <!-- 底部统计 -->
          <view class="post-footer">
            <view class="post-stat">
              <text class="stat-icon">&#9825;</text>
              <text class="stat-num">{{ formatNumber(post.likes) }}</text>
            </view>
            <view class="post-stat">
              <text class="stat-icon">&#9993;</text>
              <text class="stat-num">{{ formatNumber(post.comments) }}</text>
            </view>
            <view class="post-stat">
              <text class="stat-icon">&#128065;</text>
              <text class="stat-num">{{ formatNumber(post.views) }}</text>
            </view>
          </view>
        </view>

        <!-- 加载更多 -->
        <view v-if="loadingMore" class="load-more">
          <text class="load-more-text">加载中...</text>
        </view>
        <view v-else-if="!hasMore && posts.length > 0" class="feed-end">
          <text class="feed-end-text">— 没有更多了 —</text>
        </view>
      </template>

      <!-- 空状态 -->
      <view v-else class="empty-state">
        <text class="empty-icon">&#128488;</text>
        <text class="empty-title">暂无帖子</text>
        <text class="empty-desc">该分类下还没有帖子</text>
      </view>

      <view class="safe-bottom" />
    </scroll-view>
  </view>
</template>

<style scoped>
.category-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  display: flex;
  flex-direction: column;
}

.category-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px;
  background: var(--bg-primary);
  border-bottom: 2px solid var(--ink-200);
}

.category-header-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.category-header-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-primary);
}

.category-header-count {
  font-size: 12px;
  color: var(--ink-300);
  margin-left: auto;
}

.feed-scroll {
  flex: 1;
  padding: 0 16px;
}

/* ── 骨架屏 ── */
.skeleton-list {
  padding-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skeleton-card {
  background: var(--bg-primary);
  border: 2px solid var(--ink-100);
  border-radius: var(--radius-lg);
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.skeleton-line {
  height: 14px;
  border-radius: var(--radius-sm);
  background: linear-gradient(90deg, var(--ink-100) 25%, var(--ink-50) 50%, var(--ink-100) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
}

.skeleton-line--short {
  width: 30%;
  height: 12px;
}

.skeleton-line--long {
  width: 80%;
  height: 18px;
}

.skeleton-line--medium {
  width: 60%;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ── 帖子卡片 ── */
.post-card {
  background: var(--bg-primary);
  border: 2px solid var(--ink-200);
  border-radius: var(--radius-lg);
  padding: 14px;
  margin-top: 10px;
}

.post-card:active {
  opacity: 0.85;
}

.post-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.post-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid var(--ink-200);
  flex-shrink: 0;
}

.post-header-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.post-username {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.post-time {
  font-size: 11px;
  color: var(--ink-300);
}

.post-category-tag {
  padding: 2px 10px;
  border-radius: var(--radius-full);
  flex-shrink: 0;
}

.post-category-text {
  font-size: 11px;
  color: #fff;
  font-weight: 500;
}

.post-title-text {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin-top: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-content-preview {
  font-size: 13px;
  color: var(--ink-400);
  margin-top: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}

.post-images {
  display: flex;
  gap: 4px;
  margin-top: 10px;
  overflow: hidden;
  border-radius: var(--radius-md);
}

.post-image {
  width: calc((100% - 8px) / 3);
  aspect-ratio: 1;
  object-fit: cover;
  border: 2px solid var(--ink-100);
  border-radius: var(--radius-sm);
}

.post-image--single {
  width: 100%;
  aspect-ratio: 16 / 10;
}

.post-image--more {
  position: relative;
}

.image-more-overlay {
  position: absolute;
  inset: 0;
  background: rgba(15, 17, 25, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
}

.image-more-text {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
}

.post-footer {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-top: 12px;
  padding-top: 10px;
  border-top: 2px solid var(--ink-100);
}

.post-stat {
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-icon {
  font-size: 12px;
  color: var(--ink-300);
}

.stat-num {
  font-size: 12px;
  color: var(--ink-300);
  font-family: var(--font-mono);
}

.load-more {
  text-align: center;
  padding: 16px;
}

.load-more-text {
  font-size: 13px;
  color: var(--ink-300);
}

.feed-end {
  text-align: center;
  padding: 20px;
}

.feed-end-text {
  font-size: 13px;
  color: var(--ink-300);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 32px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--ink-500);
  margin-bottom: 6px;
}

.empty-desc {
  font-size: 13px;
  color: var(--ink-300);
}

.safe-bottom {
  height: 20px;
}
</style>
