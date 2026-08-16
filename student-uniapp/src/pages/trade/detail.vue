<template>
  <view class="trade-detail">
    <!-- Loading -->
    <Loading v-if="loading" variant="skeleton" :count="2" />

    <!-- Error -->
    <ErrorState v-else-if="error" :title="error" @retry="fetchDetail" />

    <!-- Content -->
    <template v-else-if="detail">
      <!-- Image Swiper -->
      <view class="trade-detail__swiper-wrap" v-if="detail.images && detail.images.length > 0">
        <swiper class="trade-detail__swiper" indicator-dots indicator-color="rgba(255,255,255,0.4)" indicator-active-color="#07C160" circular @change="onSwiperChange">
          <swiper-item v-for="(img, index) in detail.images" :key="index">
            <image v-if="!imgErrors[index]" :src="resolveAssetUrl(img)" class="trade-detail__swiper-img" mode="aspectFill" @tap="previewImage(index)" @error="imgErrors[index] = true" />
            <view v-else class="trade-detail__img-fallback">
              <wd-icon name="picture" size="48px" color="var(--ink-200)" />
            </view>
          </swiper-item>
        </swiper>
        <view class="trade-detail__swiper-counter">
          <text class="trade-detail__swiper-counter-text">{{ currentSwiper + 1 }} / {{ detail.images.length }}</text>
        </view>
      </view>
      <view v-else class="trade-detail__img-fallback">
        <wd-icon name="goods" size="48px" color="var(--ink-200)" />
      </view>

      <!-- Price & Status -->
      <view class="trade-detail__header card">
        <view class="trade-detail__price-row">
          <PriceTag :price="detail.price" :original-price="detail.originalPrice" size="md" />
          <view class="trade-detail__status" :class="statusClass">
            <text class="trade-detail__status-text">{{ statusLabel }}</text>
          </view>
        </view>
        <text class="trade-detail__title">{{ detail.title }}</text>
      </view>

      <!-- Description & Details -->
      <view class="trade-detail__body card">
        <text class="trade-detail__section-title">商品描述</text>
        <text class="trade-detail__desc">{{ detail.description || '暂无描述' }}</text>

        <view class="trade-detail__meta">
          <view class="trade-detail__meta-item" v-if="conditionLabel">
            <text class="trade-detail__meta-label">成色</text>
            <view class="trade-detail__meta-tag">{{ conditionLabel }}</view>
          </view>
          <view class="trade-detail__meta-item" v-if="detail.location">
            <text class="trade-detail__meta-label">地点</text>
            <text class="trade-detail__meta-value">{{ detail.location }}</text>
          </view>
          <view class="trade-detail__meta-item">
            <text class="trade-detail__meta-label">浏览</text>
            <text class="trade-detail__meta-value">{{ detail.views || 0 }} 次</text>
          </view>
          <view class="trade-detail__meta-item">
            <text class="trade-detail__meta-label">收藏</text>
            <text class="trade-detail__meta-value">{{ detail.favorites || 0 }} 次</text>
          </view>
        </view>
      </view>

      <!-- Seller Info -->
      <view class="trade-detail__seller card">
        <view class="trade-detail__seller-info" @tap="goProfile">
          <UserAvatar :avatar="detail.user?.avatar" :name="detail.user?.nickname" :user-id="detail.userId" :size="44" />
          <view class="trade-detail__seller-detail">
            <text class="trade-detail__seller-name">{{ detail.user?.nickname || '卖家' }}</text>
            <text class="trade-detail__seller-time" v-if="detail.createdAt">{{ timeAgo(detail.createdAt) }}发布</text>
          </view>
        </view>
        <view class="trade-detail__seller-actions" v-if="!isOwner">
          <view class="trade-detail__seller-btn trade-detail__seller-btn--outline" @tap="handleContact">
            <text class="trade-detail__seller-btn-text">联系卖家</text>
          </view>
          <view class="trade-detail__seller-btn trade-detail__seller-btn--primary" @tap="handleChat">
            <text class="trade-detail__seller-btn-text trade-detail__seller-btn-text--white">发私信</text>
          </view>
        </view>
      </view>

      <!-- Owner Actions -->
      <view class="trade-detail__owner-actions" v-if="isOwner">
        <view class="trade-detail__owner-btn" @tap="goEdit">
          <text class="trade-detail__owner-btn-text">编辑</text>
        </view>
        <view class="trade-detail__owner-btn trade-detail__owner-btn--danger" @tap="handleDelete">
          <text class="trade-detail__owner-btn-text trade-detail__owner-btn-text--danger">删除</text>
        </view>
      </view>
    </template>

    <!-- Bottom Action Bar -->
    <view class="trade-detail__bottom-bar safe-area-bottom" v-if="detail && !isOwner">
      <view class="trade-detail__bottom-action" :class="{ 'is-active': detail.isFavorited }" @tap="handleFavorite">
        <wd-icon :name="detail.isFavorited ? 'star-filled' : 'star'" size="20px" :color="detail.isFavorited ? 'var(--warning)' : 'var(--ink-400)'" />
        <text class="trade-detail__bottom-action-text">收藏</text>
      </view>
      <view class="trade-detail__bottom-action" @tap="handleContact">
        <wd-icon name="phone" size="20px" />
        <text class="trade-detail__bottom-action-text">联系</text>
      </view>
      <view class="trade-detail__bottom-buy" @tap="handleChat">
        <text class="trade-detail__bottom-buy-text">我想要</text>
      </view>
    </view>

    <view class="trade-detail__placeholder" v-if="detail" />
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { tradeApi } from '@api/trade'
import { resolveAssetUrl } from '@/http'
import { useUserStore } from '@stores/user'
import { timeAgo } from '@utils/format'
import { CONDITIONS, TRADE_STATUS_LABELS } from '@utils/constants'
import PriceTag from '@components/trade/PriceTag.vue'
import Loading from '@components/common/Loading.vue'
import ErrorState from '@components/common/ErrorState.vue'
import UserAvatar from '@components/common/UserAvatar.vue'

