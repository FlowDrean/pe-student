<template>
  <div class="empty-state">
    <div class="empty-state__icon">
      <slot name="icon">
        <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="var(--ink-200)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8" />
          <line x1="21" y1="21" x2="16.65" y2="16.65" />
        </svg>
      </slot>
    </div>
    <p class="empty-state__title">{{ title }}</p>
    <p v-if="description" class="empty-state__desc">{{ description }}</p>
    <button
      v-if="actionText"
      class="btn btn-outline btn-sm"
      @click="$emit('action')"
    >
      {{ actionText }}
    </button>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, default: '暂无内容' },
  description: { type: String, default: '' },
  actionText: { type: String, default: '' },
})
defineEmits(['action'])
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-10) var(--space-6);
  text-align: center;
}
.empty-state__icon { margin-bottom: var(--space-4); opacity: 0.6; }
.empty-state__title { font-family: var(--font-display); font-size: var(--text-lg); font-weight: var(--font-bold); color: var(--ink-500); margin-bottom: var(--space-2); }
.empty-state__desc { font-size: var(--text-sm); color: var(--ink-300); max-width: 240px; margin-bottom: var(--space-5); }
</style>
