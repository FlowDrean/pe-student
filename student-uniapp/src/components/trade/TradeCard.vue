<template>
  <view class="trade-card" @tap="$emit('click')">
    <!-- 图片区域 -->
    <view class="trade-card__image-wrap">
      <image
        v-if="trade.images && trade.images.length && !imgError"
        :src="resolveAssetUrl(trade.images[0])"
        mode="aspectFill"
        class="trade-card__image"
        @error="imgError = true"
      />
      <view v-else class="trade-card__image-fallback" :style="{ background: fallbackBg }">
        <wd-icon :name="categoryIcon" size="48px" color="var(--ink-300)" />
      </view>

      <!-- 状态徽章 -->
      <view class="trade-card__badge" :class="[`trade-card__badge--${statusClass}`]">
        <text class="trade-card__badge-text">{{ statusLabel }}</text>
      </view>
    </view>

    <!-- 信息区域 -->
    <view class="trade-card__body">
      <text class="trade-card__title">{{ trade.title }}</text>
      <text class="trade-card__desc">{{ trade.description }}</text>

      <view class="trade-card__footer">
        <PriceTag :price="trade.price" :original-price="trade.originalPrice" size="sm" />
        <view class="trade-card__views">
          <wd-icon name="browse" size="13px" color="var(--ink-400)" />
          <text class="trade-card__views-count">{{ formatNumber(trade.views || 0) }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { resolveAssetUrl } from '@/http'
import { computed, ref } from 'vue'
import { formatNumber } from '@utils/format'
import PriceTag from './PriceTag.vue'

const imgError = ref(false)

interface TradeAuthor {
  nickname?: string
  avatar?: string
  id?: number
}

interface Trade {
  id: number
  title: string
  description: string
  price: number
  originalPrice?: number
  images?: string[]
  category?: string
  condition?: number
  status?: number
  views?: number
  createdAt: string
  author?: TradeAuthor
}

const props = defineProps<{
  trade: Trade
}>()

defineEmits<{
  click: []
}>()

const statusLabel = computed(() => {
  const map: Record<number, string> = { 0: '已下架', 1: '在售', 2: '已售', 3: '交易中' }
  return map[props.trade.status ?? 1] || '在售'
})

const statusClass = computed(() => {
  const map: Record<number, string> = { 0: 'offline', 1: 'selling', 2: 'sold', 3: 'dealing' }
  return map[props.trade.status ?? 1] || 'selling'
})

const categoryIconMap: Record<string, string> = {
  book: 'books',
  electronics: 'computer',
  daily: 'tools',
  clothing: 'shop',
  other: 'goods',
}

const categoryIcon = computed(() => {
  return categoryIconMap[props.trade.category || 'other'] || 'goods'
})

const fallbackBg = computed(() => {
  const colors = ['#f0fdf4', '#fff7ed', '#ecfdf5', '#fefce8', '#fef2f2', '#f5f3ff']
  const idx = (props.trade.id || 0) % colors.length
  return colors[idx]
})
</script>

<style scoped>
.trade-card {
  background: var(--bg-primary);
  border: 2px solid var(--ink-100);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  transition: all 0.15s ease;
}

.trade-card:active {
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

/* ── 图片 ── */
.trade-card__image-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 4/3;
  background: var(--ink-100);
  overflow: hidden;
}

.trade-card__image {
  width: 100%;
  height: 100%;
  display: block;
}

.trade-card__image-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ── 状态徽章 ── */
.trade-card__badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 3px 10px;
  border-radius: var(--radius-full);
}

.trade-card__badge--selling {
  background: rgba(7, 193, 96, 0.9);
}

.trade-card__badge--sold {
  background: rgba(161, 161, 170, 0.9);
}

.trade-card__badge--dealing {
  background: rgba(245, 158, 11, 0.9);
}

.trade-card__badge--offline {
  background: rgba(113, 113, 122, 0.9);
}

.trade-card__badge-text {
  color: #fff;
  font-size: 11px;
  font-weight: 600;
}

/* ── 内容 ── */
.trade-card__body {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.trade-card__title {
  font-size: 15px;
  font-weight: 700;
  color: var(--ink-900);
  line-height: 1.35;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.trade-card__desc {
  font-size: 13px;
  color: var(--ink-500);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ── 底部 ── */
.trade-card__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 8px;
  border-top: 1px solid var(--ink-50);
}

.trade-card__views {
  display: flex;
  align-items: center;
  gap: 4px;
}

.trade-card__views-count {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--ink-400);
}
</style>
