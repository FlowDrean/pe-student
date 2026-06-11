<template>
  <div class="loading" :class="`loading--${variant}`">
    <template v-if="variant === 'skeleton'">
      <div class="loading__skeleton">
        <div v-for="i in count" :key="i" class="skeleton-card">
          <div class="skeleton skeleton-avatar"></div>
          <div class="skeleton-content">
            <div class="skeleton skeleton-line skeleton-line--1"></div>
            <div class="skeleton skeleton-line skeleton-line--2"></div>
            <div class="skeleton skeleton-line skeleton-line--3"></div>
          </div>
        </div>
      </div>
    </template>

    <template v-else-if="variant === 'spinner'">
      <div class="loading__spinner">
        <div class="spinner"></div>
        <p v-if="text" class="loading__text">{{ text }}</p>
      </div>
    </template>

    <template v-else>
      <div class="loading__fullpage">
        <div class="spinner spinner--primary"></div>
        <p v-if="text" class="loading__text">{{ text }}</p>
      </div>
    </template>
  </div>
</template>

<script setup>
defineProps({
  variant: { type: String, default: 'skeleton' },
  count: { type: Number, default: 4 },
  text: { type: String, default: '加载中...' },
})
</script>

<style scoped>
.loading { width: 100%; }
.loading__skeleton { display: flex; flex-direction: column; gap: var(--space-4); padding: var(--space-4) var(--content-padding); }
.skeleton-card { display: flex; gap: var(--space-3); padding: var(--space-4); background: var(--bg-primary); border: 2px solid var(--ink-100); border-radius: var(--radius-lg); }
.skeleton-avatar { width: 44px; height: 44px; border-radius: var(--radius-full); flex-shrink: 0; }
.skeleton-content { flex: 1; display: flex; flex-direction: column; gap: var(--space-2); }
.skeleton-line { height: 13px; border-radius: var(--radius-xs); }
.skeleton-line--1 { width: 35%; height: 15px; }
.skeleton-line--2 { width: 85%; }
.skeleton-line--3 { width: 60%; }
.loading__spinner,
.loading__fullpage { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: var(--space-3); padding: var(--space-8); }
.loading__fullpage { min-height: 60vh; }
.spinner { width: 32px; height: 32px; border: 3px solid var(--ink-100); border-top-color: var(--ink-500); border-radius: 50%; animation: spin 0.7s linear infinite; }
.spinner--primary { width: 40px; height: 40px; border-color: var(--accent-100); border-top-color: var(--accent-600); }
.loading__text { font-size: var(--text-sm); color: var(--ink-400); font-weight: var(--font-medium); }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
