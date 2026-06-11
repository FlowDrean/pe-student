<template>
  <div class="noti-page page">
    <div class="noti-page__header">
      <h1 class="noti-page__title">通知中心</h1>
      <button v-if="unreadCount > 0" class="noti-page__read-all" @click="markAll">全部已读</button>
    </div>
    <Loading v-if="loading" variant="skeleton" :count="4" />
    <div v-else-if="list.length">
      <div v-for="item in list" :key="item.id" class="noti-item" :class="{ unread: !item.isRead }" @click="handleClick(item)">
        <div class="noti-item__icon">
          <SvgIcon :name="typeIcon(item.type)" :size="20" :color="typeColor(item.type)" />
        </div>
        <div class="noti-item__body">
          <p class="noti-item__title">{{ item.title }}</p>
          <p class="noti-item__content">{{ item.content }}</p>
          <span class="noti-item__time">{{ timeAgo(item.createdAt) }}</span>
        </div>
        <div v-if="!item.isRead" class="noti-item__unread-dot"></div>
      </div>
    </div>
    <EmptyState v-else title="暂无通知" description="有新的互动时会出现在这里" />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { timeAgo } from '@utils/format'
import { getNotifications, markNotificationRead, markAllNotificationsRead } from '@api'
import { useMessageStore } from '@stores/message'
import SvgIcon from '@components/common/SvgIcon.vue'
import Loading from '@components/common/Loading.vue'
import EmptyState from '@components/common/EmptyState.vue'

const router = useRouter()
const messageStore = useMessageStore()
const list = ref([])
const loading = ref(true)
const unreadCount = computed(() => list.value.filter(n => !n.isRead).length)
const typeIcons = { 1: 'heart_filled', 2: 'message-circle', 3: 'user', 4: 'bell', 5: 'shopping-cart' }
const typeColors = { 1: 'var(--coral-500)', 2: 'var(--accent-500)', 3: 'var(--accent-600)', 4: 'var(--amber-500)', 5: 'var(--coral-500)' }

function typeIcon(type) { return typeIcons[type] || 'bell' }
function typeColor(type) { return typeColors[type] || 'var(--ink-400)' }

async function fetchNotifications() {
  loading.value = true
  try { const data = await getNotifications({ page: 1, size: 50 }); list.value = (data.list || []).map(n => ({ ...n, isRead: n.isRead === 1 || n.isRead === true })); messageStore.setNotifications(list.value) }
  catch { list.value = [] }
  finally { loading.value = false }
}

async function markAll() {
  try { await markAllNotificationsRead(); list.value.forEach(n => { n.isRead = true }); messageStore.markAllRead(); showToast('已全部标记为已读') }
  catch { showToast('操作失败，请重试') }
}

async function handleClick(item) {
  if (!item.isRead) { try { await markNotificationRead(item.id) } catch { /* ignore */ }; item.isRead = true; messageStore.markRead(item.id) }
  if (item.targetId) {
    if (item.targetType === 1) router.push(`/post/${item.targetId}`)
    else if (item.targetType === 2) router.push(`/trade-item/${item.targetId}`)
  }
}

fetchNotifications()
</script>

<style scoped>
.noti-page__header { display: flex; justify-content: space-between; align-items: center; padding: var(--space-4) var(--content-padding); border-bottom: 2px solid var(--ink-100); background: var(--bg-primary); }
.noti-page__title { font-family: var(--font-display); font-size: var(--text-2xl); font-weight: var(--font-black); color: var(--ink-900); }
.noti-page__read-all { font-size: var(--text-sm); color: var(--accent-600); font-weight: var(--font-semibold); }
.noti-page__read-all:active { opacity: 0.7; }
.noti-item { display: flex; align-items: flex-start; gap: var(--space-3); padding: var(--space-4) var(--content-padding); background: var(--bg-primary); border-bottom: 1px solid var(--ink-50); position: relative; cursor: pointer; transition: background var(--transition-fast); }
.noti-item:active { background: var(--ink-50); }
.noti-item.unread { background: var(--accent-50); border-left: 3px solid var(--accent-500); }
.noti-item__icon { flex-shrink: 0; display: flex; margin-top: 2px; }
.noti-item__body { flex: 1; min-width: 0; }
.noti-item__title { font-size: var(--text-sm); font-weight: var(--font-semibold); color: var(--ink-800); }
.noti-item__content { font-size: var(--text-sm); color: var(--ink-500); margin-top: 2px; line-height: 1.5; word-break: break-all; }
.noti-item__time { font-size: var(--text-2xs); color: var(--ink-300); margin-top: 4px; display: block; }
.noti-item__unread-dot { width: 6px; height: 6px; background: var(--accent-600); border-radius: 2px; margin-top: 8px; flex-shrink: 0; }
</style>
