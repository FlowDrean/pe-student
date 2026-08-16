<template>
  <view class="profile-page">
    <scroll-view class="profile-page__body" scroll-y>
      <!-- Loading -->
      <view v-if="loading" class="profile-page__loading">
        <text class="profile-page__loading-text">加载中...</text>
      </view>

      <template v-else-if="profile">
        <!-- User Card -->
        <view class="profile-page__card card">
          <UserAvatar :avatar="profile.avatar" :name="profile.nickname || profile.username" :user-id="userId" :size="80" />
          <text class="profile-page__nickname">{{ profile.nickname || profile.username || '用户' }}</text>
          <text v-if="profile.school" class="profile-page__school">{{ profile.school }}{{ profile.major ? ' · ' + profile.major : '' }}</text>
          <text v-if="profile.bio" class="profile-page__bio">{{ profile.bio }}</text>
          <!-- 数据统计 -->
          <view class="profile-page__stats">
            <view class="profile-page__stat">
              <text class="profile-page__stat-num">{{ profile.postCount ?? 0 }}</text>
              <text class="profile-page__stat-label">帖子</text>
            </view>
            <view class="profile-page__stat profile-page__stat--link" @tap="goFollows('following')">
              <text class="profile-page__stat-num">{{ profile.followCount ?? 0 }}</text>
              <text class="profile-page__stat-label">关注</text>
            </view>
            <view class="profile-page__stat profile-page__stat--link" @tap="goFollows('fans')">
              <text class="profile-page__stat-num">{{ profile.fansCount ?? 0 }}</text>
              <text class="profile-page__stat-label">粉丝</text>
            </view>
            <view class="profile-page__stat">
              <text class="profile-page__stat-num">{{ profile.likedCount ?? 0 }}</text>
              <text class="profile-page__stat-label">获赞</text>
            </view>
          </view>
          <view v-if="!isSelf" class="profile-page__actions">
            <view
              class="profile-page__follow-btn"
              :class="{ 'profile-page__follow-btn--following': isFollowing }"
              @tap="toggleFollow"
            >
              <text class="profile-page__follow-btn-text">
                {{ isFollowing ? '已关注' : '+ 关注' }}
              </text>
            </view>
            <view class="profile-page__msg-btn" @tap="goChat">
              <text class="profile-page__msg-btn-text">发私信</text>
            </view>
          </view>
        </view>

        <!-- Tab Bar -->
        <view class="profile-page__tabs">
          <view
            class="profile-page__tab"
            :class="{ 'profile-page__tab--active': activeTab === 'post' }"
            @tap="switchTab('post')"
          >
            <text class="profile-page__tab-text">帖子</text>
          </view>
          <view
            class="profile-page__tab"
            :class="{ 'profile-page__tab--active': activeTab === 'trade' }"
            @tap="switchTab('trade')"
          >
            <text class="profile-page__tab-text">商品</text>
          </view>
        </view>

        <!-- Post List -->
        <view v-if="activeTab === 'post'" class="profile-page__list">
          <view v-if="postsLoading" class="profile-page__list-status">
            <text class="profile-page__list-status-text">加载中...</text>
          </view>
          <view v-else-if="posts.length === 0" class="profile-page__list-empty">
            <text class="profile-page__list-empty-text">暂无帖子</text>
          </view>
          <view v-else class="profile-page__list-items">
            <PostCard
              v-for="post in posts"
              :key="post.id"
              :post="post"
              @click="goPostDetail(post.id)"
            />
          </view>
        </view>

        <!-- Trade List -->
        <view v-if="activeTab === 'trade'" class="profile-page__list">
          <view v-if="tradesLoading" class="profile-page__list-status">
            <text class="profile-page__list-status-text">加载中...</text>
          </view>
          <view v-else-if="trades.length === 0" class="profile-page__list-empty">
            <text class="profile-page__list-empty-text">暂无商品</text>
          </view>
          <view v-else class="profile-page__list-items">
            <TradeCard
              v-for="trade in trades"
              :key="trade.id"
              :trade="trade"
              @click="goTradeDetail(trade.id)"
            />
          </view>
        </view>
      </template>

      <!-- Error -->
      <view v-else class="profile-page__error">
        <text class="profile-page__error-text">用户不存在</text>
      </view>

      <view class="profile-page__safe-bottom" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { userApi } from '@api/user'
import { useUserStore } from '@stores/user'
import PostCard from '@components/post/PostCard.vue'
import UserAvatar from '@components/common/UserAvatar.vue'

const userStore = useUserStore()

const userId = ref<number>(0)
const profile = ref<any>(null)
const loading = ref(true)
const isFollowing = ref(false)
const activeTab = ref<'post' | 'trade'>('post')
const posts = ref<any[]>([])
const trades = ref<any[]>([])
const postsLoading = ref(false)
const tradesLoading = ref(false)

const isSelf = computed(() => userStore.isLogin && userId.value === userStore.userId)

onLoad((options: any) => {
  // 兼容两种参数名：帖子/商品详情跳转传 id，通知中心传 userId
  userId.value = Number(options.userId || options.id) || 0
  fetchProfile()
  fetchPosts()
})

async function fetchProfile() {
  try {
    const data = await userApi.getUserById(userId.value)
    profile.value = data
    isFollowing.value = !!data?.isFollowed
  } catch {
    profile.value = null
  } finally {
    loading.value = false
  }
}

async function fetchPosts() {
  postsLoading.value = true
  try {
    const data = await userApi.getUserPosts(userId.value)
    posts.value = Array.isArray(data) ? data : (data?.list || data?.records || [])
  } catch {
    posts.value = []
  } finally {
    postsLoading.value = false
  }
}

