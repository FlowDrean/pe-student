<template>
  <div class="default-layout">
    <AppHeader
      v-if="showHeader"
      :title="title"
      :show-back="showBack"
      :right-icon="rightIcon"
      @back="goBack"
      @right-click="handleRightClick"
    />
    <div
      class="default-layout__content"
      :class="{ 'with-header': showHeader }"
    >
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import AppHeader from '@components/common/AppHeader.vue'

const router = useRouter()
const route = useRoute()

const showHeader = computed(() => route.meta.showHeader !== false)
const title = computed(() => route.meta.title || '')
const showBack = computed(() => route.meta.showBack !== false)
const rightIcon = computed(() => route.meta.rightIcon || '')

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.replace('/')
  }
}

function handleRightClick() {
  // 由具体页面通过 provide/inject 处理
}
</script>

<style scoped>
.default-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  min-height: 100dvh;
  background: var(--bg-secondary);
}

.default-layout__content {
  flex: 1;
  overflow-y: auto;
  background: var(--bg-secondary);
}

.with-header {
  padding-top: var(--header-height);
}
</style>
