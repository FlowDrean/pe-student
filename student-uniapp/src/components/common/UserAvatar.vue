<template>
  <view
    class="user-avatar"
    :style="{
      width: size + 'px',
      height: size + 'px',
      background: avatarUrl ? 'transparent' : bgColor,
    }"
    @tap="$emit('tap')"
  >
    <image v-if="avatarUrl" class="user-avatar__img" :src="avatarUrl" mode="aspectFill" />
    <text v-else class="user-avatar__text" :style="{ fontSize: Math.round(size * 0.4) + 'px' }">{{ firstChar }}</text>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { resolveAssetUrl } from '@/http'

const props = withDefaults(
  defineProps<{
    /** 头像 URL（相对路径自动拼接服务器地址） */
    avatar?: string
    /** 昵称/用户名，无头像时取首字符占位 */
    name?: string
    /** 用户ID，用于生成占位底色 */
    userId?: number
    /** 头像尺寸（px） */
    size?: number
  }>(),
  { avatar: '', name: '', userId: 0, size: 40 }
)

defineEmits(['tap'])

const avatarUrl = computed(() => (props.avatar ? resolveAssetUrl(props.avatar) : ''))

const firstChar = computed(() => (props.name || '?').charAt(0))

const bgColor = computed(() => {
  const colors = ['#07C160', '#f97316', '#059669', '#f59e0b', '#dc2626', '#7c3aed']
  return colors[(props.userId || 0) % colors.length]
})
</script>

<style scoped>
.user-avatar {
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}

.user-avatar__img {
  width: 100%;
  height: 100%;
}

.user-avatar__text {
  color: #fff;
  font-weight: 700;
  font-family: var(--font-display);
}
</style>