const userStore = useUserStore()

const detail = ref<any>(null)
const loading = ref(true)
const error = ref('')
const currentSwiper = ref(0)
const imgErrors = ref<Record<number, boolean>>({})

const tradeId = ref<number>(0)

// ---- Status ----
const statusLabel = computed(() => {
  if (!detail.value) return ''
  return TRADE_STATUS_LABELS[detail.value.status] || '在售'
})

const statusClass = computed(() => {
  const s = detail.value?.status
  if (s === 2) return 'trade-detail__status--sold'
  if (s === 3) return 'trade-detail__status--dealing'
  if (s === 0) return 'trade-detail__status--offline'
  return ''
})

// ---- Condition ----
const conditionLabel = computed(() => {
  if (!detail.value) return ''
  const c = CONDITIONS.find((c) => c.key === detail.value.condition)
  return c?.label || ''
})

// ---- Owner check ----
const isOwner = computed(() => {
  return userStore.userId && detail.value?.userId === userStore.userId
})

// ---- Fetch ----
async function fetchDetail() {
  if (!tradeId.value) return
  loading.value = true
  error.value = ''
  try {
    const data: any = await tradeApi.getDetail(tradeId.value)
    detail.value = data
  } catch (e: any) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

// ---- Actions ----
function previewImage(index: number) {
  if (!detail.value?.images) return
  uni.previewImage({
    urls: detail.value.images,
    current: String(index),
  })
}

async function handleFavorite() {
  if (!detail.value) return
  try {
    await tradeApi.toggleFavorite(detail.value.id)
    detail.value.isFavorited = !detail.value.isFavorited
    detail.value.favorites = (detail.value.favorites || 0) + (detail.value.isFavorited ? 1 : -1)
  } catch {
    // error handled by interceptor
  }
}

function handleContact() {
  if (detail.value?.contact) {
    uni.showModal({
      title: '联系方式',
      content: detail.value.contact,
      showCancel: false,
      confirmText: '知道了',
    })
  } else {
    uni.showToast({ title: '暂无联系方式', icon: 'none' })
  }
}

function handleChat() {
  const peerId = detail.value?.userId
  const peerName = detail.value?.userNickname || '卖家'
  if (!peerId) {
    return uni.showToast({ title: '无法联系', icon: 'none' })
  }
  uni.navigateTo({ url: `/pages/message/chat?peerId=${peerId}&peerName=${encodeURIComponent(peerName)}` })
}

function goProfile() {
  const userId = detail.value?.userId
  if (userId) {
    uni.navigateTo({ url: `/pages/user/profile?id=${userId}` })
  }
}

function goEdit() {
  uni.navigateTo({ url: `/pages/trade/edit?id=${tradeId.value}` })
}

function handleDelete() {
  uni.showModal({
    title: '确认删除',
    content: '删除后将无法恢复，确定要删除这个商品吗？',
    confirmText: '删除',
    confirmColor: '#dc2626',
    success: async (res) => {
      if (res.confirm) {
        try {
          await tradeApi.delete(tradeId.value)
          uni.showToast({ title: '已删除', icon: 'success' })
          setTimeout(() => {
            uni.switchTab({ url: '/pages/trade/index' })
          }, 800)
        } catch {
          // error handled by interceptor
        }
      }
    },
  })
}

// ---- Swiper change ----
function onSwiperChange(e: any) {
  currentSwiper.value = e.detail.current
}

// ---- Lifecycle ----
onLoad((options: any) => {
  if (options?.id) {
    tradeId.value = Number(options.id)
    fetchDetail()
  }
})
</script>

<style scoped>
.trade-detail {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding-bottom: 16px;
}

/* ── Swiper ── */
.trade-detail__swiper-wrap {
  position: relative;
}

.trade-detail__swiper {
  width: 100%;
  height: 320px;
}

.trade-detail__swiper-img {
  width: 100%;
  height: 100%;
  display: block;
}

.trade-detail__swiper-counter {
  position: absolute;
  bottom: 12px;
  right: 12px;
  padding: 2px 10px;
  border-radius: var(--radius-full);
  background: rgba(15, 17, 25, 0.6);
}

.trade-detail__swiper-counter-text {
  font-size: 12px;
  color: #fff;
  font-family: var(--font-mono);
}

.trade-detail__img-fallback {
  width: 100%;
  height: 240px;
  background: var(--ink-100);
  display: flex;
  align-items: center;
  justify-content: center;
}

.trade-detail__img-fallback-text {
  font-size: 56px;
}

/* ── Header ── */
.trade-detail__header {
  padding: 16px;
  margin: 12px 12px 0;
}

.trade-detail__price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.trade-detail__status {
  padding: 2px 10px;
  border-radius: var(--radius-full);
  background: var(--accent-500);
}

.trade-detail__status--sold {
  background: var(--ink-400);
}

.trade-detail__status--dealing {
  background: var(--warning);
}

.trade-detail__status--offline {
  background: var(--ink-400);
}

.trade-detail__status-text {
  font-size: 12px;
  font-weight: 600;
  color: #fff;
}

.trade-detail__title {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 700;
  color: var(--ink-900);
  line-height: 1.4;
  display: block;
}

/* ── Body ── */
.trade-detail__body {
  padding: 16px;
  margin: 12px 12px 0;
}

.trade-detail__section-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--ink-700);
  margin-bottom: 8px;
  display: block;
}

