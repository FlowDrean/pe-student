<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    title?: string
    showBack?: boolean
    bgColor?: string
  }>(),
  {
    showBack: true,
    bgColor: 'var(--bg-secondary)',
  },
)

const pages = getCurrentPages()
const canBack = computed(() => props.showBack && pages.length > 1)

function goBack() {
  uni.navigateBack()
}
</script>

<template>
  <view class="layout-default" :style="{ backgroundColor: bgColor }">
    <view class="navbar" v-if="title">
      <view class="navbar-left" @tap="goBack" v-if="canBack">
        <wd-icon name="arrow-left" size="20" color="var(--ink-700)" />
      </view>
      <text class="navbar-title">{{ title }}</text>
      <view class="navbar-right">
        <slot name="right" />
      </view>
    </view>
    <view class="layout-body">
      <slot />
    </view>
  </view>
</template>

<style scoped>
.layout-default {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.navbar {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  background: var(--bg-primary);
  border-bottom: 2px solid var(--ink-100);
  position: sticky;
  top: 0;
  z-index: 100;
}

.navbar-left {
  position: absolute;
  left: 16px;
}

.navbar-back {
  font-size: 20px;
  color: var(--accent-500);
  font-weight: 700;
}

.navbar-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--ink-900);
}

.navbar-right {
  position: absolute;
  right: 16px;
}

.layout-body {
  flex: 1;
  overflow-y: auto;
}
</style>
