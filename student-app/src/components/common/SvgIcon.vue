<template>
  <svg
    :width="size"
    :height="size"
    :viewBox="viewBox"
    fill="none"
    :stroke="stroke ? color : 'none'"
    :fill="fill ? color : 'none'"
    :stroke-width="strokeWidth"
    stroke-linecap="round"
    stroke-linejoin="round"
    class="svg-icon"
  >
    <path v-for="(d, i) in paths" :key="i" :d="d" />
  </svg>
</template>

<script setup>
import { computed } from 'vue'
import { getIcon } from '@utils/icons'

const props = defineProps({
  name: { type: String, required: true },
  size: { type: [Number, String], default: 20 },
  color: { type: String, default: 'currentColor' },
  strokeWidth: { type: Number, default: 1.8 },
})

const icon = computed(() => getIcon(props.name, Number(props.size), props.color, props.strokeWidth))

const viewBox = computed(() => icon.value?.viewBox || '0 0 24 24')
const paths = computed(() => icon.value?.paths || [])
const stroke = computed(() => icon.value?.stroke !== false)
const fill = computed(() => icon.value?.fill === true)
</script>

<style scoped>
.svg-icon {
  flex-shrink: 0;
  vertical-align: middle;
}
</style>
