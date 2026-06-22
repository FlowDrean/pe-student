<template>
  <aside class="sidebar" :class="{ collapsed }">
    <div class="sidebar-brand">
      <div class="brand-icon">P</div>
      <transition name="fade">
        <div v-if="!collapsed" class="brand-text">
          <div class="brand-title">PE-Student</div>
          <div class="brand-subtitle">后台管理</div>
        </div>
      </transition>
    </div>

    <nav class="sidebar-nav">
      <router-link
        v-for="item in menuItems"
        :key="item.path"
        :to="item.path"
        class="nav-item"
        :class="{ active: isActive(item.path) }"
      >
        <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
        <span v-if="!collapsed" class="nav-label">{{ item.label }}</span>
      </router-link>
    </nav>

    <div class="sidebar-footer">
      <button class="collapse-btn" @click="collapsed = !collapsed">
        <el-icon><component :is="collapsed ? 'Expand' : 'Fold'" /></el-icon>
      </button>
    </div>
  </aside>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const collapsed = ref(false)

const menuItems = [
  { path: '/dashboard', label: '仪表盘', icon: 'Odometer' },
  { path: '/users', label: '用户管理', icon: 'User' },
  { path: '/posts', label: '帖子管理', icon: 'Document' },
  { path: '/trades', label: '商品管理', icon: 'ShoppingBag' },
  { path: '/comments', label: '评论管理', icon: 'ChatLineSquare' },
  { path: '/messages', label: '消息管理', icon: 'Message' },
]

function isActive(path) {
  return route.path === path || route.path.startsWith(path + '/')
}
</script>

<style scoped>
.sidebar {
  width: 220px;
  background: var(--ink-800);
  display: flex;
  flex-direction: column;
  transition: width 0.25s ease;
  overflow: hidden;
  flex-shrink: 0;
}
.sidebar.collapsed {
  width: 64px;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  padding: 20px 16px;
  gap: 12px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}
.brand-icon {
  width: 36px;
  height: 36px;
  background: var(--accent-500);
  color: #fff;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-size: 1.25rem;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 2px 0 0 rgba(6, 173, 86, 0.3);
}
.brand-text {
  white-space: nowrap;
}
.brand-title {
  color: #fff;
  font-family: var(--font-display);
  font-size: 1rem;
  font-weight: 600;
  line-height: 1.2;
}
.brand-subtitle {
  color: var(--ink-400);
  font-size: 0.75rem;
  line-height: 1.3;
}

.sidebar-nav {
  flex: 1;
  padding: 12px 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: var(--radius-md);
  color: var(--ink-300);
  text-decoration: none;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.15s ease;
  white-space: nowrap;
}
.nav-item:hover {
  background: rgba(255,255,255,0.06);
  color: #fff;
}
.nav-item.active {
  background: rgba(7, 193, 96, 0.12);
  color: var(--accent-500);
}
.nav-icon {
  font-size: 1.125rem;
  flex-shrink: 0;
}

.sidebar-footer {
  padding: 12px 8px;
  border-top: 1px solid rgba(255,255,255,0.06);
}
.collapse-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  background: transparent;
  border: none;
  color: var(--ink-400);
  cursor: pointer;
  border-radius: var(--radius-md);
  font-size: 1rem;
  transition: all 0.15s ease;
}
.collapse-btn:hover {
  background: rgba(255,255,255,0.06);
  color: #fff;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
