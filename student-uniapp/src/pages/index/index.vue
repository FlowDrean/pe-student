<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { postApi } from '@api/post'
import { resolveAssetUrl } from '@/http'
import { usePostStore, type Post } from '@stores/post'
import { POST_CATEGORIES } from '@utils/constants'
import { timeAgo, formatNumber } from '@utils/format'
import tabbar from '@layouts/tabbar.vue'

const postStore = usePostStore()

// ── 状态 ──
const initialLoading = ref(true)
const refreshing = ref(false)
const loadingMore = ref(false)
const activeCategory = ref('')
const page = ref(1)
const hasMore = ref(true)
const bannerPosts = ref<Post[]>([])

const categories = POST_CATEGORIES as unknown as Array<{ key: string; label: string; color: string }>

function getCategoryLabel(key: string): string {
  const found = categories.find((c) => c.key === key)
  return found?.label || key
}

function getCategoryColor(key: string): string {
  const found = categories.find((c) => c.key === key)
  return found?.color || '#868e96'
}

function truncateContent(content: string, maxLen = 120): string {
  if (!content) return ''
  return content.length > maxLen ? content.slice(0, maxLen) + '...' : content
}

function getImageClass(total: number, index: number): string {
  if (total === 1) return 'post-image--first-single'
  return ''
}

function getInitial(post: Post): string {
  return (post.user?.nickname || '用').charAt(0)
}

function getAvatarColor(post: Post): string {
  const colors = ['#07C160', '#f97316', '#059669', '#f59e0b', '#dc2626', '#7c3aed']
  const idx = (post.id || 0) % colors.length
  return colors[idx]
}

// ── 获取热门帖子（Banner） ──
async function fetchBannerPosts() {
  try {
    const res: any = await postApi.getHotPosts({ size: 5 })
    bannerPosts.value = (res.list || res.records || res || []).filter((p: any) => p.images && p.images.length)
  } catch {
    // silence
  }
}

// ── 获取帖子列表 ──
async function fetchPosts(append = false) {
  if (!append) {
    page.value = 1
  }
  try {
    const res: any = await postApi.getList({
      page: page.value,
      size: 10,
      category: activeCategory.value || undefined,
    })
    const list: Post[] = res.list || res.records || []
    postStore.setPosts(list, append)
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
  await Promise.all([fetchBannerPosts(), fetchPosts(false)])
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
      category: activeCategory.value || undefined,
    })
    const list: Post[] = res.list || res.records || []
    if (list.length > 0) {
      postStore.setPosts(list, true)
      page.value = nextPage
    }
    hasMore.value = list.length >= 10
  } catch {
    // handled by http layer
  } finally {
    loadingMore.value = false
  }
}

// ── 分类切换 ──
async function onCategoryChange(key: string) {
  if (activeCategory.value === key) {
    activeCategory.value = ''
  } else {
    activeCategory.value = key
  }
  postStore.reset()
  page.value = 1
  hasMore.value = true
  initialLoading.value = true
  await fetchPosts(false)
  initialLoading.value = false
}

