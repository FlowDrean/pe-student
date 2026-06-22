<template>
  <view class="my-posts-page">
    <!-- Loading -->
    <view v-if="loading" class="my-posts-page__skeleton">
      <view v-for="i in 3" :key="i" class="my-posts-page__skeleton-item">
        <view class="skeleton my-posts-page__skeleton-line" style="width:60%" />
        <view class="skeleton my-posts-page__skeleton-line" style="width:100%" />
        <view class="skeleton my-posts-page__skeleton-line" style="width:80%" />
      </view>
    </view>

    <!-- Post List -->
    <scroll-view
      v-else-if="posts.length > 0"
      class="my-posts-page__list"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
    >
      <view class="my-posts-page__list-items">
        <view
          v-for="post in posts"
          :key="post.id"
          class="my-posts-page__item-wrapper"
        >
          <view
            class="my-posts-page__item"
            @tap="goDetail(post.id)"
            @longpress="showActions(post)"
          >
            <PostCard :post="post" />
          </view>
        </view>
      </view>
      <view v-if="hasMore" class="my-posts-page__load-more">
        <text class="my-posts-page__load-more-text">加载更多...</text>
      </view>
      <view v-else class="my-posts-page__no-more">
        <text class="my-posts-page__no-more-text">没有更多了</text>
      </view>
      <view class="my-posts-page__safe-bottom" />
    </scroll-view>

    <!-- Empty -->
    <view v-else class="my-posts-page__empty">
      <view class="my-posts-page__empty-icon">
        <wd-icon name="edit" size="48" color="var(--ink-200)" />
      </view>
      <text class="my-posts-page__empty-title">暂无帖子</text>
      <text class="my-posts-page__empty-desc">快去发布你的第一条帖子吧</text>
      <view class="my-posts-page__empty-btn" @tap="goCreate">
        <text class="my-posts-page__empty-btn-text">发布帖子</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { userApi } from '@api/user'
import { postApi } from '@api/post'
import { useUserStore } from '@stores/user'
import PostCard from '@components/post/PostCard.vue'
import { SIZE_DEFAULT } from '@utils/constants'

const userStore = useUserStore()

const posts = ref<any[]>([])
const loading = ref(true)
const isRefreshing = ref(false)
const page = ref(1)
const hasMore = ref(true)

onMounted(() => {
  fetchPosts()
})

async function fetchPosts(reset = false) {
  if (reset) page.value = 1
  try {
    const userId = userStore.userId
    if (!userId) {
      posts.value = []
      loading.value = false
      return
    }
    const data = await userApi.getUserPosts(userId, { page: page.value, size: SIZE_DEFAULT })
    const list = Array.isArray(data) ? data : (data?.list || data?.records || [])
    if (reset) {
      posts.value = list
    } else {
      posts.value = [...posts.value, ...list]
    }
    hasMore.value = list.length >= SIZE_DEFAULT
  } catch {
    // handled
  } finally {
    loading.value = false
    isRefreshing.value = false
  }
}

function onRefresh() {
  isRefreshing.value = true
  fetchPosts(true)
}

function onLoadMore() {
  if (!hasMore.value || isRefreshing.value) return
  page.value++
  fetchPosts()
}

function showActions(post: any) {
  uni.showActionSheet({
    itemList: ['查看详情', '删除帖子'],
    success: (res) => {
      if (res.tapIndex === 0) {
        goDetail(post.id)
      } else if (res.tapIndex === 1) {
        confirmDelete(post)
      }
    },
  })
}

function confirmDelete(post: any) {
  uni.showModal({
    title: '确认删除',
    content: '删除后无法恢复，确定要删除这条帖子吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await postApi.delete(post.id)
          posts.value = posts.value.filter((p) => p.id !== post.id)
          uni.showToast({ title: '已删除', icon: 'success' })
        } catch {
          // handled
        }
      }
    },
  })
}

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/post/detail?id=${id}` })
}

function goCreate() {
  uni.navigateTo({ url: '/pages/post/create' })
}
</script>

<style scoped>
.my-posts-page {
  min-height: 100vh;
  background: var(--bg-secondary);
}

/* ── Skeleton ── */
.my-posts-page__skeleton {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.my-posts-page__skeleton-item {
  padding: 16px;
  background: var(--bg-primary);
  border: 2px solid var(--ink-100);
  border-radius: var(--radius-lg);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.my-posts-page__skeleton-line {
  height: 14px;
}

/* ── List ── */
.my-posts-page__list {
  height: 100vh;
  padding: 16px;
}

.my-posts-page__list-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.my-posts-page__item-wrapper {
  position: relative;
}

.my-posts-page__item {
  transition: transform 0.15s ease;
}

.my-posts-page__load-more,
.my-posts-page__no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px 0;
}

.my-posts-page__load-more-text,
.my-posts-page__no-more-text {
  font-size: 13px;
  color: var(--ink-300);
}

.my-posts-page__safe-bottom {
  height: 40px;
}

/* ── Empty ── */
.my-posts-page__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 32px;
}

.my-posts-page__empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.my-posts-page__empty-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--ink-600);
  margin-bottom: 8px;
}

.my-posts-page__empty-desc {
  font-size: 14px;
  color: var(--ink-300);
  text-align: center;
  margin-bottom: 20px;
}

.my-posts-page__empty-btn {
  padding: 10px 28px;
  background: var(--accent-500);
  border-radius: var(--radius-full);
}

.my-posts-page__empty-btn:active {
  background: var(--accent-600);
}

.my-posts-page__empty-btn-text {
  color: #fff;
  font-size: 15px;
  font-weight: 600;
}
</style>
