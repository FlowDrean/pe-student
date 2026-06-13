<template>
  <div class="home-page">
    <!-- 顶部区域：Logo + 搜索 -->
    <div class="home-page__header">
      <div class="home-page__brand">
        <h1 class="home-page__logo">校园互助</h1>
        <span class="home-page__tagline">Campus · Mutual · Aid</span>
      </div>
      <div class="search-bar" @click="goSearch">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--ink-400)" stroke-width="2.5" stroke-linecap="round">
          <circle cx="11" cy="11" r="8" />
          <line x1="21" y1="21" x2="16.65" y2="16.65" />
        </svg>
        <span class="search-bar__placeholder">搜索帖子、商品...</span>
      </div>
    </div>

    <!-- Banner 轮播 -->
    <div v-if="banners.length" class="banner-wrapper">
      <Swipe
        :autoplay="3000"
        indicator-color="var(--accent-600)"
        :stop-propagation="false"
        class="banner-swipe"
      >
        <SwipeItem v-for="(banner, idx) in banners" :key="idx">
          <div
            class="banner-card"
            :style="{ background: banner.bg }"
            @click="goDetail(banner.id)"
          >
            <div class="banner-card__text">
              <span class="banner-card__tag">HOT</span>
              <h3 class="banner-card__title">{{ banner.title }}</h3>
              <p class="banner-card__desc">{{ banner.content }}</p>
            </div>
            <img
              v-if="banner.images && banner.images.length"
              :src="banner.images[0]"
              :alt="banner.title"
              class="banner-card__img"
              loading="lazy"
            />
          </div>
        </SwipeItem>
      </Swipe>
    </div>

    <!-- 分类标签 -->
    <div class="category-tabs scroll-hide">
      <button
        v-for="cat in categories"
        :key="cat.key"
        class="category-tab"
        :class="{ 'category-tab--active': activeCategory === cat.key }"
        @click="switchCategory(cat.key)"
      >
        <SvgIcon :name="cat.icon" :size="15" class="category-tab__icon" />
        <span>{{ cat.label }}</span>
      </button>
    </div>

    <!-- 帖子列表 -->
    <PullRefresh
      v-model="refreshing"
      success-text="刷新成功"
      pull-distance="80"
      @refresh="onRefresh"
    >
      <List
        v-model:loading="loadingMore"
        :finished="finished"
        finished-text="— 已经到底了 —"
        :offset="100"
        @load="onLoad"
      >
        <div ref="postListRef" class="post-list">
          <PostCard
            v-for="(post, index) in posts"
            :key="post.id"
            :post="post"
            :data-index="index"
            @like="handleLike(post.id)"
            @favorite="handleFavorite(post.id)"
            @comment="goDetail(post.id)"
            @preview="handlePreview"
            @click="goDetail(post.id)"
          />
        </div>
      </List>
    </PullRefresh>

    <!-- 加载状态 -->
    <Loading v-if="initialLoading" variant="skeleton" :count="4" />
    <EmptyState
      v-if="!initialLoading && posts.length === 0"
      title="暂无内容"
      description="快来发布第一条帖子吧"
      action-text="去发布"
      @action="goCreate"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showImagePreview, Swipe, SwipeItem, PullRefresh, List } from 'vant'
import { POST_CATEGORIES, SIZE_DEFAULT } from '@utils/constants'
import { listStagger, likeAnimation } from '@composables/useAnimation'
import { getPosts, getPostCategories, togglePostLike, togglePostFavorite, getHotPosts } from '@api'
import { usePostStore } from '@stores/post'
import SvgIcon from '@components/common/SvgIcon.vue'
import PostCard from '@components/post/PostCard.vue'
import Loading from '@components/common/Loading.vue'
import EmptyState from '@components/common/EmptyState.vue'

const router = useRouter()
const postStore = usePostStore()
const categories = ref(POST_CATEGORIES)

const banners = ref([])
const bannerColors = [
  'linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%)',
  'linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%)',
  'linear-gradient(135deg, #ecfdf5 0%, #d1fae5 100%)',
  'linear-gradient(135deg, #f5f3ff 0%, #ede9fe 100%)',
  'linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%)',
]

const activeCategory = ref('')
const refreshing = ref(false)
const loadingMore = ref(false)
const finished = ref(false)
const initialLoading = ref(true)
const posts = ref([])
const postListRef = ref(null)
const currentPage = ref(1)

async function fetchPosts(reset = false) {
  if (reset) currentPage.value = 1

  try {
    const params = {
      page: currentPage.value,
      size: SIZE_DEFAULT,
    }
    if (activeCategory.value) {
      params.category = activeCategory.value
    }

    const data = await getPosts(params)

    if (reset) {
      posts.value = data.list || []
    } else {
      posts.value = [...posts.value, ...(data.list || [])]
    }

    finished.value = currentPage.value >= (data.pages || 1)
  } catch {
    if (reset) posts.value = []
    finished.value = true
  }
}

async function onRefresh() {
  await fetchPosts(true)
  refreshing.value = false
  showToast('刷新成功')
}

async function onLoad() {
  currentPage.value++
  loadingMore.value = true
  await fetchPosts()
  loadingMore.value = false
}

async function switchCategory(key) {
  activeCategory.value = key === activeCategory.value ? '' : key
  initialLoading.value = true
  await fetchPosts(true)
  initialLoading.value = false
}