.trade-detail__desc {
  font-size: 14px;
  color: var(--ink-600);
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  display: block;
  margin-bottom: 16px;
}

.trade-detail__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--ink-50);
}

.trade-detail__meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.trade-detail__meta-label {
  font-size: 12px;
  color: var(--ink-400);
}

.trade-detail__meta-value {
  font-size: 13px;
  color: var(--ink-600);
  font-weight: 500;
}

.trade-detail__meta-tag {
  padding: 1px 8px;
  border-radius: var(--radius-full);
  background: var(--ink-100);
  font-size: 12px;
  color: var(--ink-600);
  font-weight: 500;
  border: 1px solid var(--ink-200);
}

/* ── Seller ── */
.trade-detail__seller {
  padding: 16px;
  margin: 12px 12px 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.trade-detail__seller-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.trade-detail__seller-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid var(--ink-100);
}

.trade-detail__seller-avatar-text {
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  font-family: var(--font-display);
}

.trade-detail__seller-detail {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.trade-detail__seller-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-800);
}

.trade-detail__seller-time {
  font-size: 11px;
  color: var(--ink-400);
}

.trade-detail__seller-actions {
  display: flex;
  gap: 8px;
}

.trade-detail__seller-btn {
  padding: 8px 14px;
  border-radius: var(--radius-full);
  border: 2px solid var(--ink-200);
  background: var(--bg-primary);
}

.trade-detail__seller-btn--primary {
  background: var(--accent-500);
  border-color: var(--accent-500);
}

.trade-detail__seller-btn:active {
  transform: scale(0.96);
}

.trade-detail__seller-btn-text {
  font-size: 13px;
  font-weight: 500;
  color: var(--ink-600);
}

.trade-detail__seller-btn-text--white {
  color: #fff;
}

/* ── Owner Actions ── */
.trade-detail__owner-actions {
  display: flex;
  gap: 12px;
  padding: 12px;
}

.trade-detail__owner-btn {
  flex: 1;
  padding: 12px;
  text-align: center;
  border-radius: var(--radius-md);
  background: var(--bg-primary);
  border: 2px solid var(--ink-200);
}

.trade-detail__owner-btn:active {
  transform: scale(0.98);
}

.trade-detail__owner-btn--danger {
  border-color: var(--error);
}

.trade-detail__owner-btn-text {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-700);
}

.trade-detail__owner-btn-text--danger {
  color: var(--error);
}

/* ── Bottom Bar ── */
.trade-detail__bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: var(--bg-primary);
  border-top: 2px solid var(--ink-100);
  z-index: 200;
  box-shadow: 0 -2px 8px rgba(15, 17, 25, 0.04);
}

.trade-detail__bottom-action {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 6px 10px;
  border-radius: var(--radius-sm);
}

.trade-detail__bottom-action:active {
  background: var(--ink-50);
  transform: scale(0.94);
}

.trade-detail__bottom-action.is-active .trade-detail__bottom-action-icon {
  color: var(--warning);
}

.trade-detail__bottom-action-icon {
  font-size: 18px;
  line-height: 1;
}

.trade-detail__bottom-action-text {
  font-size: 10px;
  color: var(--ink-400);
}

.trade-detail__bottom-buy {
  flex: 1;
  padding: 12px;
  text-align: center;
  background: var(--accent-500);
  border-radius: var(--radius-full);
  box-shadow: 0 2px 8px rgba(7, 193, 96, 0.3);
}

.trade-detail__bottom-buy:active {
  background: var(--accent-600);
  transform: scale(0.98);
}

.trade-detail__bottom-buy-text {
  font-size: 16px;
  font-weight: 700;
  color: #fff;
}

.trade-detail__placeholder {
  height: 70px;
}
</style>
