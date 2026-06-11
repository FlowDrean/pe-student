<template>
  <div class="fav-page page">
    <div class="fav-switch">
      <button class="fav-switch__btn" :class="{ 'fav-switch__btn--active': activeTab === 0 }" @click="activeTab = 0">收藏帖子</button>
      <button class="fav-switch__btn" :class="{ 'fav-switch__btn--active': activeTab === 1 }" @click="switchToTrade">收藏商品</button>
    </div>
    <div v-show="activeTab === 0">
      <Loading v-if="loadingPost" variant="skeleton" :count="3" />
      <div v-else-if="favPosts.length" class="fav-content">
        <PostCard v-for="post in favPosts" :key="post.id" :post="post" @click="goPost(post.id)" />
      </div>
      <EmptyState v-else title="暂无收藏的帖子" description="去首页发现精彩内容吧" />
    </div>
    <div v-show="activeTab === 1">
      <Loading v-if="loadingTrade" variant="skeleton" :count="4" />
      <div v-else-if="favTrades.length" class="fav-content-grid">
        <TradeCard v-for="item in favTrades" :key="item.id" :trade="item" @click="goTrade(item.id)" />
      </div>
      <EmptyState v-else title="暂无收藏的商品" description="去市场逛逛吧" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getUserFavorites } from '@api/user'
import PostCard from '@components/post/PostCard.vue'
import TradeCard from '@components/trade/TradeCard.vue'
import Loading from '@components/common/Loading.vue'
import EmptyState from '@components/common/EmptyState.vue'

const router = useRouter()
const activeTab = ref(0)
const favPosts = ref([])
const favTrades = ref([])
const loadingPost = ref(true)
const loadingTrade = ref(true)
const loadedTrade = ref(false)

async function loadFavPosts() {
  loadingPost.value = true
  try { const data = await getUserFavorites({ targetType: 1, page: 1, size: 50 }); favPosts.value = data.list || [] }
  catch { /* ignore */ }
  finally { loadingPost.value = false }
}

async function switchToTrade() {
  activeTab.value = 1
  if (loadedTrade.value) return
  loadingTrade.value = true
  try { const data = await getUserFavorites({ targetType: 2, page: 1, size: 50 }); favTrades.value = data.list || []; loadedTrade.value = true }
  catch { /* ignore */ }
  finally { loadingTrade.value = false }
}

loadFavPosts()

function goPost(id) { router.push(`/post/${id}`) }
function goTrade(id) { router.push(`/trade-item/${id}`) }
</script>

<style scoped>
.fav-switch { display: flex; gap: var(--space-1); padding: var(--space-3) var(--content-padding); background: var(--bg-primary); border-bottom: 2px solid var(--ink-100); position: sticky; top: 0; z-index: 10; }
.fav-switch__btn { flex: 1; padding: 9px 0; border-radius: var(--radius-md); border: 2px solid var(--ink-200); background: var(--bg-primary); font-size: var(--text-sm); font-weight: var(--font-medium); color: var(--ink-500); text-align: center; transition: all var(--transition-fast); cursor: pointer; }
.fav-switch__btn:active { transform: scale(0.97); }
.fav-switch__btn--active { background: var(--ink-900); border-color: var(--ink-900); color: #fff; font-weight: var(--font-semibold); }
.fav-content { padding: var(--space-3) var(--content-padding); display: flex; flex-direction: column; gap: var(--space-3); min-height: 50vh; }
.fav-content-grid { padding: var(--space-3) var(--content-padding); display: grid; grid-template-columns: repeat(2, 1fr); gap: var(--space-3); min-height: 50vh; }
</style>
