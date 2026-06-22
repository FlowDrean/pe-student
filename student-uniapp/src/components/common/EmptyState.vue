<script setup lang="ts">
defineProps<{
  title: string
  description?: string
  actionText?: string
}>()

const emit = defineEmits<{
  action: []
}>()

function onAction() {
  emit('action')
}
</script>

<template>
  <view class="empty-state">
    <!-- 图标区域 -->
    <view class="empty-state__icon">
      <slot name="icon">
        <text class="empty-state__default-icon">&#128220;</text>
      </slot>
    </view>

    <!-- 标题 -->
    <text class="empty-state__title">{{ title }}</text>

    <!-- 描述 -->
    <text v-if="description" class="empty-state__description">
      {{ description }}
    </text>

    <!-- 操作按钮 -->
    <view
      v-if="actionText"
      class="empty-state__action btn-primary"
      @tap="onAction"
    >
      <text class="empty-state__action-text">{{ actionText }}</text>
    </view>
  </view>
</template>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px 32px;
  text-align: center;
}

.empty-state__icon {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  background: var(--ink-50);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  border: 2px solid var(--ink-100);
}

.empty-state__default-icon {
  font-size: 36px;
}

.empty-state__title {
  font-size: 17px;
  font-weight: 700;
  color: var(--ink-800);
  margin-bottom: 8px;
}

.empty-state__description {
  font-size: 14px;
  color: var(--ink-400);
  line-height: 1.6;
  max-width: 260px;
  margin-bottom: 24px;
}

.empty-state__action {
  display: inline-flex;
}

.empty-state__action-text {
  color: #fff;
  font-weight: 600;
}
</style>
