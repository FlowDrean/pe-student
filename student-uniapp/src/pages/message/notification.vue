<template>
  <view class="notification-page">
    <!-- Header -->
    <view class="notification-page__header">
      <text class="notification-page__title">通知中心</text>
      <view class="notification-page__mark-all" @tap="markAllRead">
        <text class="notification-page__mark-all-text">全部已读</text>
      </view>
    </view>

    <!-- Loading -->
    <view v-if="loading" class="notification-page__skeleton">
      <view v-for="i in 5" :key="i" class="notification-page__skeleton-item">
        <view class="skeleton notification-page__skeleton-icon" />
        <view class="notification-page__skeleton-body">
          <view class="skeleton notification-page__skeleton-title" />
          <view class="skeleton notification-page__skeleton-content" />
        </view>
      </view>
    </view>

    <!-- Notification List (grouped by type) -->
    <scroll-view
      v-else-if="groupedNotis.length > 0"
      class="notification-page__list"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
    >
      <view v-for="group in groupedNotis" :key="group.type" class="notification-page__group">
        <view class="notification-page__group-header">
          <wd-icon :name="group.icon" size="16px" />
          <text class="notification-page__group-label">{{ group.label }}</text>
          <text class="notification-page__group-count">{{ group.items.length }}</text>
        </view>
        <view
          v-for="item in group.items"
          :key="item.id"
          class="notification-page__item"
          :class="{ 'notification-page__item--unread': !item.isRead }"
          @tap="onTapItem(item)"
        >
          <view class="notification-page__item-body">
            <text class="notification-page__item-title">{{ item.title }}</text>
            <text class="notification-page__item-content">{{ item.content }}</text>
            <text class="notification-page__item-time">{{ timeAgo(item.createdAt) }}</text>
          </view>
          <view v-if="!item.isRead" class="notification-page__item-dot" />
        </view>
      </view>
      <view class="notification-page__safe-bottom" />
    </scroll-view>

    <!-- Empty State -->
    <view v-else class="notification-page__empty">
      <wd-icon name="notification" size="48px" color="var(--ink-200)" />
      <text class="notification-page__empty-title">暂无通知</text>
      <text class="notification-page__empty-desc">当有人点赞、评论或关注你时，会在这里显示</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { notificationApi } from '@api/notification'
import { useMessageStore } from '@stores/message'
import { timeAgo } from '@utils/format'
import { NOTI_TYPE } from '@utils/constants'

const messageStore = useMessageStore()

const loading = ref(true)
const isRefreshing = ref(false)

const typeMeta: Record<number, { label: string; icon: string }> = {
  [NOTI_TYPE.LIKE]: { label: '点赞', icon: 'heart-filled' },
  [NOTI_TYPE.COMMENT]: { label: '评论', icon: 'chat' },
  [NOTI_TYPE.FOLLOW]: { label: '关注', icon: 'user' },
  [NOTI_TYPE.SYSTEM]: { label: '系统', icon: 'notification' },
  [NOTI_TYPE.TRADE]: { label: '交易', icon: 'cart' },
}

const groupedNotis = computed(() => {
  const groups: Record<number, any[]> = {}
  for (const item of messageStore.notifications) {
    const t = item.type || 0
    if (!groups[t]) groups[t] = []
    groups[t].push(item)
  }
  return Object.entries(groups).map(([type, items]) => ({
    type: Number(type),
    label: typeMeta[Number(type)]?.label || '其他',
    icon: typeMeta[Number(type)]?.icon || 'pin',
    items,
  }))
})

async function fetchNotifications() {
  try {
    const data = await notificationApi.getList()
    const list = Array.isArray(data) ? data : (data?.list || data?.records || [])
    messageStore.setNotifications(list)
  } catch {
    // handled
  } finally {
    loading.value = false
    isRefreshing.value = false
  }
}

function onRefresh() {
  isRefreshing.value = true
  fetchNotifications()
}

