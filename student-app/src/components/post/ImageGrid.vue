<template>
  <div class="image-grid" :class="`image-grid--${gridClass}`">
    <div
      v-for="(img, index) in displayImages"
      :key="index"
      class="image-grid__item"
      :class="{ 'image-grid__item--more': index === maxShow - 1 && hasMore }"
      @click.stop="$emit('preview', index)"
    >
      <img :src="img" :alt="`图片${index + 1}`" loading="lazy" />
      <div v-if="index === maxShow - 1 && hasMore" class="image-grid__overlay">
        <span>+{{ images.length - maxShow + 1 }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({ images: { type: Array, required: true }, maxShow: { type: Number, default: 9 } })
defineEmits(['preview'])
const displayImages = computed(() => props.images.slice(0, props.maxShow))
const hasMore = computed(() => props.images.length > props.maxShow)
const gridClass = computed(() => { const len = displayImages.value.length; if (len === 1) return 'single'; if (len === 2 || len === 4) return 'double'; return 'triple' })
</script>

<style scoped>
.image-grid { display: grid; gap: 3px; }
.image-grid--single { grid-template-columns: 1fr; }
.image-grid--single .image-grid__item { aspect-ratio: 16/10; max-height: 240px; }
.image-grid--double { grid-template-columns: repeat(2, 1fr); }
.image-grid--double .image-grid__item { aspect-ratio: 1; }
.image-grid--triple { grid-template-columns: repeat(3, 1fr); }
.image-grid--triple .image-grid__item { aspect-ratio: 1; }
.image-grid__item { position: relative; overflow: hidden; border-radius: var(--radius-sm); background: var(--ink-100); border: 1px solid var(--ink-100); }
.image-grid__item img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.3s ease; }
.image-grid__item img:active { transform: scale(1.03); }
.image-grid__item--more .image-grid__overlay { position: absolute; inset: 0; background: rgba(15, 17, 25, 0.55); display: flex; align-items: center; justify-content: center; color: #fff; font-family: var(--font-mono); font-size: var(--text-xl); font-weight: var(--font-bold); }
</style>
