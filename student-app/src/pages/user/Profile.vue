<template>
  <div class="profile-page page">
    <!-- 用户卡片 -->
    <div class="profile-card">
      <div class="profile-card__bg bg-ink-texture"></div>
      <div class="profile-card__info">
        <div class="avatar avatar-xl" :style="{ background: user.avatar ? 'transparent' : 'var(--accent-600)' }">
          <img v-if="user.avatar" :src="user.avatar" class="avatar-img" />
          <span v-else class="avatar-inner">{{ user.nickname?.charAt(0) || '?' }}</span>
        </div>
        <h2 class="profile-card__name">{{ user.nickname || '未登录' }}</h2>
        <p class="profile-card__school">{{ user.school || '请设置学校信息' }}</p>
        <!-- 数据统计 -->
        <div class="profile-stats">
          <div class="profile-stat">
            <span class="profile-stat__num">{{ user.postCount ?? 0 }}</span>
            <span class="profile-stat__label">帖子</span>
          </div>
          <div class="profile-stat profile-stat--link" @click="goFollows('following')">
            <span class="profile-stat__num">{{ user.followCount ?? 0 }}</span>
            <span class="profile-stat__label">关注</span>
          </div>
          <div class="profile-stat profile-stat--link" @click="goFollows('fans')">
            <span class="profile-stat__num">{{ user.fansCount ?? 0 }}</span>
            <span class="profile-stat__label">粉丝</span>
          </div>
          <div class="profile-stat">
            <span class="profile-stat__num">{{ user.likedCount ?? 0 }}</span>
            <span class="profile-stat__label">获赞</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 他人主页：关注 + 发私信 + 内容标签 -->
    <template v-if="!isSelf">
      <div v-if="isLogin" class="menu-section profile-actions">
        <button class="profile-action-btn" :class="{ 'profile-action-btn--followed': isFollowed }" @click="handleFollow">
          {{ isFollowed ? '已关注' : '+ 关注' }}
        </button>
        <button class="menu-item profile-action-chat" @click="goChat">
          <SvgIcon name="chat" :size="18" color="var(--ink-600)" />
          <span>发私信</span>
        </button>
      </div>

      <!-- 内容标签切换 -->
      <div class="profile-tabs">
        <button class="profile-tab" :class="{ 'profile-tab--active': activeTab === 'posts' }" @click="activeTab = 'posts'">
          <span>{{ userPosts.length }}</span> 帖子
        </button>
        <button class="profile-tab" :class="{ 'profile-tab--active': activeTab === 'trades' }" @click="activeTab = 'trades'">
          <span>{{ userTrades.length }}</span> 商品
        </button>
      </div>

      <!-- 帖子列表 -->
      <div v-if="activeTab === 'posts'" class="profile-content">
        <div v-if="postsLoading" class="profile-loading">
          <Loading variant="skeleton" :count="2" />
        </div>
        <EmptyState v-else-if="!userPosts.length" title="暂无帖子" description="Ta 还没有发布过帖子" :show-action="false" />
        <div v-else class="post-list">
          <PostCard
            v-for="post in userPosts"
            :key="post.id"
            :post="post"
            @click="goPostDetail(post.id)"
            @like="handleLike(post.id)"
            @preview="handlePreview"
          />
        </div>
      </div>

      <!-- 商品列表 -->
      <div v-if="activeTab === 'trades'" class="profile-content">
        <div v-if="tradesLoading" class="profile-loading">
          <Loading variant="skeleton" :count="2" />
        </div>
        <EmptyState v-else-if="!userTrades.length" title="暂无商品" description="Ta 还没有发布过商品" :show-action="false" />
        <div v-else class="trade-list">
          <TradeCard
            v-for="trade in userTrades"
            :key="trade.id"
            :trade="trade"
            @click="goTradeDetail(trade.id)"
          />
        </div>
      </div>
    </template>

    <!-- 功能菜单（仅自己可见） -->
    <template v-if="isSelf">
      <div class="menu-section">
        <router-link to="/user-profile/posts" class="menu-item">
          <SvgIcon name="file-text" :size="18" color="var(--ink-600)" />
          <span>我的帖子</span>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--ink-300)" stroke-width="2.5"><polyline points="9 18 15 12 9 6"/></svg>
        </router-link>
        <router-link to="/user-profile/trades" class="menu-item">
          <SvgIcon name="shopping-cart" :size="18" color="var(--ink-600)" />
          <span>我的商品</span>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--ink-300)" stroke-width="2.5"><polyline points="9 18 15 12 9 6"/></svg>
        </router-link>
        <router-link to="/user-profile/favorites" class="menu-item">
          <SvgIcon name="star" :size="18" color="var(--ink-600)" />
          <span>我的收藏</span>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--ink-300)" stroke-width="2.5"><polyline points="9 18 15 12 9 6"/></svg>
        </router-link>
      </div>

      <div class="menu-section">
        <router-link to="/user-profile/edit" class="menu-item">
          <SvgIcon name="edit" :size="18" color="var(--ink-600)" />
          <span>编辑资料</span>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--ink-300)" stroke-width="2.5"><polyline points="9 18 15 12 9 6"/></svg>
        </router-link>
        <router-link to="/user-profile/settings" class="menu-item">
          <SvgIcon name="settings" :size="18" color="var(--ink-600)" />
          <span>设置</span>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--ink-300)" stroke-width="2.5"><polyline points="9 18 15 12 9 6"/></svg>
        </router-link>
      </div>

      <button
        v-if="isLogin"
        class="logout-btn"
        @click="handleLogout"
      >
        退出登录
      </button>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog, showImagePreview } from 'vant'
