<template>
  <div class="post-detail page-white" v-if="post">
    <!-- 作者 -->
    <div class="post-detail__author">
      <div class="avatar avatar-md" :style="{ background: avatarColor }" @click="goUserProfile">
        <span class="avatar-inner">{{ post.user?.nickname?.charAt(0) }}</span>
      </div>
      <div class="post-detail__author-info">
        <span class="post-detail__author-name">{{ post.user?.nickname }}</span>
        <span class="post-detail__author-meta">{{ timeAgo(post.createdAt) }} · {{ formatNumber(post.views) }} 浏览</span>
      </div>
      <div class="post-detail__author-actions">
        <button v-if="isOwner" class="btn btn-sm btn-primary" style="margin-right:8px" @click="handleDelete">删除</button>
        <button v-else-if="userStore.isLogin" class="btn btn-sm btn-primary" @click="goChat">私信</button>
      </div>
    </div>

    <!-- 内容 -->
    <h1 class="post-detail__title">{{ post.title }}</h1>
    <p class="post-detail__content">{{ post.content }}</p>

    <div v-if="post.images?.length" class="post-detail__images">
      <img v-for="(img, i) in post.images" :key="i" :src="img" class="post-detail__img" @click="previewImage(i)" loading="lazy" />
    </div>

    <div class="post-detail__meta">
      <span class="tag tag-primary">{{ categoryLabel }}</span>
    </div>

    <!-- 互动栏 -->
    <div class="post-detail__actions">
      <button class="action-btn" :class="{ 'is-active': post.isLiked }" @click="handleLike">
        <svg width="20" height="20" viewBox="0 0 24 24" :fill="post.isLiked ? 'var(--coral-500)' : 'none'" :stroke="post.isLiked ? 'var(--coral-500)' : 'var(--ink-500)'" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/></svg>
        <span>{{ formatNumber(post.likes) }}</span>
      </button>
      <button class="action-btn">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="var(--ink-500)" stroke-width="2"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>
        <span>{{ formatNumber(post.comments) }}</span>
      </button>
      <button class="action-btn" :class="{ 'is-active': post.isFavorited }" @click="handleFavorite">
        <svg width="20" height="20" viewBox="0 0 24 24" :fill="post.isFavorited ? 'var(--amber-500)' : 'none'" :stroke="post.isFavorited ? 'var(--amber-500)' : 'var(--ink-500)'" stroke-width="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
      </button>
      <button class="action-btn">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="var(--ink-500)" stroke-width="2"><circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/><line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/></svg>
      </button>
    </div>

    <div class="divider" />

    <!-- 评论区 -->
    <div class="comment-section">
      <h3 class="comment-section__title">全部评论 <span class="comment-section__count">{{ post.comments }}</span></h3>
      <div v-if="comments.length" class="comment-list">
        <CommentItem v-for="c in comments" :key="c.id" :comment="c" />
      </div>
      <EmptyState v-else title="暂无评论" description="快来抢沙发吧" :show-action="false" />
    </div>
  </div>

  <Loading v-else variant="skeleton" :count="3" />

  <!-- 底部评论输入 -->
  <div class="comment-bar">
    <input v-model="commentText" class="comment-bar__input" placeholder="说点什么..." @keyup.enter="submitComment" />
    <button class="comment-bar__send" :disabled="!commentText.trim()" @click="submitComment">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" :stroke="commentText.trim() ? 'var(--accent-600)' : 'var(--ink-300)'" stroke-width="2.5"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
    </button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog, showImagePreview } from 'vant'
import { timeAgo, formatNumber } from '@utils/format'
import { POST_CATEGORIES } from '@utils/constants'
import { likeAnimation, favoriteAnimation } from '@composables/useAnimation'
import { useUserStore } from '@stores/user'
import { getPostDetail, getComments, createComment, togglePostLike, togglePostFavorite, deletePost } from '@api'
import CommentItem from '@components/comment/CommentItem.vue'
import Loading from '@components/common/Loading.vue'
import EmptyState from '@components/common/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const post = ref(null)
const comments = ref([])
const commentText = ref('')
const loading = ref(true)

const categoryLabel = computed(() => {
  const cat = POST_CATEGORIES.find((c) => c.key === post.value?.category)
  return cat?.label || '其他'
})

const avatarColor = computed(() => {
  const colors = ['#07C160', '#f97316', '#059669', '#f59e0b', '#7c3aed']
  return colors[(post.value?.user?.id || 0) % colors.length]
})

const isOwner = computed(() => {
  return userStore.userId && post.value?.user?.id === userStore.userId
})

async function handleDelete() {
  if (!post.value) return
  try {
    await showConfirmDialog({ title: '删除帖子', message: '确定要删除这篇帖子吗？删除后不可恢复。' })
  } catch {
    return
  }
  try {
    await deletePost(post.value.id)
    showToast('已删除')
    router.back()
  } catch {
    // 错误已在请求拦截器中处理
  }
}

async function handleLike() {
  if (!post.value) return
  try {
    await togglePostLike(post.value.id)
    post.value.isLiked = !post.value.isLiked
    post.value.likes += post.value.isLiked ? 1 : -1
    const el = document.querySelector('.post-detail__actions .action-btn')
    if (el) likeAnimation(el)
  } catch { /* ignore */ }
}

