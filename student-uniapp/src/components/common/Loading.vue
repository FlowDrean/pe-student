<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    variant?: 'skeleton' | 'spinner' | 'fullpage'
    count?: number
    text?: string
  }>(),
  {
    variant: 'spinner',
    count: 3,
    text: '加载中...',
  },
)

const skeletonLines = computed(() => Array.from({ length: props.count }, (_, i) => i))
</script>

<template>
  <!-- 骨架屏 -->
  <view v-if="variant === 'skeleton'" class="loading-skeleton">
    <view class="skeleton-header">
      <view class="skeleton-avatar" />
      <view class="skeleton-info">
        <view class="skeleton-line skeleton-line--title" />
        <view class="skeleton-line skeleton-line--sub" />
      </view>
    </view>
    <view v-for="i in skeletonLines" :key="i" class="skeleton-body">
      <view class="skeleton-line skeleton-line--full" />
      <view class="skeleton-line skeleton-line--partial" />
    </view>
  </view>

  <!-- 旋转加载器 -->
  <view v-else-if="variant === 'spinner'" class="loading-spinner">
    <view class="spinner-ring">
      <view class="spinner-arc" />
    </view>
  </view>

  <!-- 全屏加载 -->
  <view v-else class="loading-fullpage">
    <view class="loading-fullpage__mask">
      <view class="loading-fullpage__card">
        <view class="spinner-ring">
          <view class="spinner-arc" />
        </view>
        <text class="loading-fullpage__text">{{ text }}</text>
      </view>
    </view>
  </view>
</template>

<style scoped>
/* ============================================
   Skeleton 骨架屏
   ============================================ */

.loading-skeleton {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.skeleton-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.skeleton-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--ink-100);
  animation: shimmer 1.5s ease-in-out infinite;
  flex-shrink: 0;
}

.skeleton-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.skeleton-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.skeleton-line {
  height: 14px;
  border-radius: var(--radius-sm);
  background: var(--ink-100);
  animation: shimmer 1.5s ease-in-out infinite;
}

.skeleton-line--title {
  width: 50%;
  height: 16px;
}

.skeleton-line--sub {
  width: 30%;
  height: 12px;
}

.skeleton-line--full {
  width: 100%;
}

.skeleton-line--partial {
  width: 65%;
}

@keyframes shimmer {
  0% { opacity: 1; }
  50% { opacity: 0.4; }
  100% { opacity: 1; }
}

/* ============================================
   Spinner 旋转加载器
   ============================================ */

.loading-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.spinner-ring {
  width: 28px;
  height: 28px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.spinner-arc {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 3px solid var(--ink-100);
  border-top-color: var(--accent-500);
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ============================================
   Fullpage 全屏加载
   ============================================ */

.loading-fullpage__mask {
  position: fixed;
  inset: 0;
  background: rgba(255, 255, 255, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

.loading-fullpage__card {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  border: 2px solid var(--ink-100);
  box-shadow: var(--shadow-md);
  padding: 32px 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.loading-fullpage__text {
  font-size: 14px;
  color: var(--ink-500);
  font-weight: 500;
}
</style>
