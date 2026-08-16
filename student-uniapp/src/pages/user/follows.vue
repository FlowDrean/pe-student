<template>
  <view class="follows-page">
    <!-- 标签切换 -->
    <view class="follows-page__tabs">
      <view
        class="follows-page__tab"
        :class="{ 'follows-page__tab--active': activeType === 'following' }"
        @tap="switchType('following')"
      >
        <text class="follows-page__tab-text">{{ followingTotal }} 关注</text>
      </view>
      <view
        class="follows-page__tab"
        :class="{ 'follows-page__tab--active': activeType === 'fans' }"
        @tap="switchType('fans')"
      >
        <text class="follows-page__tab-text">{{ fansTotal }} 粉丝</text>
      </view>
    </view>

    <!-- 列表 -->
    <view v-if="loading" class="follows-page__status">
      <text class="follows-page__status-text">加载中...</text>
    </view>
    <view v-else-if="users.length === 0" class="follows-page__status">
      <text class="follows-page__status-text">{{ activeType === 'following' ? '暂无关注' : '暂无粉丝' }}</text>
    </view>
    <view v-else class="follows-page__list">
      <view v-for="u in users" :key="u.id" class="follows-page__item" @tap="goProfile(u.id)">
        <view class="follows-page__avatar" :style="{ background: u.avatar ? 'transparent' : avatarColor(u.id) }">
          <image v-if="u.avatar" class="follows-page__avatar-img" :src="resolveAssetUrl(u.avatar)" mode="aspectFill" />
          <text v-else class="follows-page__avatar-text">{{ (u.nickname || u.username || '?').charAt(0) }}</text>
        </view>
        <view class="follows-page__info">
          <text class="follows-page__name">{{ u.nickname || u.username || '匿名' }}</text>
          <text class="follows-page__meta">{{ u.school || u.major || '这个人很懒，什么都没写' }}</text>
        </view>
        <view
          v-if="userStore.isLogin && u.id !== userStore.userId"
          class="follows-page__btn"
          :class="{ 'follows-page__btn--followed': u.isFollowed }"
          @tap.stop="toggleFollow(u)"
        >
          <text class="follows-page__btn-text">{{ u.isFollowed ? '已关注' : '+ 关注' }}</text>
        </view>
      </view>

      <view v-if="hasMore" class="follows-page__more" @tap="loadMore">
        <text class="follows-page__more-text">{{ loadingMore ? '加载中...' : '加载更多' }}</text>
      </view>
    </view>

    <view class="follows-page__safe-bottom" />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { userApi } from '@api/user'
import { useUserStore } from '@stores/user'
import { resolveAssetUrl } from '@/http'

const userStore = useUserStore()

const userId = ref<number>(0)
const activeType = ref<'following' | 'fans'>('following')
const users = ref<any[]>([])
const loading = ref(true)
const loadingMore = ref(false)
const page = ref(1)
const hasMore = ref(false)
const followingTotal = ref(0)
const fansTotal = ref(0)

const PAGE_SIZE = 20

onLoad((options: any) => {
  userId.value = Number(options.id || options.userId) || userStore.userId || 0
  activeType.value = options.type === 'fans' ? 'fans' : 'following'
  fetchList()
})

function avatarColor(id: number) {
  const colors = ['#07C160', '#f97316', '#059669', '#f59e0b', '#dc2626', '#7c3aed']
  return colors[(id || 0) % colors.length]
}

async function fetchList(append = false) {
  if (!userId.value) return
  append ? (loadingMore.value = true) : (loading.value = true)
  try {
    const params = { page: page.value, size: PAGE_SIZE }
    const data =
      activeType.value === 'following'
        ? await userApi.getFollowingList(userId.value, params)
        : await userApi.getFansList(userId.value, params)
    const list = data?.list || []
    users.value = append ? [...users.value, ...list] : list
    hasMore.value = data?.pages ? page.value < data.pages : false
    fetchTotals()
  } catch {
    if (!append) users.value = []
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

async function fetchTotals() {
  try {
    const [following, fans] = await Promise.all([
      userApi.getFollowingList(userId.value, { page: 1, size: 1 }),
      userApi.getFansList(userId.value, { page: 1, size: 1 }),
    ])
    followingTotal.value = following?.total || 0
    fansTotal.value = fans?.total || 0
  } catch { /* ignore */ }
}

function switchType(type: 'following' | 'fans') {
  if (activeType.value === type) return
  activeType.value = type
  page.value = 1
  fetchList()
}

function loadMore() {
  if (loadingMore.value) return
  page.value++
  fetchList(true)
}

async function toggleFollow(u: any) {
  if (!userStore.isLogin) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  try {
    if (u.isFollowed) {
      await userApi.unfollowUser(u.id)
      u.isFollowed = false
    } else {
      await userApi.followUser(u.id)
      u.isFollowed = true
    }
  } catch { /* handled */ }
}

function goProfile(id: number) {
  uni.navigateTo({ url: `/pages/user/profile?id=${id}` })
}
</script>

<style scoped>
.follows-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding: 16px;
}

/* ── Tabs ── */
.follows-page__tabs {
  display: flex;
  background: var(--bg-primary);
  border: 2px solid var(--ink-100);
  border-radius: var(--radius-lg);
  overflow: hidden;
  margin-bottom: 16px;
}

.follows-page__tab {
  flex: 1;
  padding: 12px 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.follows-page__tab--active {
  background: var(--accent-500);
}

.follows-page__tab-text {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-400);
}

.follows-page__tab--active .follows-page__tab-text {
  color: #fff;
}

/* ── 列表 ── */
.follows-page__list {
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
  border: 2px solid var(--ink-100);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.follows-page__item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--ink-50);
}

.follows-page__item:last-child {
  border-bottom: none;
}

.follows-page__avatar {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}

.follows-page__avatar-img {
  width: 100%;
  height: 100%;
}

.follows-page__avatar-text {
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  font-family: var(--font-display);
}

.follows-page__info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.follows-page__name {
  font-size: 15px;
  font-weight: 600;
  color: var(--ink-900);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.follows-page__meta {
  font-size: 12px;
  color: var(--ink-400);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.follows-page__btn {
  padding: 5px 14px;
  background: var(--accent-500);
  border-radius: var(--radius-full);
  flex-shrink: 0;
}

.follows-page__btn:active {
  transform: scale(0.95);
}

.follows-page__btn--followed {
  background: var(--ink-100);
}

.follows-page__btn-text {
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}

.follows-page__btn--followed .follows-page__btn-text {
  color: var(--ink-500);
}

/* ── 状态 ── */
.follows-page__status {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
}

.follows-page__status-text {
  font-size: 14px;
  color: var(--ink-300);
}

.follows-page__more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 0;
}

.follows-page__more-text {
  font-size: 13px;
  color: var(--ink-400);
}

.follows-page__safe-bottom {
  height: 40px;
}
</style>
