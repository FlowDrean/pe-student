<template>
  <div class="message-page page">
    <div class="message-page__header">
      <h1 class="message-page__title">消息</h1>
      <router-link to="/notification" class="message-page__noti">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="var(--ink-600)" stroke-width="2"><path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 01-3.46 0"/></svg>
        <span v-if="notiUnread" class="dot"></span>
      </router-link>
    </div>

    <Loading v-if="loading" variant="skeleton" :count="3" />
    <div v-else-if="conversations.length" class="conversation-list">
      <MessagePreview
        v-for="conv in conversations"
        :key="conv.userId"
        :conversation="conv"
        @click="goChat(conv.userId)"
      />
    </div>
    <EmptyState v-else title="暂无消息" description="去逛逛帖子，和同学们互动吧" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessageStore } from '@stores/message'
import { getConversations } from '@api'
import { getNotificationUnreadCount } from '@api/notification'
import MessagePreview from '@components/message/MessagePreview.vue'
import Loading from '@components/common/Loading.vue'
import EmptyState from '@components/common/EmptyState.vue'

const router = useRouter()
const messageStore = useMessageStore()
const notiUnread = computed(() => messageStore.notiUnreadCount)
const loading = ref(true)
const conversations = ref([])

async function fetchConversations() {
  loading.value = true
  try {
    const data = await getConversations()
    conversations.value = data || []
    messageStore.setConversations(conversations.value)
  } catch { conversations.value = [] }
  finally { loading.value = false }
}

function goChat(userId) { router.push(`/chat/${userId}`) }

async function fetchNotiUnread() {
  try { const count = await getNotificationUnreadCount(); messageStore.notiUnreadCount = count || 0 } catch { /* ignore */ }
}

onMounted(() => { fetchConversations(); fetchNotiUnread() })
</script>

<style scoped>
.message-page { background: var(--bg-secondary); }
.message-page__header {
  display: flex; justify-content: space-between; align-items: center;
  padding: var(--space-4) var(--content-padding);
  padding-top: calc(env(safe-area-inset-top) + var(--space-4));
  background: var(--bg-primary);
  border-bottom: 2px solid var(--ink-100);
}
.message-page__title { font-family: var(--font-display); font-size: var(--text-2xl); font-weight: var(--font-black); color: var(--ink-900); letter-spacing: -0.02em; }
.message-page__noti { position: relative; padding: 6px; border-radius: var(--radius-md); transition: all var(--transition-fast); }
.message-page__noti:active { background: var(--ink-50); }
.dot { position: absolute; top: 4px; right: 4px; width: 8px; height: 8px; background: var(--error); border-radius: 2px; box-shadow: 0 1px 0 0 rgba(220, 38, 38, 0.3); }
.conversation-list { background: var(--bg-primary); }
</style>
