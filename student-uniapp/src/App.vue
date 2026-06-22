<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useAppStore } from '@stores/app'

const appStore = useAppStore()

onLaunch(() => {
  appStore.init()
})

onShow(() => {
  appStore.onShow()
})

onHide(() => {
  appStore.onHide()
})
</script>

<style>
/* 全局 CSS 变量 — Editorial Ink Design System */
page {
  --ink-50: #f4f4f6;
  --ink-100: #e4e4e7;
  --ink-200: #c9c9cf;
  --ink-300: #a1a1aa;
  --ink-400: #71717a;
  --ink-500: #52525b;
  --ink-600: #3f3f46;
  --ink-700: #27272a;
  --ink-800: #18181b;
  --ink-900: #0f1119;
  --accent-50: #e6f9f0;
  --accent-100: #b3efd1;
  --accent-200: #8de5b9;
  --accent-300: #59d899;
  --accent-400: #38cf81;
  --accent-500: #07C160;
  --accent-600: #06af56;
  --bg-primary: #ffffff;
  --bg-secondary: #f4f4f6;
  --bg-tertiary: #f8f9fa;
  --text-primary: #0f1119;
  --text-secondary: #52525b;
  --text-muted: #a1a1aa;
  --error: #dc2626;
  --success: #07C160;
  --warning: #f59e0b;
  --font-display: system-ui, -apple-system, sans-serif;
  --font-sans: system-ui, -apple-system, sans-serif;
  --font-mono: 'JetBrains Mono', 'Courier New', monospace;
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --radius-full: 9999px;
  --shadow-card: 0 2px 0 0 rgba(15, 17, 25, 0.06);
  --shadow-md: 0 4px 6px -1px rgba(15, 17, 25, 0.06);
  --header-height: 44px;
  background-color: var(--bg-secondary);
  font-family: var(--font-sans);
  color: var(--text-primary);
  font-size: 16px;
  line-height: 1.5;
}
</style>
