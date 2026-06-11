<template>
  <div class="list-page">
    <Loading v-if="loading" variant="skeleton" :count="4" />
    <template v-else>
      <TradeCard v-for="item in items" :key="item.id" :trade="item" @click="goDetail(item.id)" />
      <EmptyState v-if="!items.length" title="暂无商品" description="快去发布第一件商品吧" action-text="去发布" @action="goCreate" />
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@stores/user'
import { getUserTrades } from '@api/user'
import TradeCard from '@components/trade/TradeCard.vue'
import Loading from '@components/common/Loading.vue'
import EmptyState from '@components/common/EmptyState.vue'

const router = useRouter()
const userStore = useUserStore()

const items = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const data = await getUserTrades(userStore.userId, { page: 1, size: 50 })
    items.value = data.list || []
  } catch {
    // 加载失败静默处理
  } finally {
    loading.value = false
  }
})

function goDetail(id) { router.push(`/trade-item/${id}`) }
function goCreate() { router.push('/trade-item/create') }
</script>

<style scoped>
.list-page { padding: var(--space-3) var(--content-padding); display: grid; grid-template-columns: repeat(2, 1fr); gap: var(--space-3); min-height: 50vh; }
</style>
