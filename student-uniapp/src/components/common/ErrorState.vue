<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    title?: string
    description?: string
    showRetry?: boolean
    retryText?: string
  }>(),
  {
    title: '加载失败',
    description: '请检查网络连接后重试',
    showRetry: true,
    retryText: '重新加载',
  },
)

const emit = defineEmits<{
  retry: []
}>()

function onRetry() {
  emit('retry')
}

const iconChar = computed(() => {
  if (props.title.includes('网络') || props.description.includes('网络')) return '\u{1F4F6}'
  if (props.title.includes('服务器') || props.title.includes('500')) return '\u{1F4BB}'
  if (props.title.includes('404') || props.title.includes('不存在')) return '\u{1F50D}'
  if (props.title.includes('权限') || props.title.includes('403')) return '\u{1F512}'
  return '⚠️'
})
</script>

<template>
  <view class="error-state">
    <!-- 错误图标 -->
    <view class="error-state__icon">
      <text class="error-state__icon-char">{{ iconChar }}</text>
    </view>

    <!-- 标题 -->
    <text class="error-state__title">{{ title }}</text>

    <!-- 描述 -->
    <text v-if="description" class="error-state__description">
      {{ description }}
    </text>

    <!-- 重试按钮 -->
    <view
      v-if="showRetry"
      class="error-state__retry btn-primary"
      @tap="onRetry"
    >
      <text class="error-state__retry-text">{{ retryText }}</text>
    </view>
  </view>
</template>

<style scoped>
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px 32px;
  text-align: center;
}

.error-state__icon {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  background: #fef2f2;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  border: 2px solid #fecaca;
}

.error-state__icon-char {
  font-size: 36px;
}

.error-state__title {
  font-size: 17px;
  font-weight: 700;
  color: var(--ink-800);
  margin-bottom: 8px;
}

.error-state__description {
  font-size: 14px;
  color: var(--ink-400);
  line-height: 1.6;
  max-width: 260px;
  margin-bottom: 24px;
}

.error-state__retry {
  display: inline-flex;
}

.error-state__retry-text {
  color: #fff;
  font-weight: 600;
}
</style>
