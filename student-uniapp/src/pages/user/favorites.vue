<template>
  <view class="favorites-page">
    <!-- Tab Bar -->
    <view class="favorites-page__tabs">
      <view
        class="favorites-page__tab"
        :class="{ 'favorites-page__tab--active': activeTab === 'post' }"
        @tap="switchTab('post')"
      >
        <text class="favorites-page__tab-text">帖子</text>
        <view v-if="activeTab === 'post'" class="favorites-page__tab-indicator" />
      </view>
      <view
        class="favorites-page__tab"
        :class="{ 'favorites-page__tab--active': activeTab === 'trade' }"
        @tap="switchTab('trade')"
      >
        <text class="favorites-page__tab-text">商品</text>
        <view v-if="activeTab === 'trade'" class="favorites-page__tab-indicator" />
      </view>
    </view>

    <!-- Loading (first load) -->
    <view v-if="currentLoading" class="favorites-page__loading">
      <text class="favorites-page__loading-text">加载中...</text>
    </view>

    <!-- Post Favorites -->
    <scroll-view
      v-show="activeTab === 'post' && !currentLoading"
      class="favorites-page__list"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
    >
      <view v-if="posts.length === 0" class="favorites-page__empty">
        <view class="favorites-page__empty-icon">
          <wd-icon name="star" size="48" color="var(--ink-200)" />
        </view>
        <text class="favorites-page__empty-title">暂无收藏的帖子</text>
      </view>
      <view v-else class="favorites-page__list-items">
        <PostCard
          v-for="item in posts"
          :key="item.id"
          :post="item"
          @click="goPostDetail(item.id)"
        />
      </view>
      <view v-if="postHasMore && posts.length > 0" class="favorites-page__load-more">
        <text class="favorites-page__load-more-text">加载更多...</text>
      </view>
      <view v-if="!postHasMore && posts.length > 0" class="favorites-page__no-more">
        <text class="favorites-page__no-more-text">没有更多了</text>
      </view>
      <view class="favorites-page__safe-bottom" />
    </scroll-view>

    <!-- Trade Favorites -->
    <scroll-view
      v-show="activeTab === 'trade' && !currentLoading"
      class="favorites-page__list"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
    >
      <view v-if="trades.length === 0" class="favorites-page__empty">
        <view class="favorites-page__empty-icon">
          <wd-icon name="star" size="48" color="var(--ink-200)" />
        </view>
        <text class="favorites-page__empty-title">暂无收藏的商品</text>
      </view>
      <view v-else class="favorites-page__list-items">
        <view
          v-for="item in trades"
          :key="item.id"
          class="favorites-page__trade-card card"
          @tap="goTradeDetail(item.id)"
        >
          <view class="favorites-page__trade-header">
            <text class="favorites-page__trade-title">{{ item.title || '无标题' }}</text>
            <PriceTag :price="item.price" :original-price="item.originalPrice" size="sm" />
          </view>
          <text v-if="item.description" class="favorites-page__trade-desc">{{ item.description }}</text>
          <view class="favorites-page__trade-footer">
            <text class="favorites-page__trade-time">{{ timeAgo(item.createdAt) }}</text>
            <view class="favorites-page__trade-status" :style="{ background: tradeStatusColor(item.status) }">
              <text class="favorites-page__trade-status-text">{{ tradeStatusLabel(item.status) }}</text>
            </view>
          </view>
        </view>
      </view>
      <view v-if="tradeHasMore && trades.length > 0" class="favorites-page__load-more">
        <text class="favorites-page__load-more-text">加载更多...</text>
      </view>
      <view v-if="!tradeHasMore && trades.length > 0" class="favorites-page__no-more">
        <text class="favorites-page__no-more-text">没有更多了</text>
      </view>
      <view class="favorites-page__safe-bottom" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { userApi } from '@api/user'
import PostCard from '@components/post/PostCard.vue'
import PriceTag from '@components/trade/PriceTag.vue'
import { timeAgo } from '@utils/format'
import { SIZE_DEFAULT } from '@utils/constants'

interface FavoritedPost {
  id: number
  title?: string
  content: string
  category?: string
  images?: string[]
  likes?: number
  comments?: number
  views?: number
  createdAt: string
  [key: string]: any
}

interface FavoritedTrade {
  id: number
  title?: string
  description?: string
  price: number
  originalPrice?: number
  status: number
  createdAt: string
  [key: string]: any
}

const activeTab = ref<'post' | 'trade'>('post')
const posts = ref<FavoritedPost[]>([])
const trades = ref<FavoritedTrade[]>([])
const loading = ref(true)
const isRefreshing = ref(false)
const postPage = ref(1)
const tradePage = ref(1)
const postHasMore = ref(true)
const tradeHasMore = ref(true)
const postLoaded = ref(false)
const tradeLoaded = ref(false)

const currentLoading = computed(() => {
  if (activeTab.value === 'post') return !postLoaded.value
  return !tradeLoaded.value
})

