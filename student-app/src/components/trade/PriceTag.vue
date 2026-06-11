<template>
  <span class="price-tag" :class="`price-tag--${size}`">
    <span class="price-tag__currency">¥</span>
    <span class="price-tag__current">{{ formatPrice(price) }}</span>
    <span v-if="originalPrice && originalPrice > price" class="price-tag__original">{{ formatPrice(originalPrice) }}</span>
  </span>
</template>

<script setup>
import { formatPrice } from '@utils/format'
defineProps({
  price: { type: [Number, String], required: true },
  originalPrice: { type: [Number, String], default: 0 },
  size: { type: String, default: 'md' },
})
</script>

<style scoped>
.price-tag {
  display: inline-flex;
  align-items: baseline;
  gap: var(--space-1);
}

.price-tag__currency {
  font-size: inherit;
  font-weight: var(--font-semibold);
  color: var(--coral-600);
  margin-right: -2px;
}

.price-tag__current {
  color: var(--coral-600);
  font-weight: var(--font-bold);
  font-family: var(--font-mono);
}

.price-tag__original {
  color: var(--ink-300);
  font-size: var(--text-xs);
  text-decoration: line-through;
  font-family: var(--font-mono);
}

.price-tag--sm { font-size: var(--text-sm); }
.price-tag--sm .price-tag__current { font-size: var(--text-base); }
.price-tag--md { font-size: var(--text-base); }
.price-tag--md .price-tag__current { font-size: var(--text-xl); }
</style>
