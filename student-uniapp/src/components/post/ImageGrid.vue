<template>
  <view class="image-grid" :class="[`image-grid--${gridClass}`]">
    <view
      v-for="(img, index) in displayImages"
      :key="index"
      class="image-grid__item"
      :class="{ 'image-grid__item--more': index === maxShow - 1 && hasMore }"
      @tap="$emit('preview', index)"
    >
      <image :src="resolveAssetUrl(img)" :mode="gridClass === 'single' ? 'widthFix' : 'aspectFill'" class="image-grid__img" />
      <view v-if="index === maxShow - 1 && hasMore" class="image-grid__overlay">
        <text class="image-grid__overlay-text">+{{ images.length - maxShow + 1 }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

import { resolveAssetUrl } from '@/http'
const props = defineProps<{
  images: string[]
  maxShow?: number
}>()

defineEmits<{
  preview: [index: number]
}>()

const maxShow = props.maxShow ?? 9

const displayImages = computed(() => props.images.slice(0, maxShow))

const hasMore = computed(() => props.images.length > maxShow)

const gridClass = computed(() => {
  const len = displayImages.value.length
  if (len === 1) return 'single'
  if (len === 2 || len === 4) return 'double'
  return 'triple'
})
</script>

<style scoped>
.image-grid {
  display: grid;
  gap: 3px;
}

.image-grid--single {
  grid-template-columns: 1fr;
}

.image-grid--single .image-grid__item {
  aspect-ratio: 16/10;
  max-height: 240px;
}

.image-grid--double {
  grid-template-columns: repeat(2, 1fr);
}

.image-grid--double .image-grid__item {
  aspect-ratio: 1;
}

.image-grid--triple {
  grid-template-columns: repeat(3, 1fr);
}

.image-grid--triple .image-grid__item {
  aspect-ratio: 1;
}

.image-grid__item {
  position: relative;
  overflow: hidden;
  border-radius: var(--radius-sm);
  background: var(--ink-100);
  border: 1px solid var(--ink-100);
}

.image-grid__img {
  width: 100%;
  height: 100%;
  display: block;
}

.image-grid__item:active .image-grid__img {
  transform: scale(1.03);
}

.image-grid__item--more .image-grid__overlay {
  position: absolute;
  inset: 0;
  background: rgba(15, 17, 25, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-grid__overlay-text {
  color: #fff;
  font-family: var(--font-mono);
  font-size: 20px;
  font-weight: 700;
}
</style>
