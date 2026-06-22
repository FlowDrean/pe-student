<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    name: string
    size?: number | string
    color?: string
  }>(),
  {
    size: 24,
  },
)

/** IconChar name → wd-icon name 映射表 */
const iconMap: Record<string, string> = {
  help: 'help-circle',
  food: 'goods',
  study: 'books',
  life: 'home',
  chat: 'chat',
  book: 'books',
  electronics: 'computer',
  daily: 'tools',
  clothing: 'shop',
  package: 'goods',
  heart: 'heart',
  'message-circle': 'chat',
  user: 'user',
  bell: 'notification',
  'shopping-cart': 'cart',
  edit: 'edit',
  'file-text': 'file',
  star: 'star',
  settings: 'setting',
  trash: 'delete',
  lock: 'lock-on',
  info: 'info-circle',
  home: 'home',
  plus: 'add',
  search: 'search',
  trending: 'chart-bubble',
  share: 'share',
  back: 'arrow-left',
  send: 'enter',
  shop: 'shop',
  clock: 'clock',
  check: 'check',
  close: 'close',
  arrowLeft: 'arrow-left',
  arrowRight: 'arrow-right',
  calendar: 'calendar',
  location: 'location',
  phone: 'phone',
  mail: 'mail',
  link: 'link',
  tag: 'discount',
  camera: 'camera',
  image: 'picture',
  play: 'play',
  pause: 'pause-circle',
  volume: 'sound',
  mute: 'close',
  download: 'download',
  upload: 'upload',
  refresh: 'refresh',
  filter: 'filter',
  grid: 'view-module',
  list: 'view-list',
  eye: 'browse',
  'eye-off': 'browse-off',
  thumbsUp: 'thumb-up',
  thumbsDown: 'thumb-down',
  bookmark: 'pin',
  award: 'flag',
  flame: 'chart-bubble',
  zap: 'poweroff',
  shield: 'secured',
  globe: 'internet',
  map: 'location',
  compass: 'location',
  sun: 'star',
  moon: 'time',
  cloud: 'cloud',
  rain: 'cloud-download',
  snow: 'cloud',
  wind: 'move',
}

const iconName = computed(() => iconMap[props.name] || 'file')

const iconSize = computed(() => {
  const s = props.size
  if (typeof s === 'number') return `${s}px`
  return s
})
</script>

<template>
  <view class="icon-char">
    <wd-icon :name="iconName" :size="iconSize" :color="color" />
  </view>
</template>

<style>
.icon-char {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  vertical-align: middle;
}
</style>
