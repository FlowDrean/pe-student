<template>
  <div class="trade-page">
    <div class="trade-page__header">
      <div class="trade-page__brand">
        <h1 class="trade-page__title">二手市场</h1>
        <span class="trade-page__badge">Campus Market</span>
      </div>
      <div class="search-bar" @click="goSearch">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--ink-400)" stroke-width="2.5"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        <span class="search-bar__placeholder">搜索二手商品...</span>
      </div>
    </div>

    <div class="category-tabs scroll-hide">
      <button v-for="cat in categories" :key="cat.key" class="category-tab" :class="{ 'category-tab--active': activeCat === cat.key }" @click="activeCat = activeCat === cat.key ? '' : cat.key">
        <SvgIcon :name="cat.icon" :size="14" /> <span>{{ cat.label }}</span>
      </button>
    </div>

    <PullRefresh v-model="refreshing" @refresh="onRefresh">
      <List v-model:loading="loadingMore" :finished="finished" finished-text="— 没有更多了 —" :offset="100" @load="onLoad">
        <div class="trade-grid">
          <TradeCard
            v-for="item in items"
            :key="item.id"
            :trade="item"
            :show-actions="true"
            @click="goDetail(item.id)"
            @edit="goEdit"
            @delete="handleDelete"
          />
        </div>
      </List>
    </PullRefresh>

    <Loading v-if="initialLoading" variant="skeleton" :count="4" />
    <EmptyState v-if="!initialLoading && !items.length" title="暂无商品" description="快来这里淘好货吧" />

    <button class="fab-strip" @click="goCreate">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
      <span>发布商品</span>
    </button>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog, PullRefresh, List } from 'vant'
import { TRADE_CATEGORIES, SIZE_DEFAULT } from '@utils/constants'
import { getTrades, getTradeCategories, deleteTrade } from '@api'
import SvgIcon from '@components/common/SvgIcon.vue'
import TradeCard from '@components/trade/TradeCard.vue'
import Loading from '@components/common/Loading.vue'
import EmptyState from '@components/common/EmptyState.vue'

const router = useRouter()
const categories = ref(TRADE_CATEGORIES)
const activeCat = ref('')
const refreshing = ref(false)
const loadingMore = ref(false)
const finished = ref(false)
const initialLoading = ref(true)
const items = ref([])
const currentPage = ref(1)

async function fetchTrades(reset = false) {
  if (reset) currentPage.value = 1
  try {
    const params = { page: currentPage.value, size: SIZE_DEFAULT }
    if (activeCat.value) params.category = activeCat.value
    const data = await getTrades(params)
    if (reset) { items.value = data.list || [] } else { items.value = [...items.value, ...(data.list || [])] }
    finished.value = currentPage.value >= (data.pages || 1)
  } catch { if (reset) items.value = []; finished.value = true }
}

async function onRefresh() { await fetchTrades(true); refreshing.value = false; showToast('刷新成功') }
async function onLoad() { currentPage.value++; loadingMore.value = true; await fetchTrades(); loadingMore.value = false }

watch(activeCat, () => { currentPage.value = 1; finished.value = false; items.value = []; initialLoading.value = true; fetchTrades(true).then(() => { initialLoading.value = false }) })

function goSearch() { router.push('/search') }
function goDetail(id) { router.push(`/trade-item/${id}`) }
function goEdit(item) { router.push(`/trade-item/${item.id}/edit`) }
function goCreate() { router.push('/trade-item/create') }

async function handleDelete(item) {
  try { await showConfirmDialog({ title: '确认删除', message: `确定要删除「${item.title}」吗？删除后无法恢复。`, confirmButtonColor: '#dc2626' }) } catch { return }
  try { await deleteTrade(item.id); items.value = items.value.filter((i) => i.id !== item.id); showToast('已删除') } catch { showToast('删除失败，请重试') }
}

onMounted(async () => { await fetchTrades(true); initialLoading.value = false })
</script>

<style scoped>
.trade-page { min-height: 100vh; background: var(--bg-secondary); }

.trade-page__header {
  padding: var(--space-4) var(--content-padding);
  padding-top: calc(env(safe-area-inset-top) + var(--space-3));
  background: var(--bg-primary);
  border-bottom: 2px solid var(--ink-100);
  position: sticky;
  top: 0;
  z-index: 10;
}

.trade-page__brand {
  display: flex;
  align-items: baseline;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
}

.trade-page__title {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: var(--font-black);
  color: var(--ink-900);
  letter-spacing: -0.02em;
}

.trade-page__badge {
  font-family: var(--font-mono);
  font-size: var(--text-3xs);
  color: var(--ink-300);
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.search-bar { display: flex; align-items: center; gap: var(--space-2); padding: 10px 14px; background: var(--bg-tertiary); border: 2px solid transparent; border-radius: var(--radius-lg); cursor: pointer; transition: all var(--transition-fast); }
.search-bar:active { border-color: var(--ink-200); }
.search-bar__placeholder { font-size: var(--text-sm); color: var(--ink-400); font-style: italic; }

.category-tabs { display: flex; gap: var(--space-2); padding: var(--space-3) var(--content-padding); background: var(--bg-primary); border-bottom: 2px solid var(--ink-50); overflow-x: auto; }
.category-tab { display: flex; align-items: center; gap: 4px; padding: 7px 15px; border-radius: var(--radius-full); border: 2px solid var(--ink-100); background: var(--bg-primary); font-size: var(--text-sm); font-weight: var(--font-medium); color: var(--ink-500); white-space: nowrap; flex-shrink: 0; transition: all var(--transition-fast); }
.category-tab:active { transform: scale(0.95); }
.category-tab--active { background: var(--ink-900); border-color: var(--ink-900); color: #fff; font-weight: var(--font-semibold); }

.trade-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: var(--space-3); padding: var(--space-3) var(--content-padding); padding-bottom: var(--space-6); }

.fab-strip {
  position: fixed;
  right: var(--content-padding);
  bottom: calc(var(--tab-height) + 48px + env(safe-area-inset-bottom, 0px));
  height: 44px;
  padding: 0 20px;
  border-radius: 22px;

  /* ── 液态玻璃材质 ── */
  background-color: rgba(255, 255, 255, 0.88);
  background:
    linear-gradient(
      135deg,
      rgba(255, 255, 255, 0.82) 0%,
      rgba(7, 193, 96, 0.30) 35%,
      rgba(7, 193, 96, 0.18) 65%,
      rgba(255, 255, 255, 0.80) 100%
    );
  backdrop-filter: blur(18px) saturate(180%);
  -webkit-backdrop-filter: blur(18px) saturate(180%);

  /* 玻璃边框 */
  border: 0.5px solid rgba(255, 255, 255, 0.9);

  /* 玻璃光感阴影 */
  box-shadow:
    0 4px 24px rgba(7, 193, 96, 0.18),
    0 1px 4px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);

  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  color: var(--accent-600);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  z-index: 50;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.fab-strip:active {
  transform: scale(0.94);
  box-shadow:
    0 2px 12px rgba(7, 193, 96, 0.15),
    inset 0 2px 0 rgba(0, 0, 0, 0.10),
    inset 0 -1px 0 rgba(255, 255, 255, 0.3);
}
</style>
