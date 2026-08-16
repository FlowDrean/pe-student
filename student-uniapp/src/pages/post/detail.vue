<template>
  <view class="post-detail">
    <!-- Loading -->
    <Loading v-if="loading" variant="skeleton" :count="3" />

    <!-- Error -->
    <ErrorState
      v-else-if="error"
      :title="error"
      @retry="fetchPost"
    />

    <!-- Content -->
    <template v-else-if="post">
      <!-- Author Header -->
      <view class="post-detail__header card">
        <view class="post-detail__author" @tap="goProfile(post.userId)">
          <UserAvatar :avatar="post.user?.avatar" :name="post.user?.nickname" :user-id="post.userId" :size="44" />
          <view class="post-detail__author-info">
            <text class="post-detail__nickname">{{ post.user?.nickname || '匿名用户' }}</text>
            <text class="post-detail__time">{{ timeAgo(post.createdAt) }}</text>
          </view>
        </view>
        <view class="post-detail__category-tag" :style="{ background: catColor + '18', color: catColor, borderColor: catColor + '40' }">
          {{ categoryLabel }}
        </view>
      </view>

      <!-- Body -->
      <view class="post-detail__body card">
        <text class="post-detail__title">{{ post.title }}</text>
        <text class="post-detail__content">{{ post.content }}</text>

        <!-- Image Grid -->
        <view v-if="post.images && post.images.length" class="post-detail__images">
          <ImageGrid :images="post.images" @preview="handlePreview" />
        </view>
      </view>

      <!-- Action Bar -->
      <view class="post-detail__actions card">
        <view
          class="post-detail__action-btn"
          :class="{ 'is-active': post.isLiked }"
          @tap="handleLike"
        >
          <wd-icon :name="post.isLiked ? 'heart-filled' : 'heart'" size="20px" :color="post.isLiked ? 'var(--error)' : 'var(--ink-400)'" />
          <text class="post-detail__action-text">喜欢</text>
          <text v-if="post.likes" class="post-detail__action-count">{{ formatNumber(post.likes) }}</text>
        </view>

        <view class="post-detail__action-btn" @tap="focusComment">
          <wd-icon name="chat" size="20px" color="var(--ink-500)" />
          <text class="post-detail__action-text">评论</text>
          <text v-if="post.comments" class="post-detail__action-count">{{ formatNumber(post.comments) }}</text>
        </view>

        <view
          class="post-detail__action-btn"
          :class="{ 'is-active': post.isFavorited }"
          @tap="handleFavorite"
        >
          <wd-icon :name="post.isFavorited ? 'star-filled' : 'star'" size="20px" :color="post.isFavorited ? 'var(--warning)' : 'var(--ink-400)'" />
          <text class="post-detail__action-text">收藏</text>
        </view>

        <view class="post-detail__action-btn" @tap="handleShare">
          <wd-icon name="share" size="20px" color="var(--ink-500)" />
          <text class="post-detail__action-text">分享</text>
        </view>
      </view>

      <!-- Divider -->
      <view class="divider" />

      <!-- Comment Section -->
      <view class="post-detail__comments">
        <view class="post-detail__comments-header">
          <text class="post-detail__comments-title">评论 ({{ comments.length }})</text>
        </view>

        <!-- Comment List -->
        <view v-if="comments.length > 0" class="post-detail__comments-list card">
          <CommentItem
            v-for="comment in comments"
            :key="comment.id"
            :comment="mapComment(comment)"
            @like="handleCommentLike(comment.id)"
            @reply="handleCommentReply(comment)"
          />
        </view>

        <!-- No Comments -->
        <view v-else class="post-detail__comments-empty">
          <text class="post-detail__comments-empty-text">暂无评论，来抢沙发吧</text>
        </view>

        <view v-if="loadingComments" class="post-detail__comments-loading">
          <Loading variant="spinner" />
        </view>
      </view>
    </template>

    <!-- Bottom Input Bar -->
    <view class="post-detail__input-bar safe-area-bottom" v-if="post">
      <input
        class="post-detail__input"
        v-model="commentText"
        placeholder="写下你的评论..."
        :focus="inputFocus"
        @confirm="sendComment"
        confirm-type="send"
        maxlength="500"
      />
      <view class="post-detail__send-btn" :class="{ 'is-disabled': !commentText.trim() }" @tap="sendComment">
        <text class="post-detail__send-btn-text">发送</text>
      </view>
    </view>

    <view class="post-detail__placeholder" v-if="post" />
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { storeToRefs } from 'pinia'
import { postApi } from '@api/post'
import { commentApi } from '@api/comment'
import { usePostStore } from '@stores/post'
import { timeAgo, formatNumber } from '@utils/format'
import ImageGrid from '@components/post/ImageGrid.vue'
import CommentItem from '@components/comment/CommentItem.vue'
import Loading from '@components/common/Loading.vue'
import ErrorState from '@components/common/ErrorState.vue'
import UserAvatar from '@components/common/UserAvatar.vue'