async function handleFavorite() {
  if (!post.value) return
  try {
    await togglePostFavorite(post.value.id)
    post.value.isFavorited = !post.value.isFavorited
    const el = document.querySelectorAll('.post-detail__actions .action-btn')[2]
    if (el) favoriteAnimation(el)
    showToast(post.value.isFavorited ? '已收藏' : '已取消收藏')
  } catch { /* ignore */ }
}

async function submitComment() {
  if (!commentText.value.trim()) return
  try {
    const result = await createComment(post.value.id, { content: commentText.value })
    comments.value.unshift({
      id: result.comment?.id || Date.now(),
      content: commentText.value,
      likes: 0,
      user: result.user || { id: 999, nickname: '我', avatar: '' },
      createdAt: new Date().toISOString(),
    })
    post.value.comments++
    commentText.value = ''
  } catch {
    showToast('评论失败，请重试')
  }
}

function goUserProfile() {
  const userId = post.value?.user?.id
  if (userId) router.push(`/user-profile/${userId}`)
}

function goChat() {
  if (!post.value?.user?.id) return
  router.push(`/chat/${post.value.user.id}`)
}

function previewImage(index) {
  if (post.value?.images?.length) {
    showImagePreview({ images: post.value.images, startPosition: index, closeable: true })
  }
}

async function fetchPostDetail() {
  const id = route.params.id
  try {
    post.value = await getPostDetail(id)
  } catch {
    showToast('获取帖子详情失败')
  }
}

async function fetchComments() {
  const id = route.params.id
  try {
    const data = await getComments(id, { page: 1, size: 20 })
    comments.value = data.list || []
  } catch { /* ignore */ }
}

onMounted(async () => {
  loading.value = true
  await Promise.all([fetchPostDetail(), fetchComments()])
  loading.value = false
})
</script>

<style scoped>
.post-detail {
  padding: var(--space-4) var(--content-padding);
  padding-bottom: 90px;
}

/* ── 作者 ── */
.post-detail__author {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-5);
  padding-bottom: var(--space-4);
  border-bottom: 2px solid var(--ink-50);
}

.avatar {
  cursor: pointer;
}

.avatar-inner {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #fff;
  font-family: var(--font-display);
  font-weight: var(--font-bold);
  border-radius: var(--radius-full);
  font-size: var(--text-base);
}

.post-detail__author-info {
  flex: 1;
}

.post-detail__author-name {
  display: block;
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--ink-900);
}

.post-detail__author-meta {
  font-size: var(--text-xs);
  color: var(--ink-400);
}

.post-detail__author-actions {
  flex-shrink: 0;
}

/* ── 标题 ── */
.post-detail__title {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: var(--font-black);
  color: var(--ink-900);
  margin-bottom: var(--space-3);
  line-height: 1.35;
  letter-spacing: -0.01em;
}

/* ── 内容 ── */
.post-detail__content {
  font-size: var(--text-base);
  line-height: 1.85;
  color: var(--ink-600);
  margin-bottom: var(--space-5);
  white-space: pre-wrap;
  word-break: break-word;
}

/* ── 图片 ── */
.post-detail__images {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 3px;
  margin-bottom: var(--space-5);
}

.post-detail__img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: var(--radius-sm);
  cursor: pointer;
  border: 1px solid var(--ink-100);
  transition: opacity var(--transition-fast);
}

.post-detail__img:active {
  opacity: 0.85;
}

/* ── 元信息 ── */
.post-detail__meta {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

/* ── 互动栏 ── */
.post-detail__actions {
  display: flex;
  justify-content: space-around;
  padding: var(--space-3) 0;
  border-top: 2px solid var(--ink-50);
  border-bottom: 2px solid var(--ink-50);
  margin-bottom: var(--space-5);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-md);
  color: var(--ink-500);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  transition: all var(--transition-fast);
}

.action-btn:active {
  background: var(--ink-50);
  transform: scale(0.94);
}

.action-btn.is-active {
  color: var(--coral-500);
}

/* ── 评论 ── */
.comment-section {
  margin-top: var(--space-2);
}

.comment-section__title {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--ink-800);
  margin-bottom: var(--space-4);
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.comment-section__count {
  font-family: var(--font-mono);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--ink-400);
  background: var(--ink-50);
  padding: 2px 8px;
  border-radius: var(--radius-sm);
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

/* ── 底部输入栏 ── */
.comment-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--content-padding);
  padding-bottom: calc(var(--space-3) + env(safe-area-inset-bottom));
  background: var(--bg-primary);
  border-top: 2px solid var(--ink-100);
  z-index: 100;
}

.comment-bar__input {
  flex: 1;
  padding: 10px 16px;
  background: var(--bg-tertiary);
  border: 2px solid var(--ink-200);
  border-radius: var(--radius-full);
  font-size: var(--text-sm);
  color: var(--ink-800);
  transition: all var(--transition-fast);
}

.comment-bar__input:focus {
  border-color: var(--accent-500);
  box-shadow: var(--shadow-glow-blue);
}

.comment-bar__send {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  border-radius: var(--radius-full);
  transition: all var(--transition-fast);
}

.comment-bar__send:not(:disabled):active {
  background: var(--accent-50);
  transform: scale(0.9);
}
</style>
