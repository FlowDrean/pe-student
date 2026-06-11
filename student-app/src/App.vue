<template>
  <router-view v-slot="{ Component, route }">
    <transition
      :name="route.meta.transition || 'page'"
      mode="out-in"
    >
      <component :is="Component" :key="route.path" />
    </transition>
  </router-view>
</template>

<script setup>
import { onMounted } from 'vue'
import { useAppStore } from '@stores/app'

const appStore = useAppStore()

onMounted(() => {
  appStore.init()
})
</script>

<style>
/* ============================================
   Vant 主题变量覆盖 — Editorial Ink
   ============================================ */
:root {
  --van-primary-color: var(--accent-600);
  --van-danger-color: var(--error);
  --van-success-color: var(--success);
  --van-warning-color: var(--warning);
  --van-text-color: var(--ink-800);
  --van-text-color-2: var(--ink-500);
  --van-text-color-3: var(--ink-300);
  --van-background: var(--bg-secondary);
  --van-background-2: var(--bg-primary);
  --van-border-color: var(--ink-100);
  --van-font-size-xs: 12px;
  --van-font-size-sm: 13px;
  --van-font-size-md: 15px;
  --van-font-size-lg: 17px;
  --van-font-bold: 600;
  --van-radius-sm: 4px;
  --van-radius-md: 6px;
  --van-radius-lg: 10px;
  --van-radius-max: 9999px;
}
</style>
