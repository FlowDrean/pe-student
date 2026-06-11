<template>
  <nav class="tab-bar">
    <div class="tab-bar__inner">
      <router-link
        v-for="tab in tabs"
        :key="tab.key"
        :to="tab.path"
        class="tab-bar__item"
        :class="{
          'tab-bar__item--active': isActive(tab.key),
          'tab-bar__item--special': tab.special,
        }"
        @click="handleTabClick(tab)"
      >
        <!-- 发布按钮（特殊样式） -->
        <div v-if="tab.special" class="tab-bar__publish">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.8" stroke-linecap="round">
            <line x1="12" y1="5" x2="12" y2="19" />
            <line x1="5" y1="12" x2="19" y2="12" />
          </svg>
        </div>

        <template v-else>
          <!-- 图标 -->
          <div class="tab-bar__icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" :stroke="isActive(tab.key) ? 'var(--accent-600)' : 'var(--ink-400)'" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <!-- Home -->
              <template v-if="tab.key === 'home'">
                <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z" />
                <polyline points="9 22 9 12 15 12 15 22" />
              </template>
              <!-- Trade -->
              <template v-else-if="tab.key === 'trade'">
                <rect x="2" y="7" width="20" height="14" rx="2" />
                <path d="M16 7V5a2 2 0 00-2-2h-4a2 2 0 00-2 2v2" />
                <circle cx="9" cy="17" r="1.5" />
                <circle cx="15" cy="17" r="1.5" />
              </template>
              <!-- Message -->
              <template v-else-if="tab.key === 'message'">
                <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2v10z" />
              </template>
              <!-- User -->
              <template v-else-if="tab.key === 'user'">
                <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </template>
            </svg>
          </div>
          <span class="tab-bar__label">{{ tab.label }}</span>

          <!-- Active 指示器 -->
          <span
            v-if="isActive(tab.key)"
            class="tab-bar__indicator"
          />

          <!-- 消息红点 -->
          <span
            v-if="tab.key === 'message' && badge > 0"
            class="tab-bar__badge"
          >{{ badge > 99 ? '99+' : badge }}</span>
        </template>
      </router-link>
    </div>
  </nav>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { TAB_BARS } from '@utils/constants'
import { useMessageStore } from '@stores/message'
import { useUserStore } from '@stores/user'

const route = useRoute()
const messageStore = useMessageStore()
const userStore = useUserStore()

const tabs = TAB_BARS

const badge = computed(() => messageStore.totalUnread)

function isActive(key) {
  const path = route.path
  if (key === 'home') return path === '/home' || path === '/'
  if (key === 'trade') return path.startsWith('/trade')
  if (key === 'message') return path.startsWith('/message')
  if (key === 'user') return path.startsWith('/user')
  return false
}

function handleTabClick(tab) {
  if (tab.key === 'message' || tab.key === 'user') {
    if (!userStore.isLogin) {
      // 路由守卫会处理的
    }
  }
}
</script>

<style scoped>
/* ============================================
   底部导航 — Liquid Glass TabBar
   Editorial Ink · 液态玻璃
   ============================================ */

.tab-bar {
  position: fixed;
  bottom: 12px;
  left: 12px;
  right: 12px;
  z-index: var(--z-tab);
  padding-bottom: env(safe-area-inset-bottom);

  /* ── 固态底色（backdrop-filter 失效时的兜底） ── */
  background-color: rgba(255, 255, 255, 0.88);

  /* ── 液态玻璃核心层 ── */
  background:
    linear-gradient(
      180deg,
      rgba(255, 255, 255, 0.82) 0%,
      rgba(255, 255, 255, 0.78) 30%,
      rgba(255, 255, 255, 0.76) 60%,
      rgba(255, 255, 255, 0.80) 100%
    );

  /* ── 毛玻璃模糊 ── */
  backdrop-filter: blur(24px) saturate(180%);
  -webkit-backdrop-filter: blur(24px) saturate(180%);

  /* ── 圆角胶囊 ── */
  border-radius: 22px;

  /* ── 玻璃边框 ── */
  border: 0.5px solid rgba(255, 255, 255, 0.9);
  box-shadow:
    /* 外层柔光 */
    0 8px 32px rgba(0, 0, 0, 0.10),
    0 2px 8px rgba(0, 0, 0, 0.08),
    /* 顶部内高光 */
    inset 0 1px 0 rgba(255, 255, 255, 0.8),
    /* 底部内阴影 — 玻璃厚度感 */
    inset 0 -1px 0 rgba(255, 255, 255, 0.4);
}

