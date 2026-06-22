<script setup lang="ts">
import { computed } from 'vue'
import { useMessageStore } from '@stores/message'
import IconChar from '@components/common/IconChar.vue'

const messageStore = useMessageStore()

interface Tab {
  key: string
  label: string
  icon: string
  path: string
  special?: boolean
}

const tabs: Tab[] = [
  { key: 'home', label: '首页', icon: 'home', path: '/pages/index/index' },
  { key: 'trade', label: '市场', icon: 'shop', path: '/pages/trade/index' },
  { key: 'create', label: '发布', icon: 'plus', path: '/pages/post/create', special: true },
  { key: 'message', label: '消息', icon: 'chat', path: '/pages/message/index' },
  { key: 'user', label: '我的', icon: 'user', path: '/pages/user/index' },
]

const badge = computed(() => messageStore.totalUnread)

const currentPages = getCurrentPages()
const currentPath = computed(() => {
  const page = currentPages[currentPages.length - 1]
  return page ? `/${page.route}` : ''
})

function isActive(key: string): boolean {
  const path = currentPath.value
  if (key === 'home') return path === '/pages/index/index'
  if (key === 'trade') return path.startsWith('/pages/trade')
  if (key === 'create') return path === '/pages/post/create'
  if (key === 'message') return path.startsWith('/pages/message')
  if (key === 'user') return path.startsWith('/pages/user')
  return false
}

function handleTabClick(tab: Tab) {
  if (isActive(tab.key)) return
  uni.switchTab({ url: tab.path })
}
</script>

<template>
  <view class="tab-layout">
    <!-- 内容区域 -->
    <view class="tab-layout__content">
      <slot />
    </view>

    <!-- 自定义底部导航栏 -->
    <view class="tab-bar safe-area-bottom">
      <view class="tab-bar__inner">
        <view
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-bar__item"
          :class="{
            'tab-bar__item--active': isActive(tab.key),
            'tab-bar__item--special': tab.special,
          }"
          @tap="handleTabClick(tab)"
        >
          <!-- 发布按钮（特殊样式） -->
          <view v-if="tab.special" class="tab-bar__publish">
            <text class="tab-bar__publish-icon">+</text>
          </view>

          <template v-else>
            <!-- 图标 -->
            <view class="tab-bar__icon">
              <IconChar :name="tab.icon" size="22" :color="isActive(tab.key) ? 'var(--accent-500)' : 'var(--ink-400)'" />
            </view>

            <!-- 文字 -->
            <text class="tab-bar__label">{{ tab.label }}</text>

            <!-- 激活指示器 -->
            <view v-if="isActive(tab.key)" class="tab-bar__indicator" />

            <!-- 消息红点 -->
            <view
              v-if="tab.key === 'message' && badge > 0"
              class="tab-bar__badge"
            >
              <text class="tab-bar__badge-text">
                {{ badge > 99 ? '99+' : badge }}
              </text>
            </view>
          </template>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.tab-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--bg-secondary);
}

.tab-layout__content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  /* 底部导航高度 50px + 边框 4px + 安全区 */
  padding-bottom: calc(56px + env(safe-area-inset-bottom, 0px));
}

/* ============================================
   底部导航 — 全宽贴底 TabBar
   ============================================ */

.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-top: 2px solid var(--ink-100);
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.06);
}

.tab-bar__inner {
  display: flex;
  align-items: center;
  justify-content: space-around;
  height: 50px;
  max-width: 480px;
  margin: 0 auto;
  position: relative;
}

/* ── 单个标签项 ── */
.tab-bar__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  height: 100%;
  position: relative;
  transition: transform 0.2s ease;
}

.tab-bar__item:active {
  transform: scale(0.94);
}

/* ── 图标 ── */
.tab-bar__icon {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 2px;
}

.tab-bar__icon-char {
  font-size: 20px;
  line-height: 1;
}

/* ── 文字 ── */
.tab-bar__label {
  font-size: 10px;
  font-weight: 500;
  color: var(--ink-400);
  transition: color 0.2s ease;
}

.tab-bar__item--active .tab-bar__label {
  color: var(--accent-500);
  font-weight: 700;
}

/* ── 激活指示器 ── */
.tab-bar__indicator {
  position: absolute;
  bottom: 4px;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--accent-500);
}

/* ── 发布按钮 ── */
.tab-bar__publish {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--accent-500);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(7, 193, 96, 0.4);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  margin-top: -8px;
}

.tab-bar__publish:active {
  transform: scale(0.92);
  box-shadow: 0 2px 8px rgba(7, 193, 96, 0.3);
}

.tab-bar__publish-icon {
  font-size: 24px;
  color: #fff;
  font-weight: 300;
  line-height: 1;
}

/* ── 消息红点 ── */
.tab-bar__badge {
  position: absolute;
  top: 0;
  right: 50%;
  transform: translateX(18px);
  min-width: 16px;
  height: 16px;
  padding: 0 5px;
  border-radius: 8px;
  background: var(--error);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1.5px solid #fff;
}

.tab-bar__badge-text {
  font-size: 10px;
  color: #fff;
  font-weight: 700;
  line-height: 1;
}

/* ── 安全区 ── */
.safe-area-bottom {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
