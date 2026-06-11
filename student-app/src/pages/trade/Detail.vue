<template>
  <div class="trade-detail page-white" v-if="item">
    <div class="trade-detail__images">
      <img v-if="item.images && item.images.length" :src="item.images[activeImage]" class="trade-detail__img-real" loading="lazy" @click="previewImage(activeImage)" />
      <div v-else class="trade-detail__img-placeholder">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="var(--ink-200)" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
      </div>
    </div>
    <div v-if="item.images && item.images.length > 1" class="trade-detail__thumbs scroll-hide">
      <img v-for="(img, idx) in item.images" :key="idx" :src="img" class="trade-detail__thumb" :class="{ 'trade-detail__thumb--active': activeImage === idx }" loading="lazy" @click="activeImage = idx" />
    </div>
    <div class="trade-detail__info">
      <PriceTag :price="item.price" :original-price="item.originalPrice" />
      <h1 class="trade-detail__title">{{ item.title }}</h1>
      <p class="trade-detail__desc">{{ item.description || '暂无描述' }}</p>
      <div class="trade-detail__meta">
        <span class="tag tag-secondary">{{ conditionLabel }}</span>
        <span class="trade-detail__views">{{ item.views }} 次浏览</span>
      </div>
    </div>
    <div class="trade-detail__seller">
      <div class="avatar avatar-md" :style="{ background: 'var(--accent-600)' }" @click="goUserProfile"><span class="avatar-inner">{{ item.user?.nickname?.charAt(0) }}</span></div>
      <div class="trade-detail__seller-info">
        <span class="trade-detail__seller-name">{{ item.user?.nickname }}</span>
        <span class="trade-detail__seller-time">发布于 {{ timeAgo(item.createdAt) }}</span>
      </div>
    </div>
    <div v-if="!isOwner" class="trade-detail__actions">
      <button class="btn btn-outline" style="flex:1" @click="handleFavorite">
        <svg width="18" height="18" viewBox="0 0 24 24" :fill="isFav ? 'var(--amber-500)' : 'none'" :stroke="isFav ? 'var(--amber-500)' : 'currentColor'" stroke-width="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
        {{ isFav ? '已收藏' : '收藏' }}
      </button>
      <button class="btn btn-primary" style="flex:2" @click="handleContact">联系卖家</button>
    </div>
    <div v-if="isOwner" class="trade-detail__owner-actions">
      <button class="btn btn-outline btn-sm" @click="handleEdit">编辑</button>
      <button class="btn btn-ghost btn-sm" style="color:var(--error)" @click="handleDelete">删除</button>
    </div>
  </div>
  <Loading v-else variant="skeleton" :count="2" />
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog, showImagePreview } from 'vant'
import { timeAgo } from '@utils/format'
import { CONDITIONS } from '@utils/constants'
import { getTradeDetail, toggleTradeFavorite, deleteTrade } from '@api'
import { useUserStore } from '@stores/user'
import PriceTag from '@components/trade/PriceTag.vue'
import Loading from '@components/common/Loading.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isFav = ref(false)
const item = ref(null)
const loading = ref(true)
const activeImage = ref(0)

const conditionLabel = computed(() => CONDITIONS.find(c => c.key === item.value?.condition)?.label || '')
const isOwner = computed(() => userStore.isLogin && item.value && (userStore.userId === item.value.user?.id || userStore.userId === item.value.userId))

async function handleFavorite() {
  try { await toggleTradeFavorite(item.value.id); isFav.value = !isFav.value; showToast(isFav.value ? '已收藏' : '已取消') } catch { /* ignore */ }
}
function previewImage(index) {
  if (item.value?.images?.length) {
    showImagePreview({ images: item.value.images, startPosition: index, closeable: true })
  }
}

function goUserProfile() {
  const userId = item.value?.user?.id || item.value?.userId
  if (userId) router.push(`/user-profile/${userId}`)
}

function handleContact() {
  const sellerId = item.value?.user?.id || item.value?.userId
  if (!sellerId) {
    showToast('无法获取卖家信息')
    return
  }
  if (!userStore.isLogin) {
    showToast('请先登录')
    router.push('/auth/login')
    return
  }
  router.push(`/chat/${sellerId}`)
}
function handleEdit() { router.push(`/trade-item/${item.value.id}/edit`) }
async function handleDelete() {
  try { await showConfirmDialog({ title: '确认删除', message: '删除后无法恢复，确定要删除这个商品吗？', confirmButtonColor: '#dc2626' }) } catch { return }
  try { await deleteTrade(item.value.id); showToast('已删除'); router.replace('/trade') } catch { showToast('删除失败，请重试') }
}

onMounted(async () => {
  try { item.value = await getTradeDetail(route.params.id) } catch { showToast('获取商品详情失败') }
  finally { loading.value = false }
})
</script>

<style scoped>
.trade-detail__images { background: var(--ink-50); aspect-ratio: 1; display: flex; align-items: center; justify-content: center; overflow: hidden; }
.trade-detail__img-real { width: 100%; height: 100%; object-fit: cover; }
.trade-detail__thumbs { display: flex; gap: var(--space-2); padding: var(--space-3) var(--content-padding); overflow-x: auto; background: var(--bg-primary); border-bottom: 2px solid var(--ink-50); }
.trade-detail__thumb { width: 56px; height: 56px; border-radius: var(--radius-md); object-fit: cover; border: 2px solid var(--ink-200); flex-shrink: 0; cursor: pointer; transition: border-color var(--transition-fast); }
.trade-detail__thumb--active { border-color: var(--accent-600); }
.trade-detail__info { padding: var(--space-4) var(--content-padding); }
.trade-detail__title { font-family: var(--font-display); font-size: var(--text-xl); font-weight: var(--font-bold); margin: var(--space-3) 0; color: var(--ink-900); letter-spacing: -0.01em; }
.trade-detail__desc { font-size: var(--text-sm); color: var(--ink-500); line-height: 1.7; margin-bottom: var(--space-3); }
.trade-detail__meta { display: flex; align-items: center; gap: var(--space-3); }
.trade-detail__views { font-size: var(--text-xs); color: var(--ink-400); }
.trade-detail__seller { display: flex; align-items: center; gap: var(--space-3); padding: var(--space-4) var(--content-padding); border-top: 6px solid var(--bg-secondary); border-bottom: 6px solid var(--bg-secondary); }
.avatar { cursor: pointer; }
.avatar-inner { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; color: #fff; font-family: var(--font-display); font-weight: var(--font-bold); border-radius: var(--radius-full); }
.trade-detail__seller-info { display: flex; flex-direction: column; gap: 2px; }
.trade-detail__seller-name { font-weight: var(--font-semibold); color: var(--ink-800); }
.trade-detail__seller-time { font-size: var(--text-xs); color: var(--ink-400); }
.trade-detail__actions { display: flex; gap: var(--space-3); padding: var(--space-4) var(--content-padding); padding-bottom: calc(var(--space-4) + env(safe-area-inset-bottom)); position: fixed; bottom: 0; left: 0; right: 0; background: var(--bg-primary); border-top: 2px solid var(--ink-100); z-index: 10; }
.trade-detail__owner-actions { display: flex; gap: var(--space-3); padding: var(--space-3) var(--content-padding); padding-bottom: calc(var(--space-4) + env(safe-area-inset-bottom)); }
</style>
