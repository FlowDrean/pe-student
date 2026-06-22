<script setup lang="ts">
import { ref, watch, onUnmounted } from 'vue'
import { postApi } from '@api/post'
import { resolveAssetUrl } from '@/http'
import { tradeApi } from '@api/trade'
import { POST_CATEGORIES, TRADE_CATEGORIES } from '@utils/constants'
import { timeAgo, formatNumber, formatPrice } from '@utils/format'

const keyword = ref('')
const activeTab = ref<'post' | 'trade'>('post')
const results = ref<any[]>([])
const loading = ref(false)
const hasSearched = ref(false)
let debounceTimer: ReturnType<typeof setTimeout> | null = null

const categories = POST_CATEGORIES as unknown as Array<{ key: string; label: string; color: string }>
const tradeCategories = TRADE_CATEGORIES as unknown as Array<{ key: string; label: string }>

const hotTags = ['求助', '二手书', '美食推荐', '学习资料', '校园活动', '校园兼职', '拼车出行', '失物招领']

// ── 防抖搜索 ──
watch(keyword, (val) => {
  if (debounceTimer) clearTimeout(debounceTimer)
  if (!val.trim()) {
    results.value = []
    hasSearched.value = false
    return
  }
  debounceTimer = setTimeout(() => {
    doSearch()
  }, 300)
})

async function doSearch() {
  if (!keyword.value.trim()) return
  loading.value = true
  hasSearched.value = true
  try {
    const api = activeTab.value === 'post' ? postApi.search : tradeApi.search
    const res: any = await api({ keyword: keyword.value.trim(), size: 20 })
    results.value = res.list || res.records || []
  } catch {
    results.value = []
  } finally {
    loading.value = false
  }
}

function switchTab(tab: 'post' | 'trade') {
  if (activeTab.value === tab) return
  activeTab.value = tab
  results.value = []
  hasSearched.value = false
  if (keyword.value.trim()) {
    doSearch()
  }
}

function onTagTap(tag: string) {
  keyword.value = tag
}

function onClear() {
  keyword.value = ''
  results.value = []
  hasSearched.value = false
}

function goDetail(id: number) {
  const url = activeTab.value === 'post'
    ? `/pages/post/detail?id=${id}`
    : `/pages/trade/detail?id=${id}`
  uni.navigateTo({ url })
}

function goBack() {
  uni.navigateBack()
}

// ── 辅助函数 ──
function getCategoryLabel(key: string): string {
  const found = categories.find((c) => c.key === key)
  return found?.label || key
}

function getCategoryColor(key: string): string {
  const found = categories.find((c) => c.key === key)
  return found?.color || '#868e96'
}

function truncateText(text: string, maxLen = 100): string {
  if (!text) return ''
  return text.length > maxLen ? text.slice(0, maxLen) + '...' : text
}

function getTradeCategoryLabel(key: string): string {
  const found = tradeCategories.find((c) => c.key === key)
  return found?.label || key
}

onUnmounted(() => {
  if (debounceTimer) clearTimeout(debounceTimer)
})
</script>

