<template>
  <tabbar>
    <view class="trade-page">
    <!-- Category Filter Bar -->
    <view class="trade-page__filter-bar">
      <scroll-view scroll-x class="trade-page__filter-scroll" :show-scrollbar="false">
        <view class="trade-page__filter-inner">
          <view
            class="trade-page__filter-chip"
            :class="{ 'is-active': activeCategory === '' }"
            @tap="onCategory('')"
          >
            <text class="trade-page__filter-chip-text">全部</text>
          </view>
          <view
            v-for="cat in TRADE_CATEGORIES"
            :key="cat.key"
            class="trade-page__filter-chip"
            :class="{ 'is-active': activeCategory === cat.key }"
            @tap="onCategory(cat.key)"
          >
            <text class="trade-page__filter-chip-text">{{ cat.label }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- Loading Skeleton -->
    <view v-if="loading" class="trade-page__skeleton">
      <view v-for="i in 3" :key="i" class="trade-page__skeleton-card">
        <view class="skeleton trade-page__skeleton-img" />
        <view class="skeleton trade-page__skeleton-title" />
        <view class="skeleton trade-page__skeleton-price" />
      </view>
    </view>

    <!-- Trade List -->
    <scroll-view
      v-else-if="trades.length > 0"
      class="trade-page__list"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onPullRefresh"
      @scrolltolower="onLoadMore"
    >
      <TradeCard
        v-for="item in trades"
        :key="item.id"
        :trade="item"
        @click="goDetail(item.id)"
      />

      <!-- Load More -->
      <view v-if="loadingMore" class="trade-page__load-more">
        <Loading variant="spinner" />
      </view>
      <view v-else-if="!hasMore" class="trade-page__load-end">
        <text class="trade-page__load-end-text">—— 没有更多了 ——</text>
      </view>

      <view class="trade-page__safe-bottom" />
    </scroll-view>

    <!-- Empty State -->
    <view v-else class="trade-page__empty-wrap">
      <EmptyState
        title="暂无商品"
        description="市场空空如也，快来发布第一个商品吧"
        action-text="去发布"
        @action="goCreate"
      />
    </view>

    <!-- Error State -->
    <view v-if="error" class="trade-page__error-wrap">
      <ErrorState :title="error" @retry="fetchTrades()" />
    </view>

    <!-- FAB Button -->
    <view class="trade-page__fab" @tap="goCreate">
      <text class="trade-page__fab-icon">+</text>
    </view>
  </view>
  </tabbar>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { tradeApi } from '@api/trade'
import { TRADE_CATEGORIES } from '@utils/constants'
import TradeCard from '@components/trade/TradeCard.vue'
import Loading from '@components/common/Loading.vue'
import EmptyState from '@components/common/EmptyState.vue'
import ErrorState from '@components/common/ErrorState.vue'
import tabbar from '@layouts/tabbar.vue'

const trades = ref<any[]>([])
const loading = ref(true)
const loadingMore = ref(false)
const activeCategory = ref('')
const page = ref(1)
const hasMore = ref(true)
const refreshing = ref(false)
const error = ref('')

async function fetchTrades(append = false) {
  try {
    if (!append) {
      page.value = 1
    }
    const res: any = await tradeApi.getList({
      page: page.value,
      size: 10,
      category: activeCategory.value || undefined,
    })
    const list = res?.list || res?.records || res || []
    if (append) {
      trades.value = [...trades.value, ...list]
    } else {
      trades.value = list
    }
    hasMore.value = list.length >= 10
    error.value = ''
  } catch (e: any) {
    if (!append) {
      error.value = e.message || '加载失败'
    }
  } finally {
    loading.value = false
    refreshing.value = false
    loadingMore.value = false
  }
}

function onPullRefresh() {
  refreshing.value = true
  fetchTrades(false)
}

function onLoadMore() {
  if (!hasMore.value || loadingMore.value) return
  loadingMore.value = true
  page.value++
  fetchTrades(true)
}

function onCategory(key: string) {
  activeCategory.value = key
  loading.value = true
  fetchTrades(false)
}

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/trade/detail?id=${id}` })
}

function goCreate() {
  uni.navigateTo({ url: '/pages/trade/create' })
}

onMounted(() => {
  fetchTrades()
})
</script>

<style scoped>
.trade-page {
  display: flex;
  flex-direction: column;
  flex: 1;
  background: var(--bg-secondary);
}

/* ── Filter Bar ── */
.trade-page__filter-bar {
  background: var(--bg-primary);
  border-bottom: 2px solid var(--ink-100);
  position: sticky;
  top: 0;
  z-index: 100;
}

.trade-page__filter-scroll {
  width: 100%;
}

.trade-page__filter-inner {
  display: flex;
  gap: 8px;
  padding: 10px 16px;
  white-space: nowrap;
}

.trade-page__filter-chip {
  display: inline-flex;
  align-items: center;
  padding: 6px 16px;
  border-radius: var(--radius-full);
  background: var(--bg-primary);
  border: 2px solid var(--ink-200);
  flex-shrink: 0;
  transition: all 0.15s ease;
}

.trade-page__filter-chip:active {
  transform: scale(0.96);
}

.trade-page__filter-chip.is-active {
  background: var(--accent-500);
  border-color: var(--accent-500);
}

.trade-page__filter-chip-text {
  font-size: 13px;
  font-weight: 500;
  color: var(--ink-600);
}

.trade-page__filter-chip.is-active .trade-page__filter-chip-text {
  color: #fff;
  font-weight: 600;
}

/* ── Skeleton ── */
.trade-page__skeleton {
  padding: 12px;
}

.trade-page__skeleton-card {
  background: var(--bg-primary);
  border: 2px solid var(--ink-100);
  border-radius: var(--radius-lg);
  padding: 0 0 12px 0;
  margin-bottom: 12px;
  overflow: hidden;
}

.trade-page__skeleton-img {
  width: 100%;
  aspect-ratio: 16/10;
  border-radius: 0;
  margin-bottom: 12px;
}

.trade-page__skeleton-title {
  width: 60%;
  height: 14px;
  margin: 0 14px 8px;
}

.trade-page__skeleton-price {
  width: 30%;
  height: 14px;
  margin: 0 14px;
}

/* ── List ── */
.trade-page__list {
  flex: 1;
  padding: 12px;
  padding-bottom: 0;
}

.trade-page__load-more {
  display: flex;
  justify-content: center;
  padding: 16px;
}

.trade-page__load-end {
  display: flex;
  justify-content: center;
  padding: 20px;
}

.trade-page__load-end-text {
  font-size: 12px;
  color: var(--ink-300);
}

.trade-page__safe-bottom {
  height: 24px;
}

/* ── Empty / Error ── */
.trade-page__empty-wrap,
.trade-page__error-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ── FAB ── */
.trade-page__fab {
  position: fixed;
  right: 20px;
  bottom: 100px;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: var(--accent-500);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(7, 193, 96, 0.4);
  z-index: 300;
  transition: all 0.2s ease;
}

.trade-page__fab:active {
  transform: scale(0.92);
  box-shadow: 0 2px 8px rgba(7, 193, 96, 0.3);
}

.trade-page__fab-icon {
  font-size: 28px;
  color: #fff;
  font-weight: 300;
  line-height: 1;
}
</style>
