<template>
  <div class="error-state">
    <div class="error-state__icon">
      <slot name="icon">
        <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="var(--gray-300)" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10" />
          <line x1="15" y1="9" x2="9" y2="15" />
          <line x1="9" y1="9" x2="15" y2="15" />
        </svg>
      </slot>
    </div>
    <p class="error-state__title">{{ title }}</p>
    <p v-if="description" class="error-state__desc">{{ description }}</p>
    <button
      v-if="retryText"
      class="btn btn-outline btn-sm"
      @click="$emit('retry')"
    >
      {{ retryText }}
    </button>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, default: '加载失败' },
  description: { type: String, default: '' },
  retryText: { type: String, default: '点击重试' },
})

defineEmits(['retry'])
</script>

<style scoped>
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-10) var(--space-6);
  text-align: center;
}

.error-state__icon {
  margin-bottom: var(--space-4);
  opacity: 0.7;
}

.error-state__title {
  font-size: var(--text-base);
  font-weight: var(--font-medium);
  color: var(--gray-600);
  margin-bottom: var(--space-2);
}

.error-state__desc {
  font-size: var(--text-sm);
  color: var(--gray-400);
  max-width: 240px;
  margin-bottom: var(--space-5);
}
</style>
