<template>
  <SwipeCell :disabled="!showActions">
    <div class="trade-card" @click="$emit('click')">
      <div class="trade-card__img" :class="!hasImage && 'trade-card__img--placeholder'">
        <img v-if="hasImage" :src="trade.images[0]" class="trade-card__img-real" loading="lazy" />
        <span v-else class="trade-card__emoji">{{ categoryEmoji }}</span>
        <!-- 状态标签 -->
        <span v-if="trade.status === 2" class="trade-card__badge trade-card__badge--sold">已售</span>
        <span v-else-if="trade.status === 3" class="trade-card__badge trade-card__badge--dealing">交易中</span>
      </div>
      <div class="trade-card__body">
        <h3 class="trade-card__title text-ellipsis">{{ trade.title }}</h3>
        <p v-if="trade.description" class="trade-card__desc text-ellipsis">{{ trade.description }}</p>
        <PriceTag :price="trade.price" :original-price="trade.originalPrice" size="sm" />
      </div>
    </div>

    <template v-if="showActions" #right>
      <button class="swipe-action swipe-action--edit" @click.stop="$emit('edit', trade)">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
        <span>编辑</span>
      </button>
      <button class="swipe-action swipe-action--delete" @click.stop="$emit('delete', trade)">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/></svg>
        <span>删除</span>
      </button>
    </template>
  </SwipeCell>
</template>

<script setup>
import { computed } from 'vue'
import { SwipeCell } from 'vant'
import PriceTag from './PriceTag.vue'

const props = defineProps({
  trade: { type: Object, required: true },
  showActions: { type: Boolean, default: false },
})

defineEmits(['click', 'edit', 'delete'])

const hasImage = computed(() => props.trade.images && props.trade.images.length > 0)

const categoryEmoji = computed(() => {
  const map = {
    book: '\u{1F4D6}',
    electronics: '\u{1F4BB}',
    daily: '\u{1F3F7}️',
    clothing: '\u{1F454}',
    other: '\u{1F4E6}',
  }
  return map[props.trade.category] || '\u{1F4E6}'
})
</script>

<style scoped>
.trade-card {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  border: 2px solid var(--ink-100);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  transition: all var(--transition-fast);
}

.trade-card:active {
  box-shadow: var(--shadow-card-hover);
  transform: translateY(-1px);
}

.trade-card__img {
  position: relative;
  aspect-ratio: 1;
  background: var(--ink-50);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.trade-card__img--placeholder {
  background: linear-gradient(135deg, var(--ink-50) 0%, var(--ink-100) 100%);
}

.trade-card__img-real {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.trade-card:active .trade-card__img-real {
  transform: scale(1.05);
}

.trade-card__emoji {
  font-size: 36px;
  opacity: 0.6;
}

/* ── 状态标签 ── */
.trade-card__badge {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 3px 8px;
  border-radius: var(--radius-xs);
  font-size: var(--text-3xs);
  font-weight: var(--font-bold);
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: #fff;
}

.trade-card__badge--sold {
  background: var(--ink-600);
}

.trade-card__badge--dealing {
  background: var(--amber-600);
}

/* ── 内容 ── */
.trade-card__body {
  padding: var(--space-3);
}

.trade-card__title {
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--ink-800);
  margin-bottom: 2px;
  line-height: 1.4;
}

.trade-card__desc {
  font-size: var(--text-2xs);
  color: var(--ink-400);
  margin-bottom: var(--space-1);
}

/* ── 滑动操作 ── */
.swipe-action {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  width: 64px;
  height: 100%;
  font-size: var(--text-2xs);
  font-weight: var(--font-semibold);
  color: #fff;
  border: none;
  cursor: pointer;
}

.swipe-action--edit {
  background: var(--accent-600);
}

.swipe-action--delete {
  background: var(--error);
}
</style>
