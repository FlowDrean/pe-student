<template>
  <tabbar>
    <view class="message-page">
    <!-- Header -->
    <view class="message-page__header">
      <text class="message-page__title">消息</text>
      <view class="message-page__bell" @tap="goNotification">
        <wd-icon name="notification" size="22px" />
        <view v-if="messageStore.notiUnreadCount > 0" class="message-page__bell-dot" />
      </view>
    </view>

    <!-- Loading Skeleton -->
    <view v-if="loading" class="message-page__skeleton-list">
      <view v-for="i in 5" :key="i" class="message-page__skeleton-item">
        <view class="skeleton message-page__skeleton-avatar" />
        <view class="message-page__skeleton-content">
          <view class="skeleton message-page__skeleton-name" />
          <view class="skeleton message-page__skeleton-text" />
        </view>
      </view>
    </view>

    <!-- Conversation List -->
    <scroll-view
      v-else-if="conversations.length > 0"
      class="message-page__list"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
    >
      <view
        v-for="conv in conversations"
        :key="conv.userId"
        class="message-page__conv"
        @tap="goChat(conv)"
      >
        <view class="message-page__conv-avatar" :style="{ background: avatarColor(conv) }">
          <text class="message-page__conv-avatar-text">{{ firstChar(conv) }}</text>
        </view>
        <view class="message-page__conv-body">
          <view class="message-page__conv-top">
            <text class="message-page__conv-name">{{ conv.nickname || '用户' }}</text>
            <text class="message-page__conv-time">{{ timeAgo(conv.lastTime || '') }}</text>
          </view>
          <view class="message-page__conv-bottom">
            <text class="message-page__conv-preview text-ellipsis">{{ conv.lastMessage || '暂无消息' }}</text>
            <view v-if="conv.unread > 0" class="message-page__conv-badge">
              <text class="message-page__conv-badge-text">{{ conv.unread > 99 ? '99+' : conv.unread }}</text>
            </view>
          </view>
        </view>
      </view>
      <view class="message-page__safe-bottom" />
    </scroll-view>

    <!-- Empty State -->
    <view v-else class="message-page__empty">
      <wd-icon name="chat" size="48px" color="var(--ink-200)" />
      <text class="message-page__empty-title">暂无消息</text>
      <text class="message-page__empty-desc">去逛逛社区，开始和同学交流吧</text>
    </view>
  </view>
  </tabbar>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { messageApi } from '@api/message'
import { notificationApi } from '@api/notification'
import { useUserStore } from '@stores/user'
import { useMessageStore } from '@stores/message'
import { timeAgo } from '@utils/format'
import tabbar from '@layouts/tabbar.vue'

const userStore = useUserStore()
const messageStore = useMessageStore()

const conversations = ref<any[]>([])
const loading = ref(true)
const isRefreshing = ref(false)
let pollTimer: number | null = null

function avatarColor(conv: any): string {
  const colors = ['#07C160', '#f97316', '#059669', '#f59e0b', '#dc2626', '#7c3aed']
  const idx = (conv.userId || 0) % colors.length
  return colors[idx]
}

function firstChar(conv: any): string {
  return (conv.nickname || '用').charAt(0)
}

async function fetchConversations() {
  try {
    const data = await messageApi.getConversations()
    conversations.value = data || []
    messageStore.setConversations(conversations.value)
  } catch {
    // error handled by http interceptor
  } finally {
    loading.value = false
    isRefreshing.value = false
  }
}

async function fetchNotiUnread() {
  try {
    const count = await notificationApi.getUnreadCount()
    messageStore.notiUnreadCount = (count as any) || 0
  } catch {
    // silent
  }
}

function onRefresh() {
  isRefreshing.value = true
  fetchConversations()
}

function goChat(conv: any) {
  uni.navigateTo({ url: `/pages/message/chat?peerId=${conv.userId}&peerName=${encodeURIComponent(conv.nickname || '用户')}` })
}

function goNotification() {
  uni.navigateTo({ url: '/pages/message/notification' })
}

// Polling fallback for real-time updates
function startPolling() {
  pollTimer = setInterval(() => {
    fetchConversations()
    fetchNotiUnread()
  }, 10000) as unknown as number
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

onMounted(() => {
  fetchConversations()
  fetchNotiUnread()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.message-page {
  flex: 1;
  background: var(--bg-secondary);
  display: flex;
  flex-direction: column;
}

/* ── Header ── */
.message-page__header {
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

.message-page__title {
  font-size: 17px;
  font-weight: 700;
  color: var(--ink-900);
}

.message-page__bell {
  position: absolute;
  right: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
}

.message-page__bell:active {
  background: var(--ink-50);
}

.message-page__bell-icon {
  font-size: 18px;
}

.message-page__bell-dot {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 8px;
  height: 8px;
  background: var(--error);
  border-radius: var(--radius-full);
  border: 2px solid var(--bg-primary);
}

/* ── Skeleton ── */
.message-page__skeleton-list {
  padding: 0;
}

.message-page__skeleton-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--bg-primary);
  border-bottom: 1px solid var(--ink-50);
}

.message-page__skeleton-avatar {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-full);
  flex-shrink: 0;
}

.message-page__skeleton-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.message-page__skeleton-name {
  width: 120px;
  height: 14px;
}

.message-page__skeleton-text {
  width: 200px;
  height: 12px;
}

/* ── List ── */
.message-page__list {
  flex: 1;
}

.message-page__conv {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--bg-primary);
  border-bottom: 1px solid var(--ink-50);
  transition: background 0.15s ease;
}

.message-page__conv:active {
  background: var(--ink-50);
}

.message-page__conv-avatar {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message-page__conv-avatar-text {
  color: #fff;
  font-size: 18px;
  font-weight: 700;
}

.message-page__conv-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-page__conv-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.message-page__conv-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--ink-800);
}

.message-page__conv-time {
  font-size: 11px;
  color: var(--ink-300);
  flex-shrink: 0;
}

.message-page__conv-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.message-page__conv-preview {
  font-size: 13px;
  color: var(--ink-400);
  max-width: 240px;
}

.message-page__conv-badge {
  min-width: 20px;
  height: 20px;
  background: var(--error);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
  flex-shrink: 0;
}

.message-page__conv-badge-text {
  color: #fff;
  font-size: 11px;
  font-weight: 700;
}

.message-page__safe-bottom {
  height: 20px;
}

/* ── Empty ── */
.message-page__empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 32px;
}

.message-page__empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.message-page__empty-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--ink-600);
  margin-bottom: 8px;
}

.message-page__empty-desc {
  font-size: 14px;
  color: var(--ink-300);
  text-align: center;
}
</style>