<template>
  <view class="search-page">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="search-input-wrap">
        <wd-icon name="search" size="14" color="var(--ink-300)" />
        <input
          v-model="keyword"
          class="search-input"
          type="text"
          placeholder="搜索帖子、商品..."
          placeholder-class="search-placeholder"
          confirm-type="search"
          :focus="true"
          @confirm="doSearch"
        />
        <wd-icon v-if="keyword" name="close" size="14" color="var(--ink-300)" @tap="onClear" />
      </view>
      <text class="search-cancel" @tap="goBack">取消</text>
    </view>

    <!-- Tab 切换 -->
    <view class="search-tabs">
      <view
        class="search-tab"
        :class="{ 'search-tab--active': activeTab === 'post' }"
        @tap="switchTab('post')"
      >
        <text class="search-tab-text">帖子</text>
      </view>
      <view
        class="search-tab"
        :class="{ 'search-tab--active': activeTab === 'trade' }"
        @tap="switchTab('trade')"
      >
        <text class="search-tab-text">商品</text>
      </view>
    </view>

    <!-- 热门搜索 -->
    <view v-if="!keyword.trim()" class="hot-section">
      <text class="hot-title">热门搜索</text>
      <view class="hot-tags">
        <text
          v-for="tag in hotTags"
          :key="tag"
          class="hot-tag"
          @tap="onTagTap(tag)"
        >{{ tag }}</text>
      </view>
    </view>

    <!-- 搜索结果 -->
    <scroll-view
      v-else
      class="results-scroll"
      scroll-y
      :show-scrollbar="false"
    >
      <!-- 加载中 -->
      <view v-if="loading" class="search-loading">
        <text class="search-loading-text">搜索中...</text>
      </view>

      <!-- 帖子结果 -->
      <template v-else-if="activeTab === 'post'">
        <view v-if="results.length" class="results-list">
          <view
            v-for="post in results"
            :key="post.id"
            class="post-card"
            @tap="goDetail(post.id)"
          >
            <view class="post-card-header">
              <image
                class="post-card-avatar"
                :src="post.user?.avatar || '/static/default-avatar.png'"
                mode="aspectFill"
              />
              <view class="post-card-meta">
                <text class="post-card-username">{{ post.user?.nickname || '用户' }}</text>
                <text class="post-card-time">{{ timeAgo(post.createdAt) }}</text>
              </view>
              <view
                v-if="post.category"
                class="post-card-category"
                :style="{ background: getCategoryColor(post.category) }"
              >
                <text class="post-card-category-text">{{ getCategoryLabel(post.category) }}</text>
              </view>
            </view>
            <text class="post-card-title">{{ post.title }}</text>
            <text v-if="post.content" class="post-card-content">{{ truncateText(post.content) }}</text>
            <view class="post-card-footer">
              <view class="post-card-stat">
                <wd-icon name="heart" size="12" color="var(--ink-300)" />
                <text>{{ formatNumber(post.likes) }}</text>
              </view>
              <view class="post-card-stat">
                <wd-icon name="chat" size="12" color="var(--ink-300)" />
                <text>{{ formatNumber(post.comments) }}</text>
              </view>
              <view class="post-card-stat">
                <wd-icon name="browse" size="12" color="var(--ink-300)" />
                <text>{{ formatNumber(post.views) }}</text>
              </view>
            </view>
          </view>
        </view>
        <view v-else-if="hasSearched" class="empty-result">
          <view class="empty-result-icon">
            <wd-icon name="chat" size="48" color="var(--ink-200)" />
          </view>
          <text class="empty-result-title">未找到相关帖子</text>
          <text class="empty-result-desc">换个关键词试试吧</text>
        </view>
      </template>

      <!-- 商品结果 -->
      <template v-else>
        <view v-if="results.length" class="results-list">
          <view
            v-for="trade in results"
            :key="trade.id"
            class="trade-card"
            @tap="goDetail(trade.id)"
          >
            <image
              v-if="trade.images && trade.images.length"
              class="trade-card-image"
              :src="resolveAssetUrl(trade.images[0])"
              mode="aspectFill"
            />
            <view class="trade-card-info">
              <text class="trade-card-title">{{ trade.title }}</text>
              <text v-if="trade.description" class="trade-card-desc">{{ truncateText(trade.description, 60) }}</text>
              <view class="trade-card-bottom">
                <text class="trade-card-price">{{ formatPrice(trade.price) }}</text>
                <text v-if="trade.category" class="trade-card-category">{{ getTradeCategoryLabel(trade.category) }}</text>
              </view>
            </view>
          </view>
        </view>
        <view v-else-if="hasSearched" class="empty-result">
          <view class="empty-result-icon">
            <wd-icon name="goods" size="48" color="var(--ink-200)" />
          </view>
          <text class="empty-result-title">未找到相关商品</text>
          <text class="empty-result-desc">换个关键词试试吧</text>
        </view>
      </template>

      <view class="safe-bottom" />
    </scroll-view>
  </view>
</template>

<style scoped>
.search-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  display: flex;
  flex-direction: column;
}

/* ── 搜索栏 ── */
.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  background: var(--bg-primary);
  border-bottom: 2px solid var(--ink-100);
}

.search-input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  background: var(--bg-tertiary);
  border: 2px solid var(--ink-200);
  border-radius: var(--radius-full);
  padding: 0 14px;
}

.search-input-icon {
  font-size: 14px;
  margin-right: 8px;
  color: var(--ink-300);
}

