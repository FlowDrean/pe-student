<template>
  <div class="settings-page page">
    <div class="menu-section">
      <div class="menu-item" @click="handleClear">
        <SvgIcon name="trash" :size="18" color="var(--ink-600)" />
        <span>清除缓存</span>
        <span class="menu-value">12.5MB</span>
      </div>
      <div class="menu-item">
        <SvgIcon name="bell" :size="18" color="var(--ink-600)" />
        <span>消息通知</span>
        <span class="menu-status">已开启</span>
      </div>
      <div class="menu-item">
        <SvgIcon name="lock" :size="18" color="var(--ink-600)" />
        <span>隐私设置</span>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--ink-300)" stroke-width="2.5"><polyline points="9 18 15 12 9 6"/></svg>
      </div>
      <div class="menu-item">
        <SvgIcon name="info" :size="18" color="var(--ink-600)" />
        <span>关于我们</span>
        <span class="menu-value">v1.0.0</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import SvgIcon from '@components/common/SvgIcon.vue'
import { showToast } from 'vant'
function handleClear() { showToast('缓存已清除') }
</script>

<style scoped>
.settings-page { min-height: 100vh; background: var(--bg-secondary); }
.menu-section { margin: var(--space-3) var(--content-padding); background: var(--bg-primary); border: 2px solid var(--ink-100); border-radius: var(--radius-lg); overflow: hidden; }
.menu-item { display: flex; align-items: center; gap: var(--space-3); padding: var(--space-4); font-size: var(--text-base); font-weight: var(--font-medium); color: var(--ink-700); border-bottom: 1px solid var(--ink-50); cursor: pointer; transition: background var(--transition-fast); }
.menu-item:last-child { border-bottom: none; }
.menu-item:active { background: var(--ink-50); }
.menu-value { margin-left: auto; font-family: var(--font-mono); font-size: var(--text-sm); color: var(--ink-400); }
.menu-status { margin-left: auto; font-size: var(--text-sm); color: var(--success); font-weight: var(--font-medium); }
</style>