.tab-bar__inner {
  display: flex;
  align-items: center;
  justify-content: space-around;
  height: var(--tab-height);
  max-width: 480px;
  margin: 0 auto;
  position: relative;
}

/* ── 单个 Tab 项 ── */
.tab-bar__item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1px;
  min-width: 56px;
  padding: 4px 0;
  color: var(--ink-400);
  transition:
    color var(--transition-fast),
    transform var(--transition-fast);
  text-decoration: none;
  -webkit-tap-highlight-color: transparent;
}

.tab-bar__item--active {
  color: var(--accent-600);
}

.tab-bar__item--active .tab-bar__icon {
  transform: translateY(-1px);
}

.tab-bar__item--special {
  margin-top: -28px;
}

/* ── 图标 ── */
.tab-bar__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  transition: transform var(--transition-fast);
}

/* ── 标签 ── */
.tab-bar__label {
  font-size: 10px;
  font-weight: var(--font-semibold);
  line-height: 1;
  letter-spacing: 0.03em;
  position: relative;
  z-index: 1;
}

/* ── Active 液态胶囊指示器 ── */
.tab-bar__indicator {
  position: absolute;
  bottom: 2px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 3px;
  border-radius: 3px;
  background: linear-gradient(
    90deg,
    var(--accent-400),
    var(--accent-600),
    var(--accent-400)
  );
  box-shadow:
    0 1px 4px rgba(7, 193, 96, 0.35),
    0 0 8px rgba(7, 193, 96, 0.2);
  animation: indicatorIn 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes indicatorIn {
  from {
    width: 6px;
    opacity: 0;
    transform: translateX(-50%) scaleX(0.3);
  }
  to {
    width: 20px;
    opacity: 1;
    transform: translateX(-50%) scaleX(1);
  }
}

/* ── 发布按钮 ── */
.tab-bar__publish {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: var(--radius-full);

  /* 玻璃质感渐变 */
  background: linear-gradient(
    135deg,
    rgba(7, 193, 96, 0.9),
    rgba(6, 173, 86, 0.9)
  );

  /* 玻璃高光 + 阴影 */
  box-shadow:
    0 4px 24px rgba(7, 193, 96, 0.35),
    0 1px 4px rgba(7, 193, 96, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.3),
    inset 0 -2px 0 rgba(0, 0, 0, 0.1);

  transition:
    transform var(--transition-fast),
    box-shadow var(--transition-fast);
}

.tab-bar__publish::after {
  content: '';
  position: absolute;
  top: 4px;
  left: 10px;
  right: 10px;
  height: 10px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  pointer-events: none;
  filter: blur(2px);
}

.tab-bar__publish:active {
  transform: scale(0.92);
  box-shadow:
    0 2px 8px rgba(7, 193, 96, 0.25),
    inset 0 2px 0 rgba(0, 0, 0, 0.15),
    inset 0 -1px 0 rgba(255, 255, 255, 0.15);
}

/* 脉冲光晕 */
.tab-bar__publish::before {
  content: '';
  position: absolute;
  inset: -5px;
  border-radius: var(--radius-full);
  background: rgba(7, 193, 96, 0.12);
  filter: blur(5px);
  animation: publishGlow 2.5s ease-in-out infinite;
  pointer-events: none;
}

@keyframes publishGlow {
  0%, 100% {
    opacity: 0.5;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.15);
  }
}

/* ── 消息红点 ── */
.tab-bar__badge {
  position: absolute;
  top: 0px;
  right: 6px;
  min-width: 17px;
  height: 17px;
  padding: 0 5px;
  border-radius: 9px;
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: #fff;
  font-size: 10px;
  font-weight: var(--font-bold);
  line-height: 17px;
  text-align: center;
  font-family: var(--font-mono);
  box-shadow:
    0 2px 6px rgba(220, 38, 38, 0.35),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  z-index: 2;
}
</style>