import { useUserStore } from '@stores/user'
import { getUserProfile, getUserById, getUserPosts, getUserTrades, followUser, unfollowUser } from '@api/user'
import { togglePostLike } from '@api'
import PostCard from '@components/post/PostCard.vue'
import TradeCard from '@components/trade/TradeCard.vue'
import Loading from '@components/common/Loading.vue'
import EmptyState from '@components/common/EmptyState.vue'
import SvgIcon from '@components/common/SvgIcon.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isLogin = computed(() => userStore.isLogin)
const user = ref(userStore.userInfo || {})

const profileUserId = computed(() => route.params.id ? Number(route.params.id) : null)
const isSelf = computed(() => !profileUserId.value || profileUserId.value === userStore.userId)

const activeTab = ref('posts')
const userPosts = ref([])
const userTrades = ref([])
const postsLoading = ref(false)
const tradesLoading = ref(false)
const isFollowed = ref(false)

async function fetchUserPosts() {
  if (!profileUserId.value) return
  postsLoading.value = true
  try {
    const data = await getUserPosts(profileUserId.value, { page: 1, size: 50 })
    userPosts.value = data.list || []
  } catch { userPosts.value = [] }
  finally { postsLoading.value = false }
}

async function fetchUserTrades() {
  if (!profileUserId.value) return
  tradesLoading.value = true
  try {
    const data = await getUserTrades(profileUserId.value, { page: 1, size: 50 })
    userTrades.value = data.list || []
  } catch { userTrades.value = [] }
  finally { tradesLoading.value = false }
}

onMounted(async () => {
  if (profileUserId.value && !isSelf.value) {
    // 查看他人主页
    try {
      user.value = await getUserById(profileUserId.value)
      isFollowed.value = !!user.value.isFollowed
    } catch { /* ignore */ }
    // 并行加载帖子和商品
    await Promise.all([fetchUserPosts(), fetchUserTrades()])
  } else if (isLogin.value) {
    // 自己的主页
    try {
      const data = await getUserProfile()
      user.value = data
      userStore.setUserInfo(data)
    } catch { /* ignore */ }
  }
})

async function handleFollow() {
  if (!profileUserId.value) return
  try {
    if (isFollowed.value) {
      await unfollowUser(profileUserId.value)
      isFollowed.value = false
      user.value.fansCount = Math.max(0, (user.value.fansCount || 0) - 1)
    } else {
      await followUser(profileUserId.value)
      isFollowed.value = true
      user.value.fansCount = (user.value.fansCount || 0) + 1
    }
  } catch { /* 错误已在请求拦截器中处理 */ }
}

function goFollows(type) {
  const base = profileUserId.value ? `/user-profile/${profileUserId.value}/follows` : '/user-profile/follows'
  router.push({ path: base, query: { type } })
}

async function handleLike(postId) {
  try {
    await togglePostLike(postId)
    const post = userPosts.value.find((p) => p.id === postId)
    if (post) {
      post.isLiked = !post.isLiked
      post.likes += post.isLiked ? 1 : -1
    }
  } catch { /* ignore */ }
}

function handlePreview(images, index) {
  if (images?.length) {
    showImagePreview({ images, startPosition: index, closeable: true })
  }
}

function goPostDetail(id) { router.push(`/post/${id}`) }
function goTradeDetail(id) { router.push(`/trade-item/${id}`) }