async function fetchTrades() {
  tradesLoading.value = true
  try {
    const data = await userApi.getUserTrades(userId.value)
    trades.value = Array.isArray(data) ? data : (data?.list || data?.records || [])
  } catch {
    trades.value = []
  } finally {
    tradesLoading.value = false
  }
}

function switchTab(tab: 'post' | 'trade') {
  activeTab.value = tab
  if (tab === 'trade' && trades.value.length === 0 && !tradesLoading.value) {
    fetchTrades()
  }
}

async function toggleFollow() {
  if (!userStore.isLogin) {
    uni.showModal({
      title: '提示',
      content: '请先登录',
      success: (res) => {
        if (res.confirm) uni.navigateTo({ url: '/pages/auth/login' })
      },
    })
    return
  }
  try {
    if (isFollowing.value) {
      await userApi.unfollowUser(userId.value)
      isFollowing.value = false
      uni.showToast({ title: '已取消关注', icon: 'none' })
    } else {
      await userApi.followUser(userId.value)
      isFollowing.value = true
      uni.showToast({ title: '已关注', icon: 'success' })
    }
  } catch {
    // handled
  }
}

function goChat() {
  if (!userStore.isLogin) {
    uni.showModal({
      title: '提示',
      content: '请先登录',
      success: (res) => {
        if (res.confirm) uni.navigateTo({ url: '/pages/auth/login' })
      },
    })
    return
  }
  uni.navigateTo({
    url: `/pages/message/chat?peerId=${userId.value}&peerName=${encodeURIComponent(profile.value?.nickname || profile.value?.username || '用户')}`,
  })
}

function goPostDetail(id: number) {
  uni.navigateTo({ url: `/pages/post/detail?id=${id}` })
}

function goTradeDetail(id: number) {
  uni.navigateTo({ url: `/pages/trade/detail?id=${id}` })
}

function goFollows(type: 'following' | 'fans') {
  uni.navigateTo({ url: `/pages/user/follows?id=${userId.value}&type=${type}` })
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: var(--bg-secondary);
}

.profile-page__body {
  height: 100vh;
  padding: 16px;
}

/* ── Loading ── */
.profile-page__loading,
.profile-page__error {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
}

.profile-page__loading-text,
.profile-page__error-text {
  font-size: 15px;
  color: var(--ink-300);
}

/* ── Card ── */
.profile-page__card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 28px 20px;
  margin-bottom: 16px;
  text-align: center;
}

.profile-page__avatar {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14px;
}

.profile-page__avatar-text {
  color: #fff;
  font-size: 32px;
  font-weight: 700;
  font-family: var(--font-display);
}

.profile-page__nickname {
  font-size: 20px;
  font-weight: 700;
  color: var(--ink-900);
  font-family: var(--font-display);
  margin-bottom: 6px;
}

.profile-page__school {
  font-size: 13px;
  color: var(--ink-400);
  margin-bottom: 8px;
}

.profile-page__bio {
  font-size: 14px;
  color: var(--ink-500);
  margin-bottom: 12px;
  line-height: 1.6;
  max-width: 280px;
}

/* ── 数据统计 ── */
.profile-page__stats {
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-bottom: 16px;
}

.profile-page__stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.profile-page__stat-num {
  font-size: 17px;
  font-weight: 700;
  color: var(--ink-900);
  font-family: var(--font-mono);
}

.profile-page__stat-label {
  font-size: 11px;
  color: var(--ink-400);
}

.profile-page__stat--link:active {
  opacity: 0.6;
}

.profile-page__actions {
  display: flex;
  gap: 12px;
}

.profile-page__follow-btn {
  padding: 8px 24px;
  background: var(--accent-500);
  border-radius: var(--radius-full);
}

.profile-page__follow-btn:active {
  background: var(--accent-600);
  transform: scale(0.96);
}

.profile-page__follow-btn--following {
  background: var(--ink-100);
}

.profile-page__follow-btn--following:active {
  background: var(--ink-200);
}

.profile-page__follow-btn-text {
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}

.profile-page__follow-btn--following .profile-page__follow-btn-text {
  color: var(--ink-500);
}

.profile-page__msg-btn {
  padding: 8px 24px;
  background: var(--bg-primary);
  border: 2px solid var(--ink-200);
  border-radius: var(--radius-full);
}

.profile-page__msg-btn:active {
  background: var(--ink-50);
  transform: scale(0.96);
}

.profile-page__msg-btn-text {
  color: var(--ink-600);
  font-size: 14px;
  font-weight: 600;
}

/* ── Tabs ── */
.profile-page__tabs {
  display: flex;
  background: var(--bg-primary);
  border: 2px solid var(--ink-100);
  border-radius: var(--radius-lg);
  overflow: hidden;
  margin-bottom: 16px;
}

.profile-page__tab {
  flex: 1;
  padding: 12px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s ease;
}

.profile-page__tab:active {
  background: var(--ink-50);
}

.profile-page__tab--active {
  background: var(--accent-500);
}

.profile-page__tab--active:active {
  background: var(--accent-600);
}

.profile-page__tab-text {
  font-size: 15px;
  font-weight: 600;
  color: var(--ink-400);
}

.profile-page__tab--active .profile-page__tab-text {
  color: #fff;
}

/* ── List ── */
.profile-page__list {
  display: flex;
  flex-direction: column;
}

.profile-page__list-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.profile-page__list-status,
.profile-page__list-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.profile-page__list-status-text,
.profile-page__list-empty-text {
  font-size: 14px;
  color: var(--ink-300);
}

.profile-page__safe-bottom {
  height: 40px;
}
</style>