.search-input {
  flex: 1;
  height: 36px;
  font-size: 14px;
  color: var(--text-primary);
  background: transparent;
  border: none;
  outline: none;
}

.search-placeholder {
  color: var(--ink-300);
  font-size: 14px;
}

.search-clear {
  font-size: 14px;
  color: var(--ink-300);
  padding: 4px;
}

.search-cancel {
  color: var(--accent-500);
  font-size: 15px;
  font-weight: 600;
  flex-shrink: 0;
}

/* ── Tab 切换 ── */
.search-tabs {
  display: flex;
  background: var(--bg-primary);
  border-bottom: 2px solid var(--ink-100);
}

.search-tab {
  flex: 1;
  text-align: center;
  padding: 12px;
  position: relative;
}

.search-tab-text {
  font-size: 15px;
  font-weight: 500;
  color: var(--ink-400);
}

.search-tab--active .search-tab-text {
  color: var(--accent-500);
  font-weight: 700;
}

.search-tab--active::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: -2px;
  transform: translateX(-50%);
  width: 24px;
  height: 3px;
  background: var(--accent-500);
  border-radius: 2px;
}

/* ── 热门搜索 ── */
.hot-section {
  padding: 20px 16px;
}

.hot-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 14px;
  display: block;
}

.hot-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hot-tag {
  padding: 7px 16px;
  background: var(--bg-primary);
  border: 2px solid var(--ink-200);
  border-radius: var(--radius-full);
  font-size: 13px;
  color: var(--ink-500);
  transition: all 0.2s;
}

.hot-tag:active {
  border-color: var(--accent-500);
  color: var(--accent-500);
  background: var(--accent-50);
}

/* ── 搜索结果 ── */
.results-scroll {
  flex: 1;
  padding: 0 16px;
}

.search-loading {
  text-align: center;
  padding: 40px;
}

.search-loading-text {
  font-size: 14px;
  color: var(--ink-300);
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-top: 10px;
}

/* ── 帖子卡片 ── */
.post-card {
  background: var(--bg-primary);
  border: 2px solid var(--ink-200);
  border-radius: var(--radius-lg);
  padding: 14px;
}

.post-card:active {
  opacity: 0.85;
}

.post-card-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.post-card-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  border: 2px solid var(--ink-200);
  flex-shrink: 0;
}

.post-card-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.post-card-username {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.post-card-time {
  font-size: 11px;
  color: var(--ink-300);
}

.post-card-category {
  padding: 2px 10px;
  border-radius: var(--radius-full);
}

.post-card-category-text {
  font-size: 11px;
  color: #fff;
  font-weight: 500;
}

.post-card-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  margin-top: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-card-content {
  font-size: 13px;
  color: var(--ink-400);
  margin-top: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}

.post-card-footer {
  display: flex;
  gap: 16px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 2px solid var(--ink-100);
}

.post-card-stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--ink-300);
}

/* ── 商品卡片 ── */
.trade-card {
  background: var(--bg-primary);
  border: 2px solid var(--ink-200);
  border-radius: var(--radius-lg);
  padding: 12px;
  display: flex;
  gap: 12px;
}

.trade-card:active {
  opacity: 0.85;
}

.trade-card-image {
  width: 100px;
  height: 100px;
  border-radius: var(--radius-md);
  border: 2px solid var(--ink-100);
  flex-shrink: 0;
}

.trade-card-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.trade-card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.trade-card-desc {
  font-size: 12px;
  color: var(--ink-400);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-top: 4px;
}

.trade-card-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
}

.trade-card-price {
  font-size: 16px;
  font-weight: 700;
  color: #f05a28;
  font-family: var(--font-mono);
}

.trade-card-category {
  font-size: 11px;
  color: var(--ink-400);
  padding: 2px 8px;
  background: var(--bg-tertiary);
  border: 2px solid var(--ink-200);
  border-radius: var(--radius-full);
}

/* ── 空结果 ── */
.empty-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 32px;
}

.empty-result-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-result-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--ink-500);
  margin-bottom: 6px;
}

.empty-result-desc {
  font-size: 13px;
  color: var(--ink-300);
}

/* ── 安全区 ── */
.safe-bottom {
  height: 20px;
}
</style>
