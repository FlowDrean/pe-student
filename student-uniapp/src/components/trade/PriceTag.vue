<template>
  <view class="price-tag" :class="[`price-tag--${size}`]">
    <text class="price-tag__price">¥{{ priceText }}</text>
    <text v-if="showOriginal" class="price-tag__original">¥{{ originalPriceText }}</text>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  price: number
  originalPrice?: number
  size?: 'sm' | 'md'
}>()

const size = computed(() => props.size || 'md')

const priceText = computed(() => {
  const p = Number(props.price)
  return Number.isInteger(p) ? String(p) : p.toFixed(2)
})

const originalPriceText = computed(() => {
  const p = Number(props.originalPrice || 0)
  return Number.isInteger(p) ? String(p) : p.toFixed(2)
})

const showOriginal = computed(() => {
  return props.originalPrice != null && props.originalPrice > props.price
})
</script>

<style scoped>
.price-tag {
  display: inline-flex;
  align-items: baseline;
  gap: 8px;
}

.price-tag__price {
  color: var(--accent-500);
  font-weight: 700;
  font-family: var(--font-display);
  line-height: 1.2;
}

.price-tag--md .price-tag__price {
  font-size: 20px;
}

.price-tag--sm .price-tag__price {
  font-size: 14px;
}

.price-tag__original {
  color: var(--ink-300);
  font-size: 13px;
  text-decoration: line-through;
  font-weight: 400;
}
</style>
