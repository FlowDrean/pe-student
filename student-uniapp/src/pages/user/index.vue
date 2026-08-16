<template>
  <tabbar>
    <view class="user-page">
    <!-- Header -->
    <view class="user-page__header">
      <text class="user-page__title">我的</text>
    </view>

    <scroll-view class="user-page__body" scroll-y>
      <!-- User Card -->
      <view v-if="userStore.isLogin" class="user-page__card card">
        <view class="user-page__card-row" @tap="goEditProfile">
          <UserAvatar :avatar="userInfo?.avatar" :name="userStore.nickname" :user-id="userStore.userId || 0" :size="56" />
          <view class="user-page__info">
            <text class="user-page__nickname">{{ userStore.nickname }}</text>
            <text class="user-page__school">{{ userInfo?.school || '未设置学校' }} · {{ userInfo?.major || '未设置专业' }}</text>
          </view>
          <text class="user-page__edit-link">编辑 </text>
          <wd-icon name="chevron-right" size="14" color="var(--accent-500)" />
        </view>
      </view>

      <!-- Login Prompt -->
      <view v-else class="user-page__card card user-page__login-card" @tap="goLogin">
        <view class="user-page__login-avatar">
          <text class="user-page__login-avatar-text">?</text>
        </view>
        <view class="user-page__login-info">
          <text class="user-page__login-text">登录 / 注册</text>
          <text class="user-page__login-desc">登录后享受完整功能</text>
        </view>
        <wd-icon name="arrow-right" size="18" color="var(--ink-300)" />
      </view>

      <!-- Menu Section 1: Content -->
      <view class="user-page__menu card">
        <view class="user-page__menu-item" @tap="goPosts">
          <wd-icon name="edit" size="20" />
          <text class="user-page__menu-label">我的帖子</text>
          <wd-icon name="chevron-right" size="16" color="var(--ink-300)" />
        </view>
        <view class="user-page__menu-divider" />
        <view class="user-page__menu-item" @tap="goTrades">
          <wd-icon name="cart" size="20" />
          <text class="user-page__menu-label">我的商品</text>
          <wd-icon name="chevron-right" size="16" color="var(--ink-300)" />
        </view>
        <view class="user-page__menu-divider" />
        <view class="user-page__menu-item" @tap="goFavorites">
          <wd-icon name="star" size="20" />
          <text class="user-page__menu-label">我的收藏</text>
          <wd-icon name="chevron-right" size="16" color="var(--ink-300)" />
        </view>
      </view>

      <!-- Menu Section 2: Settings -->
      <view class="user-page__menu card">
        <view class="user-page__menu-item" @tap="goEditProfile">
          <wd-icon name="setting" size="20" />
          <text class="user-page__menu-label">编辑资料</text>
          <wd-icon name="chevron-right" size="16" color="var(--ink-300)" />
        </view>
        <view class="user-page__menu-divider" />
        <view class="user-page__menu-item" @tap="goSettings">
          <wd-icon name="tools" size="20" />
          <text class="user-page__menu-label">设置</text>
          <wd-icon name="chevron-right" size="16" color="var(--ink-300)" />
        </view>
      </view>

      <!-- Logout -->
      <view v-if="userStore.isLogin" class="user-page__logout-btn" @tap="handleLogout">
        <text class="user-page__logout-text">退出登录</text>
      </view>

      <view class="user-page__safe-bottom" />
    </scroll-view>
  </view>
  </tabbar>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@stores/user'
import { userApi } from '@api/user'
import tabbar from '@layouts/tabbar.vue'
import UserAvatar from '@components/common/UserAvatar.vue'

const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)

async function refreshProfile() {
  if (!userStore.isLogin) return
  try {
    const data = await userApi.getProfile()
    if (data) {
      userStore.setUserInfo(data)
    }
  } catch {
    // silent
  }
}

onMounted(() => {
  refreshProfile()
})

onShow(() => {
  if (userStore.isLogin) {
    refreshProfile()
  }
})

function goLogin() {
  uni.navigateTo({ url: '/pages/auth/login' })
}

