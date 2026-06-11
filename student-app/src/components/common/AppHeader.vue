<template>
  <header class="app-header" :class="{ 'app-header--transparent': transparent }">
    <div class="app-header__left">
      <button
        v-if="showBack"
        class="app-header__back"
        @click="$emit('back')"
        aria-label="返回"
      >
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M15 18l-6-6 6-6" />
        </svg>
      </button>
    </div>

    <h1 v-if="title" class="app-header__title text-ellipsis">{{ title }}</h1>

    <div class="app-header__right">
      <button
        v-if="rightIcon"
        class="app-header__action"
        @click="$emit('right-click')"
        aria-label="更多"
      >
        <slot name="right">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <circle cx="12" cy="5" r="1.5" />
            <circle cx="12" cy="12" r="1.5" />
            <circle cx="12" cy="19" r="1.5" />
          </svg>
        </slot>
      </button>
      <slot name="extra" />
    </div>
  </header>
</template>

<script setup>
defineProps({
  title: { type: String, default: '' },
  showBack: { type: Boolean, default: true },
  rightIcon: { type: [Boolean, String], default: false },
  transparent: { type: Boolean, default: false },
})

defineEmits(['back', 'right-click'])
</script>

<style scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: var(--z-header);
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: var(--header-height);
  padding: 0 var(--content-padding);
  padding-top: env(safe-area-inset-top);
  background: var(--bg-primary);
  border-bottom: 2px solid var(--ink-100);
  backdrop-filter: blur(12px);
}

.app-header--transparent {
  background: transparent;
  border-bottom-color: transparent;
}

.app-header__left,
.app-header__right {
  display: flex;
  align-items: center;
  min-width: 48px;
}

.app-header__right {
  justify-content: flex-end;
}

.app-header__back {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  margin-left: -8px;
  border-radius: var(--radius-md);
  color: var(--ink-700);
  transition: all var(--transition-fast);
}

.app-header__back:active {
  background: var(--ink-50);
  transform: scale(0.92);
}

.app-header__title {
  flex: 1;
  text-align: center;
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--ink-800);
  letter-spacing: 0.02em;
}

.app-header__action {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  color: var(--ink-600);
  transition: all var(--transition-fast);
}

.app-header__action:active {
  background: var(--ink-50);
  transform: scale(0.92);
}
</style>