// ── 跳转 ──
function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/post/detail?id=${id}` })
}

function goSearch() {
  uni.navigateTo({ url: '/pages/search/index' })
}

// 图片加载失败处理（小程序端安全）

// ── 点赞/收藏 ──
async function handleLike(post: Post) {
  try {
    await postApi.toggleLike(post.id)
    postStore.toggleLike(post.id)
  } catch { /* handled */ }
}

async function handleFavorite(post: Post) {
  try {
    await postApi.toggleFavorite(post.id)
    postStore.toggleFavorite(post.id)
    uni.showToast({
      title: post.isFavorited ? '已取消收藏' : '已收藏',
      icon: 'none',
    })
  } catch { /* handled */ }
}

// ── 生命周期 ──
onMounted(async () => {
  await Promise.all([fetchBannerPosts(), fetchPosts(false)])
  initialLoading.value = false
})
</script>

<template>
  <tabbar>
    <view class="home-page">
    <!-- 搜索栏 -->
    <view class="search-bar" @tap="goSearch">
      <wd-icon name="search" size="15" color="var(--ink-300)" />
      <text class="search-placeholder">搜索帖子、商品...</text>
    </view>

    <!-- Banner 轮播 -->
    <view v-if="bannerPosts.length" class="banner-section">
      <swiper
        class="banner-swiper"
        :autoplay="true"
        :interval="4000"
        :duration="500"
        :circular="true"
        indicator-dots
        indicator-color="rgba(255,255,255,0.4)"
        indicator-active-color="#ffffff"
      >
        <swiper-item
          v-for="item in bannerPosts"
          :key="item.id"
          @tap="goDetail(item.id)"
        >
          <view class="banner-slide">
            <image
              v-if="item.images && item.images.length"
              class="banner-image"
              :src="resolveAssetUrl(item.images[0])"
              mode="aspectFill"
            />
            <view class="banner-overlay" />
            <view class="banner-info">
              <text class="banner-title">{{ item.title }}</text>
              <view class="banner-meta">
                <text class="banner-author">{{ item.user?.nickname || '用户' }}</text>
                <text class="banner-views">{{ formatNumber(item.views) }} 浏览</text>
              </view>
            </view>
          </view>
        </swiper-item>
      </swiper>
    </view>

    <!-- 分类 Tab -->
    <scroll-view class="category-tabs" scroll-x enhanced :show-scrollbar="false">
      <view class="category-tabs-inner">
        <view
          v-for="cat in categories"
          :key="cat.key"
          class="cat-chip"
          :class="{ 'cat-chip--active': activeCategory === cat.key }"
          :style="activeCategory === cat.key ? { background: cat.color, borderColor: cat.color } : {}"
          @tap="onCategoryChange(cat.key)"
        >
          <text class="cat-chip-text">{{ cat.label }}</text>
        </view>
      </view>
    </scroll-view>

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
      <!-- 初始加载骨架屏 -->
      <view v-if="initialLoading" class="skeleton-list">
        <view v-for="i in 3" :key="i" class="skeleton-card">
          <view class="skeleton-line skeleton-line--short" />
          <view class="skeleton-line skeleton-line--long" />
          <view class="skeleton-line" />
          <view class="skeleton-line skeleton-line--medium" />
        </view>
      </view>

      <!-- 帖子列表 -->
      <template v-else-if="postStore.posts.length">
        <view
          v-for="post in postStore.posts"
          :key="post.id"
          class="post-card"
          @tap="goDetail(post.id)"
        >
          <!-- 头部 -->
          <view class="post-header">
            <view class="post-avatar" :style="{ background: getAvatarColor(post) }">
              <text class="post-avatar-text">{{ getInitial(post) }}</text>
            </view>
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

          <!-- 标题 -->
          <text class="post-title">{{ post.title }}</text>

          <!-- 内容预览 -->
          <text v-if="post.content" class="post-content-preview">{{ truncateContent(post.content) }}</text>

          <!-- 图片 -->
          <view v-if="post.images && post.images.length" class="post-images">
            <image
              v-for="(img, idx) in post.images.slice(0, 3)"
              :key="idx"
              class="post-image"
              :class="getImageClass(post.images.length, idx)"
              :src="resolveAssetUrl(img)"
              mode="aspectFill"
            />
            <view
              v-if="post.images.length > 3"
              class="post-image post-image--more"
            >
              <image
                class="post-image"
                :src="resolveAssetUrl(post.images[3])"
                mode="aspectFill"
              />
              <view class="image-more-overlay">
                <text class="image-more-text">+{{ post.images.length - 3 }}</text>
              </view>
            </view>
          </view>

          <!-- 底部操作栏 -->
          <view class="post-footer">
            <view class="post-stat">
              <wd-icon name="heart" size="12" color="var(--ink-300)" />
              <text class="stat-num">{{ formatNumber(post.likes) }}</text>
            </view>
            <view class="post-stat">
              <wd-icon name="chat" size="12" color="var(--ink-300)" />
              <text class="stat-num">{{ formatNumber(post.comments) }}</text>
            </view>
            <view class="post-stat">
              <wd-icon name="browse" size="12" color="var(--ink-300)" />
              <text class="stat-num">{{ formatNumber(post.views) }}</text>
            </view>
            <view class="post-actions">
              <view class="action-btn" :class="{ 'action-btn--active': post.isLiked }" @tap.stop="handleLike(post)">
                <wd-icon :name="post.isLiked ? 'heart-filled' : 'heart'" size="16" :color="post.isLiked ? 'var(--accent-500)' : 'var(--ink-300)'" />
              </view>
              <view class="action-btn" :class="{ 'action-btn--active': post.isFavorited }" @tap.stop="handleFavorite(post)">
                <wd-icon :name="post.isFavorited ? 'star-filled' : 'star'" size="16" :color="post.isFavorited ? 'var(--accent-500)' : 'var(--ink-300)'" />
              </view>
            </view>
          </view>
        </view>

        <!-- 加载更多 -->
        <view v-if="loadingMore" class="load-more">
          <text class="load-more-text">加载中...</text>
        </view>
        <view v-else-if="!hasMore" class="feed-end">
          <text class="feed-end-text">— 没有更多了 —</text>
        </view>
      </template>

      <!-- 空状态 -->
      <view v-else class="empty-state">
        <view class="empty-icon">
          <wd-icon name="chat" size="48" color="var(--ink-200)" />
        </view>
        <text class="empty-title">暂无帖子</text>
        <text class="empty-desc">快去发布第一条帖子吧</text>
      </view>

      <!-- 底部安全区 -->
      <view class="safe-bottom" />
    </scroll-view>
  </view>
  </tabbar>
</template>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  flex: 1;
  background: var(--bg-secondary);
}

/* ── 搜索栏 ── */
.search-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 8px 16px 4px;
  padding: 10px 16px;
  background: var(--bg-primary);
  border: 2px solid var(--ink-200);
  border-radius: var(--radius-full);
}

.search-icon {
  font-size: 15px;
}

.search-placeholder {
  color: var(--ink-300);
  font-size: 14px;
}

/* ── Banner ── */
.banner-section {
  margin: 8px 16px 4px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 2px solid var(--ink-200);
}

.banner-swiper {
  width: 100%;
  height: 150px;
}

.banner-slide {
  position: relative;
  width: 100%;
  height: 100%;
}

.banner-image {
  width: 100%;
  height: 100%;
}

.banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(15, 17, 25, 0.8), rgba(15, 17, 25, 0.15));
}

.banner-info {
  position: absolute;
  left: 16px;
  right: 16px;
  bottom: 14px;
}

.banner-title {
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.banner-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
}

.banner-author {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.banner-views {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
}

/* ── 分类 Tab ── */
.category-tabs {
  padding: 8px 0 4px;
}

.category-tabs-inner {
  display: flex;
  gap: 8px;
  padding: 0 16px;
  white-space: nowrap;
}

.cat-chip {
  display: inline-flex;
  align-items: center;
  padding: 6px 16px;
  border-radius: var(--radius-full);
  background: var(--bg-primary);
  border: 2px solid var(--ink-200);
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  flex-shrink: 0;
  transition: all 0.2s;
}

.cat-chip--active {
  color: #fff;
}

.cat-chip-text {
  font-size: 13px;
}

/* ── 滚动列表 ── */
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

/* 头部 */
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
  display: flex;
  align-items: center;
  justify-content: center;
}

.post-avatar-text {
  color: #fff;
  font-size: 14px;
  font-weight: 700;
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
  color: var(--ink-800);
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

/* 标题 */
.post-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--ink-900);
  margin-top: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 内容 */
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

/* 图片网格 */
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

.post-image--first-single {
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

/* 底部 */
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

.post-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: auto;
}

.action-btn {
  font-size: 16px;
  color: var(--ink-300);
  padding: 2px;
}

.action-btn--active {
  color: var(--accent-500);
}

/* ── 加载更多 ── */
.load-more {
  text-align: center;
  padding: 16px;
}

.load-more-text {
  font-size: 13px;
  color: var(--ink-300);
}

/* ── 没有更多 ── */
.feed-end {
  text-align: center;
  padding: 20px;
}

.feed-end-text {
  font-size: 13px;
  color: var(--ink-300);
}

/* ── 空状态 ── */
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

/* ── 安全区 ── */
.safe-bottom {
  height: 20px;
}
</style>