const postStore = usePostStore()
const { comments } = storeToRefs(postStore)

const post = computed(() => postStore.currentPost)

const loading = ref(true)
const loadingComments = ref(false)
const error = ref('')
const commentText = ref('')
const inputFocus = ref(false)

const postId = ref<number>(0)
const replyTo = ref<{ id: number; nickname: string } | null>(null)

// ---- Category display ----
const categoryMap: Record<string, { label: string; color: string }> = {
  help: { label: '求助', color: '#f05a28' },
  food: { label: '美食', color: '#ff7a4d' },
  study: { label: '学习', color: '#07C160' },
  life: { label: '生活', color: '#14b88a' },
  other: { label: '其他', color: '#868e96' },
}
const catColor = computed(() => categoryMap[post.value?.category || 'other']?.color || '#868e96')
const categoryLabel = computed(() => categoryMap[post.value?.category || 'other']?.label || '其他')

// ---- Map comment to component format ----
// 优先使用后端返回的 user 对象，兼容乐观更新的扁平字段
function mapComment(c: any) {
  return {
    ...c,
    user: c.user || {
      id: c.userId || 0,
      nickname: c.userNickname || '用户',
      avatar: c.userAvatar || '',
    },
  }
}

// ---- Fetch post ----
async function fetchPost() {
  if (!postId.value) return
  loading.value = true
  error.value = ''
  try {
    const data = await postApi.getDetail(postId.value)
    postStore.setCurrentPost(data)
    fetchComments()
  } catch (e: any) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function fetchComments() {
  if (!postId.value) return
  loadingComments.value = true
  try {
    const list = await commentApi.getList(postId.value)
    postStore.setComments(list || [])
  } catch {
    // silent
  } finally {
    loadingComments.value = false
  }
}

// ---- Actions ----
async function handleLike() {
  if (!post.value) return
  try {
    await postApi.toggleLike(post.value.id)
    postStore.toggleLike(post.value.id)
  } catch {
    // error handled by interceptor
  }
}

async function handleFavorite() {
  if (!post.value) return
  try {
    await postApi.toggleFavorite(post.value.id)
    postStore.toggleFavorite(post.value.id)
  } catch {
    // error handled by interceptor
  }
}

function handleShare() {
  // uni-app share
}

function handlePreview(index: number) {
  if (!post.value?.images) return
  uni.previewImage({
    urls: post.value.images,
    current: String(index),
  })
}

// ---- Comment actions ----
function focusComment() {
  inputFocus.value = true
}

async function sendComment() {
  const text = commentText.value.trim()
  if (!text || !postId.value) return
  const parentId = replyTo.value?.id

  // 乐观更新：先用临时 ID 添加到列表
  const tempId = Date.now()
  const tempComment = {
    id: tempId,
    postId: postId.value,
    content: text,
    userId: 0,
    userNickname: '我',
    userAvatar: '',
    likes: 0,
    isLiked: false,
    createdAt: new Date().toISOString(),
    parentId: parentId || undefined,
  }
  postStore.addComment(tempComment)
  commentText.value = ''
  inputFocus.value = false
  replyTo.value = null

  try {
    const payload: { content: string; parentId?: number } = { content: text }
    if (parentId) payload.parentId = parentId
    const result = await commentApi.create(postId.value, payload)
    // 用后端返回的真实数据替换临时评论
    if (result) {
      const realComment = result.comment || result
      const realUser = result.user
      postStore.replaceComment(tempId, {
        ...realComment,
        user: realUser || undefined,
      })
    }
    uni.showToast({ title: '评论成功', icon: 'success' })
  } catch {
    // 后端失败，移除乐观更新的临时评论
    postStore.removeComment(tempId)
    uni.showToast({ title: '评论失败，请重试', icon: 'none' })
  }
}

function handleCommentLike(commentId: number) {
  commentApi.toggleLike(commentId).catch(() => {})
  // 本地更新点赞状态
  postStore.toggleCommentLike(commentId)
}

function handleCommentReply(comment: any) {
  const nick = comment.user?.nickname || comment.userNickname || '用户'
  replyTo.value = { id: comment.id, nickname: nick }
  commentText.value = `回复 ${nick}：`
  inputFocus.value = true
}

// ---- Navigation ----
function goProfile(userId: number) {
  uni.navigateTo({ url: `/pages/user/profile?id=${userId}` })
}

// ---- Lifecycle ----
onLoad((options: any) => {
  if (options?.id) {
    postId.value = Number(options.id)
    fetchPost()
  }
})
</script>

<style scoped>
.post-detail {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding-bottom: 16px;
}

/* ── Header ── */
.post-detail__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  margin: 12px 12px 0;
}

