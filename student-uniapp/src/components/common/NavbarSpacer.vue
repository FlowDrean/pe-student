<script setup lang="ts">
import { computed } from 'vue'

const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = systemInfo.statusBarHeight || 0
const navbarHeight = 44

const spacerStyle = computed(() => ({
  paddingTop: `${statusBarHeight + navbarHeight}px`,
}))
</script>

<template>
  <view class="navbar-spacer" :style="spacerStyle" />
</template>

<style scoped>
.navbar-spacer {
  width: 100%;
}
</style>
