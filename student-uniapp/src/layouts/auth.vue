<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    title?: string
    subtitle?: string
    showBack?: boolean
  }>(),
  {
    title: 'PE-Student',
    subtitle: '校园互助平台',
    showBack: true,
  },
)

const pages = getCurrentPages()
const canBack = computed(() => props.showBack && pages.length > 1)

function goBack() {
  uni.navigateBack()
}
</script>

<template>
  <view class="layout-auth">
    <!-- 装饰性渐变光球 -->
    <view class="auth-decor">
      <view class="auth-orb auth-orb--1" />
      <view class="auth-orb auth-orb--2" />
      <view class="auth-orb auth-orb--3" />
    </view>

    <!-- 返回按钮 -->
    <view class="auth-back" v-if="canBack" @tap="goBack">
      <wd-icon name="arrow-left" size="20" color="#fff" />
    </view>

    <!-- 内容区域 -->
    <view class="auth-content">
      <!-- 品牌标识 -->
      <view class="auth-brand">
        <view class="auth-brand__logo">
          <wd-icon name="edit" size="32" color="#fff" />
        </view>
        <text class="auth-brand__title">{{ title }}</text>
        <text class="auth-brand__subtitle">{{ subtitle }}</text>
      </view>

      <!-- 插槽内容 -->
      <view class="auth-slot">
        <slot />
      </view>
    </view>

    <!-- 底部版权 -->
    <view class="auth-footer">
      <text class="auth-footer__text">PE-Student &copy; 2025</text>
    </view>
  </view>
</template>

<style scoped>
.layout-auth {
  min-height: 100vh;
  background: var(--ink-900);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
  position: relative;
  overflow: hidden;
}

/* ── 装饰性渐变光球 ── */
.auth-decor {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.auth-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.15;
}

.auth-orb--1 {
  width: 320px;
  height: 320px;
  background: var(--accent-500);
  top: -80px;
  right: -60px;
  animation: orbFloat1 8s ease-in-out infinite;
}

.auth-orb--2 {
  width: 200px;
  height: 200px;
  background: #6366f1;
  bottom: 15%;
  left: -40px;
  animation: orbFloat2 10s ease-in-out infinite;
}

.auth-orb--3 {
  width: 240px;
  height: 240px;
  background: var(--accent-400);
  top: 35%;
  right: -80px;
  animation: orbFloat3 12s ease-in-out infinite;
}

@keyframes orbFloat1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-20px, 30px) scale(1.08); }
}

@keyframes orbFloat2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(25px, -25px) scale(1.1); }
}

@keyframes orbFloat3 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-15px, -20px) scale(0.95); }
}

/* ── 返回按钮 ── */
.auth-back {
  position: absolute;
  top: 16px;
  left: 16px;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

.auth-back__icon {
  font-size: 22px;
  color: var(--ink-200);
  font-weight: 700;
}

/* ── 内容区域 ── */
.auth-content {
  width: 100%;
  max-width: 360px;
  display: flex;
  flex-direction: column;
  align-items: center;
  z-index: 1;
}

/* ── 品牌标识 ── */
.auth-brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 40px;
}

.auth-brand__logo {
  width: 72px;
  height: 72px;
  border-radius: 20px;
  background: linear-gradient(135deg, var(--accent-500), var(--accent-400));
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  box-shadow: 0 4px 20px rgba(7, 193, 96, 0.35);
}

.auth-brand__icon {
  font-size: 32px;
  color: #fff;
}

.auth-brand__title {
  font-size: 28px;
  font-weight: 800;
  color: #fff;
  letter-spacing: -0.5px;
}

.auth-brand__subtitle {
  font-size: 14px;
  color: var(--ink-400);
  margin-top: 6px;
  letter-spacing: 1px;
}

/* ── 插槽内容 ── */
.auth-slot {
  width: 100%;
}

/* ── 底部版权 ── */
.auth-footer {
  position: absolute;
  bottom: 40px;
  z-index: 1;
}

.auth-footer__text {
  font-size: 12px;
  color: var(--ink-500);
}
</style>
