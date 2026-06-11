<template>
  <div class="search-page page">
    <div class="search-page__input">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--ink-400)" stroke-width="2.5"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
      <input
        ref="inputRef"
        v-model="keyword"
        class="search-input"
        :placeholder="activeTab === 'post' ? '搜索帖子...' : '搜索商品...'"
        autofocus
        @input="onInput"
        @keyup.enter="doSearch"
      />
      <span v-if="keyword" class="search-clear" @click="clearSearch">✕</span>
    </div>

    <div class="search-tabs">
      <button class="search-tab" :class="{ 'search-tab--active': activeTab === 'post' }" @click="switchTab('post')">帖子</button>
      <button class="search-tab" :class="{ 'search-tab--active': activeTab === 'trade' }" @click="switchTab('trade')">商品</button>
    </div>

    <div v-if="!keyword && !hasSearched" class="search-hot">
      <h3 class="search-hot__title">🔥 热门搜索</h3>
      <div class="search-hot__tags">
        <span v-for="tag in hotTags" :key="tag" class="tag tag-ink" @click="quickSearch(tag)">{{ tag }}</span>
      </div>
    </div>

    <div v-else class="search-results">
      <Loading v-if="loading" variant="skeleton" :count="3" />
      <template v-else-if="activeTab === 'post'">
        <div v-if="postResults.length" class="search-results__list">
          <PostCard v-for="post in postResults" :key="post.id" :post="post" @click="goPostDetail(post.id)" />
        </div>
        <EmptyState v-else title="未找到相关帖子" description="换个关键词试试吧" />
      </template>
      <template v-else>
        <div v-if="tradeResults.length" class="search-results__grid">
          <TradeCard v-for="trade in tradeResults" :key="trade.id" :trade="trade" @click="goTradeDetail(trade.id)" />
        </div>
        <EmptyState v-else title="未找到相关商品" description="换个关键词试试吧" />
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import PostCard from '@components/post/PostCard.vue'
import TradeCard from '@components/trade/TradeCard.vue'
import Loading from '@components/common/Loading.vue'
import EmptyState from '@components/common/EmptyState.vue'
import { searchPosts } from '@api/post'
import { searchTrades } from '@api/trade'

const router = useRouter()
const inputRef = ref(null)
const keyword = ref('')
const activeTab = ref('post')
const loading = ref(false)
const hasSearched = ref(false)
const postResults = ref([])
const tradeResults = ref([])

const hotTags = ['火锅', '高数复习', '二手书', '约球', '图书馆', '合租', '考研', '兼职']

let debounceTimer = null

function switchTab(tab) { activeTab.value = tab; if (keyword.value) doSearch() }
function onInput() { clearTimeout(debounceTimer); debounceTimer = setTimeout(() => { if (keyword.value.trim()) doSearch() }, 400) }
function quickSearch(tag) { keyword.value = tag; doSearch() }
function clearSearch() { keyword.value = ''; postResults.value = []; tradeResults.value = []; hasSearched.value = false; inputRef.value?.focus() }

async function doSearch() {
  const kw = keyword.value.trim()
  if (!kw) return
  loading.value = true; hasSearched.value = true
  try {
    if (activeTab.value === 'post') { const data = await searchPosts({ keyword: kw, page: 1, size: 20 }); postResults.value = data.list || [] }
    else { const data = await searchTrades({ keyword: kw, page: 1, size: 20 }); tradeResults.value = data.list || [] }
  } catch { /* ignore */ }
  finally { loading.value = false }
}

function goPostDetail(id) { router.push(`/post/${id}`) }
function goTradeDetail(id) { router.push(`/trade-item/${id}`) }
</script>

<style scoped>
.search-page__input { display: flex; align-items: center; gap: var(--space-2); padding: var(--space-3) var(--content-padding); background: var(--bg-primary); border-bottom: 2px solid var(--ink-100); }
.search-input { flex: 1; padding: 10px 0; font-size: var(--text-base); color: var(--ink-800); }
.search-clear { padding: 2px 6px; color: var(--ink-400); cursor: pointer; font-size: var(--text-sm); }
.search-clear:active { color: var(--ink-600); }
.search-tabs { display: flex; padding: 0 var(--content-padding); background: var(--bg-primary); border-bottom: 2px solid var(--ink-50); }
.search-tab { position: relative; padding: 10px 20px; font-size: var(--text-sm); font-weight: var(--font-medium); color: var(--ink-400); cursor: pointer; transition: color var(--transition-fast); }
.search-tab--active { color: var(--accent-600); font-weight: var(--font-semibold); }
.search-tab--active::after { content: ''; position: absolute; bottom: -2px; left: 20px; right: 20px; height: 2.5px; background: var(--accent-600); border-radius: 2px; }
.search-hot { padding: var(--space-6) var(--content-padding); }
.search-hot__title { font-family: var(--font-display); font-size: var(--text-lg); font-weight: var(--font-bold); margin-bottom: var(--space-4); color: var(--ink-800); }
.search-hot__tags { display: flex; flex-wrap: wrap; gap: var(--space-2); }
.search-hot__tags .tag { cursor: pointer; padding: 8px 16px; font-size: var(--text-sm); font-weight: var(--font-medium); transition: transform var(--transition-fast), background var(--transition-fast); }
.search-hot__tags .tag:active { transform: scale(0.95); background: var(--ink-100); }
.search-results { padding: var(--space-3) var(--content-padding); min-height: 50vh; }
.search-results__list { display: flex; flex-direction: column; gap: var(--space-3); }
.search-results__grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: var(--space-3); }
</style>