function goPosts() {
  if (!checkLogin()) return
  uni.navigateTo({ url: '/pages/user/my-posts' })
}

function goTrades() {
  if (!checkLogin()) return
  uni.navigateTo({ url: '/pages/user/my-trades' })
}

function goFavorites() {
  if (!checkLogin()) return
  uni.navigateTo({ url: '/pages/user/favorites' })
}

function goEditProfile() {
  if (!checkLogin()) return
  uni.navigateTo({ url: '/pages/user/edit-profile' })
}

function goSettings() {
  uni.navigateTo({ url: '/pages/user/settings' })
}

function handleLogout() {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
        uni.showToast({ title: '已退出登录', icon: 'success' })
      }
    },
  })
}

function checkLogin(): boolean {
  if (!userStore.isLogin) {
    uni.showModal({
      title: '提示',
      content: '请先登录',
      success: (res) => {
        if (res.confirm) {
          uni.navigateTo({ url: '/pages/auth/login' })
        }
      },
    })
    return false
  }
  return true
}
</script>

<style scoped>
.user-page {
  flex: 1;
  background: var(--bg-secondary);
  display: flex;
  flex-direction: column;
}

/* ── Header ── */
.user-page__header {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  background: var(--bg-primary);
  border-bottom: 2px solid var(--ink-100);
}

.user-page__title {
  font-size: 17px;
  font-weight: 700;
  color: var(--ink-900);
}

/* ── Body ── */
.user-page__body {
  flex: 1;
  padding: 16px;
}

/* ── User Card ── */
.user-page__card {
  padding: 20px 16px;
  margin-bottom: 16px;
}

.user-page__card-row {
  display: flex;
  align-items: center;
  gap: 14px;
}

.user-page__avatar {
  width: 60px;
  height: 60px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.user-page__avatar-text {
  color: #fff;
  font-size: 24px;
  font-weight: 700;
  font-family: var(--font-display);
}

.user-page__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-page__nickname {
  font-size: 18px;
  font-weight: 700;
  color: var(--ink-900);
  font-family: var(--font-display);
}

.user-page__school {
  font-size: 13px;
  color: var(--ink-400);
}

.user-page__edit-link {
  font-size: 14px;
  color: var(--accent-500);
  font-weight: 600;
  flex-shrink: 0;
}

/* ── Login Card ── */
.user-page__login-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px 16px;
}

.user-page__login-avatar {
  width: 60px;
  height: 60px;
  border-radius: var(--radius-full);
  background: var(--ink-100);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.user-page__login-avatar-text {
  font-size: 24px;
  font-weight: 700;
  color: var(--ink-300);
}

.user-page__login-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-page__login-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--ink-600);
}

.user-page__login-desc {
  font-size: 13px;
  color: var(--ink-300);
}

.user-page__login-arrow {
  font-size: 18px;
  color: var(--ink-300);
  font-weight: 700;
}

/* ── Menu ── */
.user-page__menu {
  margin-bottom: 16px;
  overflow: hidden;
}

.user-page__menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  transition: background 0.15s ease;
}

.user-page__menu-item:active {
  background: var(--ink-50);
}

.user-page__menu-icon {
  font-size: 18px;
}

.user-page__menu-label {
  flex: 1;
  font-size: 15px;
  font-weight: 500;
  color: var(--ink-700);
}

.user-page__menu-arrow {
  font-size: 16px;
  color: var(--ink-300);
}

.user-page__menu-divider {
  height: 1px;
  background: var(--ink-50);
  margin: 0 16px;
}

/* ── Logout ── */
.user-page__logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  margin-top: 8px;
  margin-bottom: 16px;
  background: var(--bg-primary);
  border: 2px solid var(--ink-100);
  border-radius: var(--radius-lg);
}

.user-page__logout-btn:active {
  background: #fef2f2;
  border-color: #fecaca;
}

.user-page__logout-text {
  font-size: 15px;
  font-weight: 600;
  color: var(--error);
}

.user-page__safe-bottom {
  height: 24px;
}
</style>