async function onTapItem(item: any) {
  if (!item.isRead) {
    try {
      await notificationApi.markRead(item.id)
      messageStore.markRead(item.id)
    } catch {
      // silent - mark locally anyway
      messageStore.markRead(item.id)
    }
  }
  // Navigate based on type
  if (item.targetId) {
    const type = item.type
    if (type === NOTI_TYPE.LIKE || type === NOTI_TYPE.COMMENT) {
      uni.navigateTo({ url: `/pages/post/detail?id=${item.targetId}` })
    } else if (type === NOTI_TYPE.FOLLOW) {
      uni.navigateTo({ url: `/pages/user/profile?userId=${item.targetId}` })
    } else if (type === NOTI_TYPE.TRADE) {
      uni.navigateTo({ url: `/pages/trade/detail?id=${item.targetId}` })
    }
  }
}

async function markAllRead() {
  try {
    await notificationApi.markAllRead()
    messageStore.markAllRead()
    uni.showToast({ title: '已全部已读', icon: 'success' })
  } catch {
    messageStore.markAllRead()
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

onMounted(() => {
  fetchNotifications()
})
</script>

<style scoped>
.notification-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  display: flex;
  flex-direction: column;
}

/* ── Header ── */
.notification-page__header {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  background: var(--bg-primary);
  border-bottom: 2px solid var(--ink-100);
  position: sticky;
  top: 0;
  z-index: 100;
}

.notification-page__title {
  font-size: 17px;
  font-weight: 700;
  color: var(--ink-900);
}

.notification-page__mark-all {
  position: absolute;
  right: 16px;
  padding: 6px 12px;
}

.notification-page__mark-all:active {
  opacity: 0.7;
}

.notification-page__mark-all-text {
  font-size: 14px;
  font-weight: 600;
  color: var(--accent-500);
}

/* ── Skeleton ── */
.notification-page__skeleton {
  padding: 0;
}

.notification-page__skeleton-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: var(--bg-primary);
  border-bottom: 1px solid var(--ink-50);
}

.notification-page__skeleton-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  flex-shrink: 0;
}

.notification-page__skeleton-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.notification-page__skeleton-title {
  width: 100px;
  height: 14px;
}

.notification-page__skeleton-content {
  width: 200px;
  height: 12px;
}

/* ── List ── */
.notification-page__list {
  flex: 1;
}

.notification-page__group {
  margin-bottom: 8px;
}

.notification-page__group-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px 8px;
}

.notification-page__group-emoji {
  font-size: 16px;
}

.notification-page__group-label {
  font-size: 13px;
  font-weight: 700;
  color: var(--ink-400);
  text-transform: uppercase;
}

.notification-page__group-count {
  font-size: 12px;
  color: var(--ink-300);
  margin-left: auto;
}

.notification-page__item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: var(--bg-primary);
  border-bottom: 1px solid var(--ink-50);
  transition: background 0.15s ease;
  position: relative;
}

.notification-page__item--unread {
  background: var(--accent-50);
}

.notification-page__item--unread::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  bottom: 4px;
  width: 3px;
  background: var(--accent-500);
  border-radius: 0 2px 2px 0;
}

.notification-page__item:active {
  background: var(--ink-50);
}

.notification-page__item-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.notification-page__item-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--ink-800);
}

.notification-page__item-content {
  font-size: 13px;
  color: var(--ink-400);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-page__item-time {
  font-size: 11px;
  color: var(--ink-300);
}

.notification-page__item-dot {
  width: 8px;
  height: 8px;
  background: var(--error);
  border-radius: var(--radius-full);
  flex-shrink: 0;
}

.notification-page__safe-bottom {
  height: 20px;
}

/* ── Empty ── */
.notification-page__empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 32px;
}

.notification-page__empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.notification-page__empty-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--ink-600);
  margin-bottom: 8px;
}

.notification-page__empty-desc {
  font-size: 14px;
  color: var(--ink-300);
  text-align: center;
}
</style>