function goChat() {
  if (profileUserId.value) {
    router.push(`/chat/${profileUserId.value}`)
  }
}

function handleLogout() {
  showConfirmDialog({ title: '提示', message: '确定要退出登录吗？' }).then(() => {
    userStore.logout()
    router.push('/auth/login')
  }).catch(() => {})
}
</script>

<style scoped>
.profile-page {
  background: var(--bg-secondary);
}

/* ── 用户卡片 ── */
.profile-card {
  position: relative;
  overflow: hidden;
  background: var(--bg-primary);
}

.profile-card__bg {
  height: 130px;
  position: relative;
}

.profile-card__bg::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--accent-500), var(--amber-500), var(--coral-500));
}

.profile-card__info {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: -44px;
  position: relative;
  z-index: 1;
}

.avatar-inner {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #fff;
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: var(--font-black);
  border-radius: var(--radius-full);
  border: 3px solid var(--bg-primary);
  box-shadow: 0 2px 0 0 rgba(26, 28, 36, 0.15);
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid var(--bg-primary);
  box-shadow: 0 2px 0 0 rgba(26, 28, 36, 0.15);
}

.profile-card__name {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  margin-top: var(--space-3);
  color: var(--ink-900);
}

.profile-card__school {
  font-size: var(--text-sm);
  color: var(--ink-400);
  margin-top: 2px;
}

/* ── 数据统计 ── */
.profile-stats {
  display: flex;
  justify-content: center;
  gap: var(--space-6);
  padding: var(--space-3) var(--space-4) var(--space-5);
}

.profile-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.profile-stat__num {
  font-family: var(--font-mono);
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--ink-900);
}

.profile-stat__label {
  font-size: var(--text-2xs);
  color: var(--ink-400);
}

.profile-stat--link {
  cursor: pointer;
}

.profile-stat--link:active {
  opacity: 0.6;
}

/* ── 关注/私信操作 ── */
.profile-actions {
  display: flex;
  gap: var(--space-2);
  align-items: stretch;
}

.profile-action-btn {
  flex-shrink: 0;
  padding: 0 24px;
  border-radius: var(--radius-md);
  border: 2px solid var(--accent-600);
  background: var(--accent-600);
  color: #fff;
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  transition: all var(--transition-fast);
}

.profile-action-btn:active {
  transform: scale(0.95);
}

.profile-action-btn--followed {
  background: var(--bg-primary);
  border-color: var(--ink-200);
  color: var(--ink-400);
  font-weight: var(--font-medium);
}

.profile-action-chat {
  flex: 1;
  justify-content: center;
}

/* ── 标签切换 ── */
.profile-tabs {
  display: flex;
  margin: var(--space-3) var(--content-padding) 0;
  background: var(--bg-primary);
  border: 2px solid var(--ink-100);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.profile-tab {
  flex: 1;
  padding: var(--space-3);
  text-align: center;
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--ink-500);
  transition: all var(--transition-fast);
  border-bottom: 2px solid transparent;
}

.profile-tab span {
  font-family: var(--font-mono);
  font-weight: var(--font-bold);
  margin-right: 2px;
}

.profile-tab--active {
  color: var(--ink-900);
  border-bottom-color: var(--accent-600);
  background: var(--accent-50);
}

.profile-content {
  margin: 0 var(--content-padding);
  padding-top: var(--space-3);
}

.profile-loading {
  margin-top: var(--space-2);
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  padding-bottom: var(--space-4);
}

.trade-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  padding-bottom: var(--space-4);
}

/* ── 菜单 ── */
.menu-section {
  margin: var(--space-3) var(--content-padding);
  background: var(--bg-primary);
  border: 2px solid var(--ink-100);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  font-size: var(--text-base);
  font-weight: var(--font-medium);
  color: var(--ink-700);
  border-bottom: 1px solid var(--ink-50);
  text-decoration: none;
  transition: all var(--transition-fast);
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:active {
  background: var(--ink-50);
}

.menu-item svg:last-child {
  margin-left: auto;
}

/* ── 退出按钮 ── */
.logout-btn {
  display: block;
  width: calc(100% - 2 * var(--content-padding));
  margin: var(--space-6) var(--content-padding);
  padding: 12px;
  text-align: center;
  border: 2px solid var(--ink-200);
  border-radius: var(--radius-lg);
  background: var(--bg-primary);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--ink-400);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.logout-btn:active {
  background: var(--ink-50);
  border-color: var(--error);
  color: var(--error);
}
</style>
