<template>
  <div class="tab-layout">
    <div class="tab-layout__content">
      <router-view v-slot="{ Component }">
        <transition name="tab-page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>
    <AppTabBar />
  </div>
</template>

<script setup>
import AppTabBar from '@components/common/AppTabBar.vue'
import { useAppStore } from '@stores/app'
import { watchEffect } from 'vue'

const appStore = useAppStore()

watchEffect(() => {
  appStore.setShowTabBar(true)
})
</script>

<style scoped>
.tab-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  min-height: 100dvh;
  background: var(--bg-secondary);
}

.tab-layout__content {
  flex: 1;
  padding-bottom: calc(var(--tab-height) + 32px + env(safe-area-inset-bottom, 0px));
  overflow-y: auto;
}

/* Tab 页面转场 */
.tab-page-enter-active {
  animation: fadeIn 0.22s ease-out;
}

.tab-page-leave-active {
  animation: fadeIn 0.12s ease-in reverse;
}
</style>
