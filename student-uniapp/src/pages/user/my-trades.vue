<template>
  <view class="my-trades-page">
    <!-- Loading -->
    <view v-if="loading" class="my-trades-page__skeleton">
      <view v-for="i in 3" :key="i" class="my-trades-page__skeleton-item">
        <view class="skeleton my-trades-page__skeleton-line" style="width:60%" />
        <view class="skeleton my-trades-page__skeleton-line" style="width:100%" />
        <view class="skeleton my-trades-page__skeleton-line" style="width:40%" />
      </view>
    </view>

    <!-- Trade List -->
    <scroll-view
      v-else-if="trades.length > 0"
      class="my-trades-page__list"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
    >
      <view class="my-trades-page__list-items">
        <view
          v-for="trade in trades"
          :key="trade.id"
          class="my-trades-page__trade-card card"
          @tap="goDetail(trade.id)"
          @longpress="showActions(trade)"
        >
          <view class="my-trades-page__trade-header">
            <text class="my-trades-page__trade-title">{{ trade.title || '无标题' }}</text>
            <view class="my-trades-page__trade-status" :style="{ background: statusColor(trade.status), color: '#fff' }">
              <text class="my-trades-page__trade-status-text">{{ statusLabel(trade.status) }}</text>
            </view>
          </view>
          <text v-if="trade.description" class="my-trades-page__trade-desc">{{ trade.description }}</text>
          <view class="my-trades-page__trade-footer">
            <PriceTag :price="trade.price" :original-price="trade.originalPrice" size="sm" />
            <text class="my-trades-page__trade-time">{{ timeAgo(trade.createdAt) }}</text>
          </view>
        </view>
      </view>
      <view v-if="hasMore" class="my-trades-page__load-more">
        <text class="my-trades-page__load-more-text">加载更多...</text>
      </view>
      <view v-else class="my-trades-page__no-more">
        <text class="my-trades-page__no-more-text">没有更多了</text>
      </view>
      <view class="my-trades-page__safe-bottom" />
    </scroll-view>

    <!-- Empty -->
    <view v-else class="my-trades-page__empty">
      <view class="my-trades-page__empty-icon">
        <wd-icon name="cart" size="48" color="var(--ink-200)" />
      </view>
      <text class="my-trades-page__empty-title">暂无商品</text>
      <text class="my-trades-page__empty-desc">快去发布你的第一个二手商品吧</text>
      <view class="my-trades-page__empty-btn" @tap="goCreate">
        <text class="my-trades-page__empty-btn-text">发布商品</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { userApi } from '@api/user'
import { tradeApi } from '@api/trade'
import { useUserStore } from '@stores/user'
import PriceTag from '@components/trade/PriceTag.vue'
import { timeAgo } from '@utils/format'
import { SIZE_DEFAULT } from '@utils/constants'

const userStore = useUserStore()

const trades = ref<any[]>([])
const loading = ref(true)
const isRefreshing = ref(false)
const page = ref(1)
const hasMore = ref(true)

const TRADE_STATUS_LABELS: Record<number, string> = {
  0: '已下架',
  1: '在售',
  2: '已售',
  3: '交易中',
}

function statusLabel(status: number): string {
  return TRADE_STATUS_LABELS[status] || '未知'
}

function statusColor(status: number): string {
  const colors: Record<number, string> = {
    0: 'var(--ink-300)',
    1: 'var(--accent-500)',
    2: 'var(--error)',
    3: 'var(--warning)',
  }
  return colors[status] || 'var(--ink-300)'
}

onMounted(() => {
  fetchTrades()
})

async function fetchTrades(reset = false) {
  if (reset) page.value = 1
  try {
    const userId = userStore.userId
    if (!userId) {
      trades.value = []
      loading.value = false
      return
    }
    const data = await userApi.getUserTrades(userId, { page: page.value, size: SIZE_DEFAULT })
    const list = Array.isArray(data) ? data : (data?.list || data?.records || [])
    if (reset) {
      trades.value = list
    } else {
      trades.value = [...trades.value, ...list]
    }
    hasMore.value = list.length >= SIZE_DEFAULT
  } catch {
    // handled
  } finally {
    loading.value = false
    isRefreshing.value = false
  }
}

