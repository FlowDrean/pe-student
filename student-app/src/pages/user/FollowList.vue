<template>
  <div class="follow-page page">
    <!-- 标签切换 -->
    <div class="follow-tabs">
      <button class="follow-tab" :class="{ 'follow-tab--active': activeType === 'following' }" @click="switchType('following')">
        <span>{{ followingTotal }}</span> 关注
      </button>
      <button class="follow-tab" :class="{ 'follow-tab--active': activeType === 'fans' }" @click="switchType('fans')">
        <span>{{ fansTotal }}</span> 粉丝
      </button>
    </div>

    <!-- 用户列表 -->
    <div v-if="loading" class="follow-loading">
      <Loading variant="skeleton" :count="3" />
    </div>
    <template v-else>
      <div v-if="users.length" class="follow-list">
        <div v-for="u in users" :key="u.id" class="follow-item" @click="goProfile(u.id)">
          <div class="avatar avatar-md" :style="{ background: u.avatar ? 'transparent' : avatarColor(u.id) }">
            <img v-if="u.avatar" :src="u.avatar" class="avatar-img" />
            <span v-else class="avatar-inner">{{ u.nickname?.charAt(0) || '?' }}</span>
          </div>
          <div class="follow-item__info">
            <span class="follow-item__name">{{ u.nickname || u.username || '匿名' }}</span>
            <span class="follow-item__meta">{{ u.school || u.major || '这个人很懒，什么都没写' }}</span>
          </div>
          <button
            v-if="userStore.isLogin && u.id !== userStore.userId"
            class="follow-item__btn"
            :class="{ 'follow-item__btn--followed': u.isFollowed }"
            @click.stop="toggleFollow(u)"
          >
            {{ u.isFollowed ? '已关注' : '+ 关注' }}
          </button>
        </div>
      </div>
      <EmptyState v-else :title="activeType === 'following' ? '暂无关注' : '暂无粉丝'" :description="activeType === 'following' ? '还没有关注任何人' : '还没有粉丝，多发帖子吸引ta人吧'" :show-action="false" />
    </template>

    <!-- 加载更多 -->
    <button v-if="hasMore && !loading" class="follow-more" @click="loadMore">加载更多</button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '@stores/user'
import { getFollowingList, getFansList, followUser, unfollowUser } from '@api/user'
import Loading from '@components/common/Loading.vue'
import EmptyState from '@components/common/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const targetUserId = computed(() => route.params.id ? Number(route.params.id) : userStore.userId)
const activeType = ref(route.query.type === 'fans' ? 'fans' : 'following')

const users = ref([])
const loading = ref(true)
const page = ref(1)
const hasMore = ref(false)
const followingTotal = ref(0)
const fansTotal = ref(0)

const PAGE_SIZE = 20

function avatarColor(id) {
  const colors = ['#07C160', '#f97316', '#059669', '#f59e0b', '#7c3aed']
  return colors[(id || 0) % colors.length]
}

async function fetchList(append = false) {
  if (!targetUserId.value) return
  loading.value = true
  try {
    const params = { page: page.value, size: PAGE_SIZE }
    const [data, totals] = await Promise.all([
      activeType.value === 'following'
        ? getFollowingList(targetUserId.value, params)
        : getFansList(targetUserId.value, params),
      fetchTotals(),
    ])
    followingTotal.value = totals.following
    fansTotal.value = totals.fans
    const list = data.list || []
    users.value = append ? [...users.value, ...list] : list
    hasMore.value = data.pages ? page.value < data.pages : false
  } catch {
    if (!append) users.value = []
  } finally {
    loading.value = false
  }
}

async function fetchTotals() {
  // 列表接口返回的 total 字段即对应计数；为拿到两个总数，这里轻量请求第一页 size=1
  const results = await Promise.all([
    getFollowingList(targetUserId.value, { page: 1, size: 1 }).catch(() => null),
    getFansList(targetUserId.value, { page: 1, size: 1 }).catch(() => null),
  ])
  return {
    following: results[0]?.total || 0,
    fans: results[1]?.total || 0,
  }
}

function switchType(type) {
  if (activeType.value === type) return
  activeType.value = type
  page.value = 1
  fetchList()
}

function loadMore() {
  page.value++
  fetchList(true)
}

async function toggleFollow(u) {
  try {
    if (u.isFollowed) {
      await unfollowUser(u.id)
      u.isFollowed = false
    } else {
      await followUser(u.id)
      u.isFollowed = true
    }
  } catch {
    showToast('操作失败，请重试')
  }
}

function goProfile(id) {
  router.push(`/user-profile/${id}`)
}

onMounted(() => fetchList())
</script>

<style scoped>
.follow-page {
  padding: var(--space-3) var(--content-padding);
  background: var(--bg-secondary);
  min-height: 100vh;
}

.follow-tabs {
  display: flex;
  margin-bottom: var(--space-3);
  background: var(--bg-primary);
  border: 2px solid var(--ink-100);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.follow-tab {
  flex: 1;
  padding: var(--space-3);
  text-align: center;
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--ink-500);
  transition: all var(--transition-fast);
  border-bottom: 2px solid transparent;
}

.follow-tab span {
  font-family: var(--font-mono);
  font-weight: var(--font-bold);
  margin-right: 2px;
}

.follow-tab--active {
  color: var(--ink-900);
  border-bottom-color: var(--accent-600);
  background: var(--accent-50);
}

.follow-list {
  background: var(--bg-primary);
  border: 2px solid var(--ink-100);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.follow-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border-bottom: 1px solid var(--ink-50);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.follow-item:last-child {
  border-bottom: none;
}

.follow-item:active {
  background: var(--ink-50);
}

.avatar-inner {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #fff;
  font-family: var(--font-display);
  font-weight: var(--font-bold);
  border-radius: var(--radius-full);
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.follow-item__info {
  flex: 1;
  min-width: 0;
}

.follow-item__name {
  display: block;
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--ink-900);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.follow-item__meta {
  display: block;
  font-size: var(--text-xs);
  color: var(--ink-400);
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.follow-item__btn {
  flex-shrink: 0;
  padding: 6px 16px;
  border-radius: var(--radius-full);
  border: 2px solid var(--accent-600);
  background: var(--accent-600);
  color: #fff;
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  transition: all var(--transition-fast);
}

.follow-item__btn:active {
  transform: scale(0.94);
}

.follow-item__btn--followed {
  background: var(--bg-primary);
  border-color: var(--ink-200);
  color: var(--ink-400);
}

.follow-more {
  display: block;
  width: 100%;
  margin-top: var(--space-3);
  padding: var(--space-3);
  background: var(--bg-primary);
  border: 2px solid var(--ink-100);
  border-radius: var(--radius-lg);
  font-size: var(--text-sm);
  color: var(--ink-500);
}

.follow-loading {
  margin-top: var(--space-2);
}
</style>
