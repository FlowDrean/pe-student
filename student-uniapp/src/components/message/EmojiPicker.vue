<template>
  <view class="emoji-picker">
    <!-- 分类标签 -->
    <scroll-view scroll-x class="emoji-picker__tabs-scroll">
      <view class="emoji-picker__tabs">
        <view
          v-for="cat in categories"
          :key="cat.key"
          class="emoji-picker__tab"
          :class="{ 'emoji-picker__tab--active': activeTab === cat.key }"
          @tap="activeTab = cat.key"
        >
          <text class="emoji-picker__tab-icon">{{ cat.icon }}</text>
          <text class="emoji-picker__tab-label">{{ cat.label }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- Emoji 网格 -->
    <scroll-view scroll-y class="emoji-picker__grid-scroll">
      <view class="emoji-picker__grid">
        <view
          v-for="(emoji, index) in currentEmojis"
          :key="index"
          class="emoji-picker__item"
          @tap="$emit('select', emoji)"
        >
          <text class="emoji-picker__item-text">{{ emoji }}</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

defineEmits<{
  select: [emoji: string]
}>()

const activeTab = ref('smile')

const categories = [
  { key: 'smile', icon: '😂', label: '表情' },
  { key: 'gesture', icon: '👋', label: '手势' },
  { key: 'heart', icon: '❤️', label: '爱心' },
  { key: 'animal', icon: '🐶', label: '动物' },
  { key: 'food', icon: '🍔', label: '食物' },
  { key: 'item', icon: '⚽', label: '物品' },
]

const emojiMap: Record<string, string[]> = {
  smile: ['😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂', '🙂', '😊', '😇', '🥰', '😍', '🤩', '😘', '😗', '😚', '😋', '😛', '😜', '🤪', '😝', '🤑', '🤗', '🤭', '🤫', '🤔', '🤐', '🤨', '😐', '😑', '😶', '😏', '😒', '🙄', '😬', '🤥', '😌', '😔', '😪', '🤤', '😴', '😷', '🤒', '🤕', '🤢', '🤮', '🥴', '😵', '🤯', '🥳', '🥺', '😢', '😭', '😤', '😠', '😡', '🤬', '💀', '☠️', '💩', '🤡', '👹', '👺', '👻', '👽', '🤖'],
  gesture: ['👋', '🤚', '🖐', '✋', '🖖', '👌', '🤌', '🤏', '✌', '🤞', '🤟', '🤘', '🤙', '👈', '👉', '👆', '🖕', '👇', '☝', '👍', '👎', '✊', '👊', '🤛', '🤜', '👏', '🙌', '👐', '🤲', '🤝', '🙏', '✍', '💅', '🤳', '💪', '🦵', '🦶', '👂', '🦻', '👃', '🧠', '🫀', '🫁', '🦷', '👀', '👁', '👅', '👄'],
  heart: ['❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '🤎', '💔', '❣', '💕', '💞', '💓', '💗', '💖', '💘', '💝', '💟', '♥', '💌', '💋', '🫂', '👥', '💑', '💏', '👪', '💍', '💎'],
  animal: ['🐶', '🐱', '🐭', '🐹', '🐰', '🦊', '🐻', '🐼', '🐻‍❄', '🐨', '🐯', '🦁', '🐮', '🐷', '🐸', '🐵', '🙈', '🙉', '🙊', '🐒', '🐔', '🐧', '🐦', '🐤', '🐣', '🐥', '🦆', '🦅', '🦉', '🦇', '🐺', '🐗', '🐴', '🦄', '🐝', '🪱', '🐛', '🦋', '🐌', '🐞', '🐜', '🪰', '🪲', '🪳', '🦟', '🦗', '🕷', '🦂', '🐢', '🐍', '🦎', '🦖', '🦕', '🐙', '🦑', '🦐', '🦞', '🦀', '🐡', '🐠', '🐟', '🐬', '🐳', '🐋', '🦈', '🐊', '🐅', '🐆', '🦓', '🦍', '🦧', '🐘', '🦛', '🦏', '🐪', '🐫', '🦒', '🦘', '🐃', '🐂', '🐄', '🐎', '🐖', '🐏', '🐑', '🦙', '🐐', '🦌', '🐕', '🐩', '🦮', '🐕‍🦺', '🐈', '🐈‍⬛', '🪶', '🐓', '🦃', '🦤', '🦚', '🦜', '🦢', '🦩'],
  food: ['🍔', '🍟', '🍕', '🌭', '🍿', '🧈', '🥞', '🧇', '🧀', '🥓', '🥩', '🍗', '🍖', '🦴', '🌮', '🌯', '🥙', '🧆', '🥚', '🍳', '🥘', '🍲', '🫕', '🥣', '🥗', '🍝', '🍜', '🍲', '🍛', '🍣', '🍱', '🥟', '🦪', '🍤', '🍙', '🍚', '🍘', '🍥', '🥠', '🥮', '🍢', '🍡', '🍧', '🍨', '🍦', '🥧', '🧁', '🍰', '🎂', '🍮', '🍭', '🍬', '🍫', '🍿', '🍩', '🍪', '🌰', '🥜', '🍯', '🥛', '🍼', '🫖', '☕', '🍵', '🧃', '🥤', '🧋', '🍶', '🍺', '🍻', '🥂', '🍷', '🥃', '🍸', '🍹', '🧉', '🍾', '🧊', '🥄', '🍴', '🍽', '🥢'],
  item: ['⚽', '🏀', '🏈', '⚾', '🥎', '🎾', '🏐', '🏉', '🥏', '🎱', '🪀', '🏓', '🏸', '🏒', '🏑', '🥍', '🏏', '🪃', '🥅', '⛳', '🪁', '🏹', '🎣', '🤿', '🥊', '🥋', '🎽', '🛹', '🛼', '🛷', '⛸', '🥌', '🎿', '⛷', '🏂', '🪂', '🏋', '🤼', '🤸', '🤺', '⛹', '🤾', '🏌', '🏇', '🧘', '🏄', '🏊', '🤽', '🚣', '🧗', '🚵', '🚴', '🏆', '🥇', '🥈', '🥉', '🏅', '🎖', '🏵', '🎗', '🎫', '🎟', '🎪', '🤹', '🎭', '🩰', '🎨', '🎬', '🎤', '🎧', '🎼', '🎹', '🥁', '🪘', '🎷', '🎺', '🪗', '🎸', '🪕', '🎻', '🎲', '♟', '🎯', '🎳', '🎮', '🕹', '🎰'],
}

const currentEmojis = computed(() => emojiMap[activeTab.value] || [])
</script>

<style scoped>
.emoji-picker {
  background: var(--bg-primary);
  border-top: 2px solid var(--ink-100);
  padding-bottom: env(safe-area-inset-bottom);
  user-select: none;
}

/* ── 分类标签 ── */
.emoji-picker__tabs-scroll {
  width: 100%;
  white-space: nowrap;
}

.emoji-picker__tabs {
  display: flex;
  gap: 4px;
  padding: 8px 8px 0;
}

.emoji-picker__tab {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: var(--radius-full);
  border: 1.5px solid transparent;
  background: transparent;
  transition: all 0.15s ease;
}

.emoji-picker__tab:active {
  background: var(--ink-100);
}

.emoji-picker__tab--active {
  background: var(--accent-50);
  border-color: var(--accent-500);
}

.emoji-picker__tab-icon {
  font-size: 16px;
  line-height: 1;
}

.emoji-picker__tab-label {
  font-size: 12px;
  color: var(--ink-500);
  font-weight: 500;
}

.emoji-picker__tab--active .emoji-picker__tab-label {
  color: var(--accent-600);
  font-weight: 600;
}

/* ── Emoji 网格 ── */
.emoji-picker__grid-scroll {
  height: 200px;
}

.emoji-picker__grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 4px;
  padding: 8px;
}

.emoji-picker__item {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  transition: all 0.15s ease;
}

.emoji-picker__item:active {
  background: var(--ink-100);
  transform: scale(1.2);
}

.emoji-picker__item-text {
  font-size: 24px;
  line-height: 1;
}
</style>