.post-detail__author {
  display: flex;
  align-items: center;
  gap: 10px;
}

.post-detail__avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid var(--ink-100);
}

.post-detail__avatar-text {
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  font-family: var(--font-display);
}

.post-detail__author-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.post-detail__nickname {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-800);
}

.post-detail__time {
  font-size: 11px;
  color: var(--ink-400);
}

.post-detail__category-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 600;
  border: 1px solid;
  flex-shrink: 0;
}

/* ── Body ── */
.post-detail__body {
  padding: 16px;
  margin: 12px 12px 0;
}

.post-detail__title {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 700;
  color: var(--ink-900);
  line-height: 1.4;
  margin-bottom: 12px;
  display: block;
}

.post-detail__content {
  font-size: 15px;
  color: var(--ink-600);
  line-height: 1.75;
  white-space: pre-wrap;
  word-break: break-word;
  display: block;
}

.post-detail__images {
  margin-top: 14px;
}

/* ── Actions ── */
.post-detail__actions {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 12px 8px;
  margin: 12px 12px 0;
}

.post-detail__action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  border-radius: var(--radius-md);
  transition: all 0.15s ease;
}

.post-detail__action-btn:active {
  background: var(--ink-50);
  transform: scale(0.94);
}

.post-detail__action-icon {
  font-size: 20px;
  line-height: 1;
}

.post-detail__action-text {
  font-size: 11px;
  color: var(--ink-400);
  font-weight: 500;
}

.post-detail__action-count {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--ink-400);
  font-weight: 500;
}

.post-detail__action-btn.is-active .post-detail__action-icon {
  color: #dc2626;
}

.post-detail__action-btn.is-active .post-detail__action-text {
  color: #dc2626;
}

/* ── Comments ── */
.post-detail__comments {
  padding: 0;
}

.post-detail__comments-header {
  padding: 14px 16px 0;
}

.post-detail__comments-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--ink-800);
}

.post-detail__comments-list {
  margin: 8px 12px 0;
  overflow: hidden;
}

.post-detail__comments-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 16px;
}

.post-detail__comments-empty-text {
  font-size: 14px;
  color: var(--ink-300);
}

.post-detail__comments-loading {
  padding: 16px;
}

/* ── Bottom Input ── */
.post-detail__input-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  background: var(--bg-primary);
  border-top: 2px solid var(--ink-100);
  z-index: 200;
  box-shadow: 0 -2px 8px rgba(15, 17, 25, 0.04);
}

.post-detail__input {
  flex: 1;
  height: 38px;
  padding: 0 14px;
  background: var(--bg-tertiary);
  border: 2px solid var(--ink-200);
  border-radius: var(--radius-full);
  font-size: 14px;
  color: var(--ink-800);
}

.post-detail__input:focus {
  border-color: var(--accent-500);
}

.post-detail__send-btn {
  padding: 8px 18px;
  background: var(--accent-500);
  border-radius: var(--radius-full);
  box-shadow: 0 2px 8px rgba(7, 193, 96, 0.25);
}

.post-detail__send-btn:active {
  background: var(--accent-600);
  transform: scale(0.96);
}

.post-detail__send-btn.is-disabled {
  background: var(--ink-200);
  box-shadow: none;
  pointer-events: none;
}

.post-detail__send-btn-text {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
}

.post-detail__placeholder {
  height: 70px;
}

/* ── Divider ── */
.divider {
  margin: 16px 12px 0;
  height: 1px;
  background: var(--ink-100);
}
</style>