const TRADE_STATUS_LABELS: Record<number, string> = {
  0: '已下架',
  1: '在售',
  2: '已售',
  3: '交易中',
}

function tradeStatusLabel(status: number): string {
  return TRADE_STATUS_LABELS[status] || '未知'
}

function tradeStatusColor(status: number): string {
  const colors: Record<number, string> = {
    0: 'var(--ink-300)',
    1: 'var(--accent-500)',
    2: 'var(--error)',
    3: 'var(--warning)',
  }
  return colors[status] || 'var(--ink-300)'
}

onMounted(() => {
  fetchFavorites('post')
})

function switchTab(tab: 'post' | 'trade') {
  activeTab.value = tab
  if (tab === 'post' && !postLoaded.value) {
    fetchFavorites('post')
  } else if (tab === 'trade' && !tradeLoaded.value) {
    fetchFavorites('trade')
  }
}

async function fetchFavorites(type: 'post' | 'trade', reset = false) {
  if (type === 'post') {
    if (reset) postPage.value = 1
  } else {
    if (reset) tradePage.value = 1
  }

  try {
    const pageNum = type === 'post' ? postPage.value : tradePage.value
    const data = await userApi.getFavorites({ type, page: pageNum, size: SIZE_DEFAULT })
    const list = Array.isArray(data) ? data : (data?.list || data?.records || [])

    if (type === 'post') {
      if (reset) {
        posts.value = list
      } else {
        posts.value = [...posts.value, ...list]
      }
      postHasMore.value = list.length >= SIZE_DEFAULT
      postLoaded.value = true
    } else {
      if (reset) {
        trades.value = list
      } else {
        trades.value = [...trades.value, ...list]
      }
      tradeHasMore.value = list.length >= SIZE_DEFAULT
      tradeLoaded.value = true
    }
  } catch {
    if (type === 'post') postLoaded.value = true
    else tradeLoaded.value = true
  } finally {
    loading.value = false
    isRefreshing.value = false
  }
}

function onRefresh() {
  isRefreshing.value = true
  fetchFavorites(activeTab.value, true)
}

function onLoadMore() {
  const hasMore = activeTab.value === 'post' ? postHasMore.value : tradeHasMore.value
  if (!hasMore || isRefreshing.value) return
  if (activeTab.value === 'post') {
    postPage.value++
  } else {
    tradePage.value++
  }
  fetchFavorites(activeTab.value)
}

function goPostDetail(id: number) {
  uni.navigateTo({ url: `/pages/post/detail?id=${id}` })
}

function goTradeDetail(id: number) {
  uni.navigateTo({ url: `/pages/trade/detail?id=${id}` })
}
</script>

<style scoped>
.favorites-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  display: flex;
  flex-direction: column;
}

/* ── Tabs ── */
.favorites-page__tabs {
  display: flex;
  background: var(--bg-primary);
  border-bottom: 2px solid var(--ink-100);
  position: sticky;
  top: 0;
  z-index: 10;
}

.favorites-page__tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 14px 0;
  position: relative;
}

.favorites-page__tab:active {
  background: var(--ink-50);
}

.favorites-page__tab-text {
  font-size: 15px;
  font-weight: 500;
  color: var(--ink-400);
  transition: color 0.15s ease;
}

.favorites-page__tab--active .favorites-page__tab-text {
  color: var(--accent-500);
  font-weight: 700;
}

.favorites-page__tab-indicator {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 32px;
  height: 3px;
  background: var(--accent-500);
  border-radius: 2px 2px 0 0;
}

/* ── Loading ── */
.favorites-page__loading {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.favorites-page__loading-text {
  font-size: 15px;
  color: var(--ink-300);
}

/* ── List ── */
.favorites-page__list {
  flex: 1;
  padding: 16px;
}

.favorites-page__list-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* ── Empty ── */
.favorites-page__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
}

.favorites-page__empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.favorites-page__empty-title {
  font-size: 15px;
  color: var(--ink-300);
}

.favorites-page__load-more,
.favorites-page__no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px 0;
}

.favorites-page__load-more-text,
.favorites-page__no-more-text {
  font-size: 13px;
  color: var(--ink-300);
}

.favorites-page__safe-bottom {
  height: 40px;
}

/* ── Trade Card (inline) ── */
.favorites-page__trade-card {
  padding: 16px;
}

.favorites-page__trade-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.favorites-page__trade-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--ink-800);
  flex: 1;
  font-family: var(--font-display);
}

.favorites-page__trade-desc {
  font-size: 14px;
  color: var(--ink-400);
  line-height: 1.5;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
}

.favorites-page__trade-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.favorites-page__trade-time {
  font-size: 11px;
  color: var(--ink-300);
}

.favorites-page__trade-status {
  padding: 2px 10px;
  border-radius: var(--radius-full);
  color: #fff;
}

.favorites-page__trade-status-text {
  font-size: 11px;
  font-weight: 600;
}
</style>
