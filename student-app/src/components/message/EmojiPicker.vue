<template>
  <div class="emoji-picker">
    <!-- 分类标签 -->
    <div class="emoji-picker__tabs">
      <button
        v-for="cat in categories"
        :key="cat.key"
        class="emoji-picker__tab"
        :class="{ active: activeTab === cat.key }"
        @click="activeTab = cat.key"
      >
        {{ cat.icon }}
      </button>
    </div>
    <!-- emoji 网格 -->
    <div class="emoji-picker__grid" ref="gridRef">
      <button
        v-for="emoji in currentEmojis"
        :key="emoji"
        class="emoji-picker__item"
        @click="$emit('select', emoji)"
      >
        {{ emoji }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

defineEmits(['select'])

const activeTab = ref('smile')

const categories = [
  { key: 'smile', icon: '😂', label: '表情' },
  { key: 'gesture', icon: '👋', label: '手势' },
  { key: 'heart', icon: '❤️', label: '爱心' },
  { key: 'animal', icon: '🐶', label: '动物' },
  { key: 'food', icon: '🍔', label: '食物' },
  { key: 'item', icon: '⚽', label: '物品' },
]

const emojiMap = {
  smile: ['😀','😃','😄','😁','😆','😅','🤣','😂','🙂','😊','😇','🥰','😍','🤩','😘','😗','😚','😋','😛','😜','🤪','😝','🤑','🤗','🤭','🤫','🤔','🤐','🤨','😐','😑','😶','😏','😒','🙄','😬','🤥','😌','😔','😪','🤤','😴','😷','🤒','🤕','🤢','🤮','🥴','😵','🤯','🥳','🥺','😢','😭','😤','😠','😡','🤬','💀','☠️','💩','🤡','👹','👺','👻','👽','🤖'],
  gesture: ['👋','🤚','🖐','✋','🖖','👌','🤌','🤏','✌','🤞','🤟','🤘','🤙','👈','👉','👆','🖕','👇','☝','👍','👎','✊','👊','🤛','🤜','👏','🙌','👐','🤲','🤝','🙏','✍','💅','🤳','💪','🦵','🦶','👂','🦻','👃','🧠','🫀','🫁','🦷','👀','👁','👅','👄'],
  heart: ['❤️','🧡','💛','💚','💙','💜','🖤','🤍','🤎','💔','❣','💕','💞','💓','💗','💖','💘','💝','💟','♥','💌','💋','🫂','👥','💑','💏','👪','💍','💎'],
  animal: ['🐶','🐱','🐭','🐹','🐰','🦊','🐻','🐼','🐻‍❄','🐨','🐯','🦁','🐮','🐷','🐸','🐵','🙈','🙉','🙊','🐒','🐔','🐧','🐦','🐤','🐣','🐥','🦆','🦅','🦉','🦇','🐺','🐗','🐴','🦄','🐝','🪱','🐛','🦋','🐌','🐞','🐜','🪰','🪲','🪳','🦟','🦗','🕷','🦂','🐢','🐍','🦎','🦖','🦕','🐙','🦑','🦐','🦞','🦀','🐡','🐠','🐟','🐬','🐳','🐋','🦈','🐊','🐅','🐆','🦓','🦍','🦧','🐘','🦛','🦏','🐪','🐫','🦒','🦘','🐃','🐂','🐄','🐎','🐖','🐏','🐑','🦙','🐐','🦌','🐕','🐩','🦮','🐕‍🦺','🐈','🐈‍⬛','🪶','🐓','🦃','🦤','🦚','🦜','🦢','🦩'],
  food: ['🍔','🍟','🍕','🌭','🍿','🧈','🥞','🧇','🧀','🥓','🥩','🍗','🍖','🦴','🌮','🌯','🥙','🧆','🥚','🍳','🥘','🍲','🫕','🥣','🥗','🍝','🍜','🍲','🍛','🍣','🍱','🥟','🦪','🍤','🍙','🍚','🍘','🍥','🥠','🥮','🍢','🍡','🍧','🍨','🍦','🥧','🧁','🍰','🎂','🍮','🍭','🍬','🍫','🍿','🍩','🍪','🌰','🥜','🍯','🥛','🍼','🫖','☕','🍵','🧃','🥤','🧋','🍶','🍺','🍻','🥂','🍷','🥃','🍸','🍹','🧉','🍾','🧊','🥄','🍴','🍽','🥢'],
  item: ['⚽','🏀','🏈','⚾','🥎','🎾','🏐','🏉','🥏','🎱','🪀','🏓','🏸','🏒','🏑','🥍','🏏','🪃','🥅','⛳','🪁','🏹','🎣','🤿','🥊','🥋','🎽','🛹','🛼','🛷','⛸','🥌','🎿','⛷','🏂','🪂','🏋','🤼','🤸','🤺','⛹','🤾','🏌','🏇','🧘','🏄','🏊','🤽','🚣','🧗','🚵','🚴','🏆','🥇','🥈','🥉','🏅','🎖','🏵','🎗','🎫','🎟','🎪','🤹','🎭','🩰','🎨','🎬','🎤','🎧','🎼','🎹','🥁','🪘','🎷','🎺','🪗','🎸','🪕','🎻','🎲','♟','🎯','🎳','🎮','🕹','🎰'],
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
.emoji-picker__tabs {
  display: flex;
  gap: 2px;
  padding: 8px 8px 0;
  overflow-x: auto;
}
.emoji-picker__tab {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  font-size: 18px;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: background var(--transition-fast);
}
.emoji-picker__tab:active,
.emoji-picker__tab.active {
  background: var(--ink-100);
}
.emoji-picker__grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 4px;
  padding: 8px;
  max-height: 200px;
  overflow-y: auto;
}
.emoji-picker__item {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  background: transparent;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background var(--transition-fast);
  padding: 0;
}
.emoji-picker__item:active {
  background: var(--ink-100);
  transform: scale(1.2);
}
</style>