function onRefresh() {
  isRefreshing.value = true
  fetchTrades(true)
}

function onLoadMore() {
  if (!hasMore.value || isRefreshing.value) return
  page.value++
  fetchTrades()
}

function showActions(trade: any) {
  const actions = ['查看详情']
  if (trade.status === 1) actions.push('下架商品')
  actions.push('删除商品')

  uni.showActionSheet({
    itemList: actions,
    success: (res) => {
      if (res.tapIndex === 0) {
        goDetail(trade.id)
      } else if (res.tapIndex === 1 && trade.status === 1) {
        offlineTrade(trade)
      } else {
        confirmDelete(trade)
      }
    },
  })
}

async function offlineTrade(trade: any) {
  try {
    await tradeApi.update(trade.id, { status: 0 })
    trade.status = 0
    uni.showToast({ title: '已下架', icon: 'success' })
  } catch {
    // handled
  }
}

function confirmDelete(trade: any) {
  uni.showModal({
    title: '确认删除',
    content: '删除后无法恢复，确定要删除吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await tradeApi.delete(trade.id)
          trades.value = trades.value.filter((t) => t.id !== trade.id)
          uni.showToast({ title: '已删除', icon: 'success' })
        } catch {
          // handled
        }
      }
    },
  })
}

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/trade/detail?id=${id}` })
}

function goCreate() {
  uni.navigateTo({ url: '/pages/trade/create' })
}
</script>

<style scoped>
.my-trades-page {
  min-height: 100vh;
  background: var(--bg-secondary);
}

/* ── Skeleton ── */
.my-trades-page__skeleton {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.my-trades-page__skeleton-item {
  padding: 16px;
  background: var(--bg-primary);
  border: 2px solid var(--ink-100);
  border-radius: var(--radius-lg);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.my-trades-page__skeleton-line {
  height: 14px;
}

/* ── List ── */
.my-trades-page__list {
  height: 100vh;
  padding: 16px;
}

.my-trades-page__list-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* ── Trade Card ── */
.my-trades-page__trade-card {
  padding: 16px;
}

.my-trades-page__trade-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.my-trades-page__trade-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--ink-800);
  flex: 1;
  font-family: var(--font-display);
}

.my-trades-page__trade-status {
  padding: 2px 10px;
  border-radius: var(--radius-full);
  flex-shrink: 0;
  margin-left: 8px;
}

.my-trades-page__trade-status-text {
  font-size: 11px;
  font-weight: 600;
}

.my-trades-page__trade-desc {
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

.my-trades-page__trade-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.my-trades-page__trade-time {
  font-size: 11px;
  color: var(--ink-300);
}

.my-trades-page__load-more,
.my-trades-page__no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px 0;
}

.my-trades-page__load-more-text,
.my-trades-page__no-more-text {
  font-size: 13px;
  color: var(--ink-300);
}

.my-trades-page__safe-bottom {
  height: 40px;
}

/* ── Empty ── */
.my-trades-page__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 32px;
}

.my-trades-page__empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.my-trades-page__empty-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--ink-600);
  margin-bottom: 8px;
}

.my-trades-page__empty-desc {
  font-size: 14px;
  color: var(--ink-300);
  text-align: center;
  margin-bottom: 20px;
}

.my-trades-page__empty-btn {
  padding: 10px 28px;
  background: var(--accent-500);
  border-radius: var(--radius-full);
}

.my-trades-page__empty-btn:active {
  background: var(--accent-600);
}

.my-trades-page__empty-btn-text {
  color: #fff;
  font-size: 15px;
  font-weight: 600;
}
</style>