async function handleLike(postId) {
  try {
    await togglePostLike(postId)
    postStore.toggleLike(postId)
    // 同步更新本地 posts 状态（store 和本地 ref 不互通）
    const post = posts.value.find((p) => p.id === postId)
    if (post) {
      post.isLiked = !post.isLiked
      post.likes += post.isLiked ? 1 : -1
    }
    const el = document.querySelector(`[data-post-id="${postId}"] .post-card__action-btn`)
    if (el) likeAnimation(el)
  } catch {
    // 点赞失败静默处理
  }
}

async function handleFavorite(postId) {
  try {
    await togglePostFavorite(postId)
    postStore.toggleFavorite(postId)
    // 同步更新本地 posts 状态
    const post = posts.value.find((p) => p.id === postId)
    if (post) {
      post.isFavorited = !post.isFavorited
      showToast(post.isFavorited ? '已收藏' : '已取消收藏')
    }
  } catch {
    // 收藏失败静默处理
  }
}

function handlePreview(images, index) {
  if (images?.length) {
    showImagePreview({ images, startPosition: index, closeable: true })
  }
}

function goSearch() { router.push('/search') }
function goDetail(id) { router.push(`/post/${id}`) }
function goCreate() { router.push('/post/create') }

async function fetchBanners() {
  try {
    const data = await getHotPosts({ page: 1, size: 5 })
    if (data.list && data.list.length) {
      banners.value = data.list.slice(0, 5).map((post, idx) => ({
        ...post,
        bg: bannerColors[idx % bannerColors.length],
        content: (post.content || '').slice(0, 40) + ((post.content || '').length > 40 ? '...' : ''),
      }))
    }
  } catch {
    // Banner 加载失败不影响主列表
  }
}

onMounted(async () => {
  await Promise.all([fetchPosts(true), fetchBanners()])
  initialLoading.value = false

  nextTick(() => {
    const items = document.querySelectorAll('.post-card')
    if (items.length) listStagger(items, { stagger: 0.05, fromY: 20 })
  })
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: var(--bg-secondary);
}

/* ── 顶部品牌区 ── */
.home-page__header {
  padding: var(--space-4) var(--content-padding);
  padding-top: calc(env(safe-area-inset-top) + var(--space-3));
  background: var(--bg-primary);
  border-bottom: 2px solid var(--ink-100);
  position: sticky;
  top: 0;
  z-index: 10;
}

.home-page__brand {
  display: flex;
  align-items: baseline;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
}

.home-page__logo {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: var(--font-black);
  color: var(--ink-900);
  letter-spacing: -0.02em;
  line-height: 1.1;
}

.home-page__tagline {
  font-family: var(--font-mono);
  font-size: var(--text-3xs);
  color: var(--ink-300);
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

/* ── 搜索栏 ── */
.search-bar {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: 10px 14px;
  background: var(--bg-tertiary);
  border: 2px solid transparent;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.search-bar:active {
  border-color: var(--ink-200);
}

.search-bar__placeholder {
  font-size: var(--text-sm);
  color: var(--ink-400);
  font-style: italic;
}

/* ── Banner ── */
.banner-wrapper {
  padding: var(--space-3) var(--content-padding);
  background: var(--bg-primary);
}

.banner-swipe {
  border-radius: var(--radius-xl);
  overflow: hidden;
  border: 2px solid var(--ink-100);
}

.banner-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4) var(--space-5);
  min-height: 120px;
  cursor: pointer;
  transition: transform var(--transition-fast);
}

.banner-card:active {
  transform: scale(0.98);
}

.banner-card__text {
  flex: 1;
  padding-right: var(--space-3);
}

.banner-card__tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: var(--radius-xs);
  background: rgba(255, 255, 255, 0.6);
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: var(--font-bold);
  color: var(--accent-700);
  letter-spacing: 0.08em;
  margin-bottom: var(--space-2);
}

.banner-card__title {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--ink-800);
  margin-bottom: var(--space-1);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.banner-card__desc {
  font-size: var(--text-sm);
  color: var(--ink-500);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.banner-card__img {
  width: 96px;
  height: 72px;
  border-radius: var(--radius-lg);
  object-fit: cover;
  flex-shrink: 0;
  border: 2px solid rgba(255, 255, 255, 0.6);
}

/* ── 分类标签 ── */
.category-tabs {
  display: flex;
  gap: var(--space-2);
  padding: var(--space-3) var(--content-padding);
  background: var(--bg-primary);
  border-bottom: 2px solid var(--ink-50);
  overflow-x: auto;
}

.category-tab {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 7px 15px;
  border-radius: var(--radius-full);
  border: 2px solid var(--ink-100);
  background: var(--bg-primary);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--ink-500);
  white-space: nowrap;
  transition: all var(--transition-fast);
  flex-shrink: 0;
}

.category-tab:active {
  transform: scale(0.95);
}

.category-tab--active {
  background: var(--ink-900);
  border-color: var(--ink-900);
  color: #fff;
  font-weight: var(--font-semibold);
}

/* ── 帖子列表 ── */
.post-list {
  padding: 0 var(--content-padding);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  padding-top: var(--space-3);
  padding-bottom: var(--space-4);
}

:deep(.van-pull-refresh) {
  min-height: 50vh;
}
</style>
