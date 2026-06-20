<template>
  <div class="stat-card">
    <div class="stat-icon" :style="{ background: iconBg, color: iconColor }">
      <el-icon :size="22"><component :is="icon" /></el-icon>
    </div>
    <div class="stat-body">
      <div class="stat-label">{{ label }}</div>
      <div class="stat-value">{{ formattedValue }}</div>
      <div v-if="trend !== undefined" class="stat-trend" :class="trend >= 0 ? 'up' : 'down'">
        <el-icon :size="14"><component :is="trend >= 0 ? 'CaretTop' : 'CaretBottom'" /></el-icon>
        <span>{{ Math.abs(trend) }}% 较昨日</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  label: String,
  value: [Number, String],
  icon: [String, Object],
  iconBg: { type: String, default: '#e8f8ef' },
  iconColor: { type: String, default: '#07C160' },
  trend: Number,
})

const formattedValue = computed(() => {
  if (typeof props.value === 'number' && props.value >= 1000) {
    return props.value >= 10000
      ? (props.value / 10000).toFixed(1) + 'w'
      : (props.value / 1000).toFixed(1) + 'k'
  }
  return props.value
})
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  background: var(--bg-card);
  border: 2px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 20px 24px;
  box-shadow: var(--shadow-card);
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-body {
  flex: 1;
  min-width: 0;
}
.stat-label {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  margin-bottom: 4px;
  font-weight: 500;
}
.stat-value {
  font-family: var(--font-display);
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}
.stat-trend {
  display: flex;
  align-items: center;
  gap: 2px;
  margin-top: 4px;
  font-size: 0.75rem;
  font-weight: 500;
}
.stat-trend.up {
  color: var(--success);
}
.stat-trend.down {
  color: var(--error);
}
</style>
