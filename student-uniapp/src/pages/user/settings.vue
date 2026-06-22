<template>
  <view class="settings-page">
    <scroll-view class="settings-page__body" scroll-y>
      <!-- General Section -->
      <view class="settings-page__section-label">
        <text class="settings-page__section-label-text">通用</text>
      </view>
      <view class="settings-page__menu card">
        <!-- Message Notifications -->
        <view class="settings-page__menu-item">
          <view class="settings-page__menu-item-left">
            <text class="settings-page__menu-item-label">消息通知</text>
            <text class="settings-page__menu-item-desc">接收新消息提醒</text>
          </view>
          <switch
            :checked="notifyEnabled"
            color="#07C160"
            @change="toggleNotify"
          />
        </view>

        <view class="settings-page__divider" />

        <!-- Clear Cache -->
        <view class="settings-page__menu-item" @tap="clearCache">
          <view class="settings-page__menu-item-left">
            <text class="settings-page__menu-item-label">清除缓存</text>
            <text class="settings-page__menu-item-desc">清除本地缓存数据</text>
          </view>
          <wd-icon name="chevron-right" size="16" color="var(--ink-300)" />
        </view>
      </view>

      <!-- Privacy Section -->
      <view class="settings-page__section-label">
        <text class="settings-page__section-label-text">隐私</text>
      </view>
      <view class="settings-page__menu card">
        <view class="settings-page__menu-item" @tap="goPrivacy">
          <text class="settings-page__menu-item-label">隐私设置</text>
          <wd-icon name="chevron-right" size="16" color="var(--ink-300)" />
        </view>

        <view class="settings-page__divider" />

        <view class="settings-page__menu-item" @tap="goUserAgreement">
          <text class="settings-page__menu-item-label">用户协议</text>
          <wd-icon name="chevron-right" size="16" color="var(--ink-300)" />
        </view>
      </view>

      <!-- About Section -->
      <view class="settings-page__section-label">
        <text class="settings-page__section-label-text">关于</text>
      </view>
      <view class="settings-page__menu card">
        <view class="settings-page__menu-item">
          <text class="settings-page__menu-item-label">版本号</text>
          <text class="settings-page__menu-value">v1.0.0</text>
        </view>

        <view class="settings-page__divider" />

        <view class="settings-page__menu-item">
          <text class="settings-page__menu-item-label">应用名称</text>
          <text class="settings-page__menu-value">PE-Student</text>
        </view>

        <view class="settings-page__divider" />

        <view class="settings-page__menu-item" @tap="checkUpdate">
          <text class="settings-page__menu-item-label">检查更新</text>
          <wd-icon name="chevron-right" size="16" color="var(--ink-300)" />
        </view>
      </view>

      <!-- Footer -->
      <view class="settings-page__footer">
        <text class="settings-page__footer-text">PE-Student 校园互助平台</text>
        <text class="settings-page__footer-sub">&#169; 2024 PE-Student Team</text>
      </view>

      <view class="settings-page__safe-bottom" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import storage from '@utils/storage'

const notifyEnabled = ref(true)

function toggleNotify(e: any) {
  notifyEnabled.value = e.detail.value
  storage.set('message_notify', notifyEnabled.value)
  uni.showToast({
    title: notifyEnabled.value ? '已开启消息通知' : '已关闭消息通知',
    icon: 'none',
  })
}

function clearCache() {
  uni.showModal({
    title: '清除缓存',
    content: '确定要清除所有本地缓存数据吗？这将不会影响你的账号数据。',
    success: (res) => {
      if (res.confirm) {
        const token = uni.getStorageSync('student_app_token')
        const userStoreData = uni.getStorageSync('user')
        storage.clear()
        if (token) uni.setStorageSync('student_app_token', token)
        if (userStoreData) uni.setStorageSync('user', userStoreData)
        uni.showToast({ title: '缓存已清除', icon: 'success' })
      }
    },
  })
}

function goPrivacy() {
  uni.showToast({ title: '隐私设置开发中', icon: 'none' })
}

function goUserAgreement() {
  uni.showToast({ title: '用户协议开发中', icon: 'none' })
}

function checkUpdate() {
  uni.showToast({ title: '当前已是最新版本', icon: 'none' })
}
</script>

<style scoped>
.settings-page {
  min-height: 100vh;
  background: var(--bg-secondary);
}

.settings-page__body {
  height: 100vh;
  padding: 16px;
}

.settings-page__section-label {
  padding: 8px 4px 8px;
  margin-top: 4px;
}

.settings-page__section-label:first-child {
  margin-top: 0;
}

.settings-page__section-label-text {
  font-size: 12px;
  font-weight: 700;
  color: var(--ink-400);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.settings-page__menu {
  margin-bottom: 16px;
  overflow: hidden;
}

.settings-page__menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  transition: background 0.15s ease;
}

.settings-page__menu-item:active {
  background: var(--ink-50);
}

.settings-page__menu-item-left {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
}

.settings-page__menu-item-label {
  font-size: 15px;
  font-weight: 500;
  color: var(--ink-700);
}

.settings-page__menu-item-desc {
  font-size: 12px;
  color: var(--ink-300);
}

.settings-page__menu-arrow {
  font-size: 18px;
  color: var(--ink-300);
  flex-shrink: 0;
}

.settings-page__menu-value {
  font-size: 14px;
  color: var(--ink-400);
  flex-shrink: 0;
}

.settings-page__divider {
  height: 1px;
  background: var(--ink-50);
  margin: 0 16px;
}

.settings-page__footer {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 40px 0 20px;
}

.settings-page__footer-text {
  font-size: 14px;
  color: var(--ink-300);
  font-weight: 500;
}

.settings-page__footer-sub {
  font-size: 12px;
  color: var(--ink-200);
}

.settings-page__safe-bottom {
  height: 40px;
}
</style>
