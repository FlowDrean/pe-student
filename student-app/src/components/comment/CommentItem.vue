<template>
  <div class="comment-item">
    <div class="avatar avatar-sm" :style="{ background: comment.user?.avatar ? 'transparent' : avatarColor(comment.user?.id) }" @click="goProfile(comment.user?.id)">
      <img v-if="comment.user?.avatar" :src="comment.user.avatar" class="avatar-img" />
      <span v-else class="avatar-text">{{ comment.user?.nickname?.charAt(0) || '?' }}</span>
    </div>
    <div class="comment-item__body">
      <div class="comment-item__header">
        <span class="comment-item__name">{{ comment.user?.nickname || '匿名' }}</span>
        <span class="comment-item__time">{{ timeAgo(comment.createdAt) }}</span>
      </div>
      <p class="comment-item__content">{{ comment.content }}</p>
      <div class="comment-item__footer">
        <button class="comment-item__action" @click="handleLike(comment)">
          <svg width="13" height="13" viewBox="0 0 24 24" :fill="comment.isLiked ? 'var(--coral-500)' : 'none'" :stroke="comment.isLiked ? 'var(--coral-500)' : 'currentColor'" stroke-width="2"><path d="M14 9V5a3 3 0 00-3-3l-4 9v11h11.28a2 2 0 002-1.7l1.38-9a2 2 0 00-2-2.3H14zM7 22H4a2 2 0 01-2-2v-7a2 2 0 012-2h3"/></svg>
          {{ comment.likes || '' }}
        </button>
        <button class="comment-item__action" @click="$emit('reply', comment)">回复</button>
        <button v-if="isOwn(comment)" class="comment-item__action comment-item__action--del" @click="handleDelete(comment)">删除</button>
      </div>

      <!-- 楼中楼回复 -->
      <div v-if="comment.replies?.length" class="comment-item__replies">
        <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
          <div class="avatar avatar-xs" :style="{ background: reply.user?.avatar ? 'transparent' : avatarColor(reply.user?.id) }" @click="goProfile(reply.user?.id)">
            <img v-if="reply.user?.avatar" :src="reply.user.avatar" class="avatar-img" />
            <span v-else class="avatar-text">{{ reply.user?.nickname?.charAt(0) || '?' }}</span>
          </div>
          <div class="reply-item__body">
            <div class="reply-item__header">
              <span class="reply-item__name">{{ reply.user?.nickname || '匿名' }}</span>
              <span class="comment-item__time">{{ timeAgo(reply.createdAt) }}</span>
            </div>
            <p class="reply-item__content">
              <span v-if="reply.replyToName" class="reply-item__replyto">回复 @{{ reply.replyToName }}</span>
              {{ reply.content }}
            </p>
            <div class="comment-item__footer">
              <button class="comment-item__action" @click="handleLike(reply)">
                <svg width="12" height="12" viewBox="0 0 24 24" :fill="reply.isLiked ? 'var(--coral-500)' : 'none'" :stroke="reply.isLiked ? 'var(--coral-500)' : 'currentColor'" stroke-width="2"><path d="M14 9V5a3 3 0 00-3-3l-4 9v11h11.28a2 2 0 002-1.7l1.38-9a2 2 0 00-2-2.3H14zM7 22H4a2 2 0 01-2-2v-7a2 2 0 012-2h3"/></svg>
                {{ reply.likes || '' }}
              </button>
              <button class="comment-item__action" @click="$emit('reply', reply, comment)">回复</button>
              <button v-if="isOwn(reply)" class="comment-item__action comment-item__action--del" @click="handleDelete(reply, comment)">删除</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { showConfirmDialog } from 'vant'
import { timeAgo } from '@utils/format'
import { useUserStore } from '@stores/user'
import { toggleCommentLike, deleteComment } from '@api/comment'

defineProps({ comment: { type: Object, required: true } })
const emit = defineEmits(['reply', 'deleted'])

const router = useRouter()
const userStore = useUserStore()

function avatarColor(id) {
  const colors = ['#07C160', '#f97316', '#059669', '#f59e0b', '#7c3aed']
  return colors[(id || 0) % colors.length]
}

function isOwn(item) {
  return userStore.isLogin && item.user?.id === userStore.userId
}

async function handleLike(item) {
  if (!userStore.isLogin) return router.push('/auth/login')
  try {
    const liked = await toggleCommentLike(item.id)
    item.isLiked = liked
    item.likes = (item.likes || 0) + (liked ? 1 : -1)
  } catch { /* 错误已在请求拦截器中处理 */ }
}

async function handleDelete(item, parent) {
  try {
    await showConfirmDialog({ title: '删除评论', message: '确定要删除这条评论吗？' })
  } catch {
    return
  }
  try {
    await deleteComment(item.id)
    emit('deleted', { item, parent })
  } catch { /* 错误已在请求拦截器中处理 */ }
}

function goProfile(id) {
  if (id) router.push(`/user-profile/${id}`)
}
</script>

<style scoped>
.comment-item { display: flex; gap: var(--space-3); padding: var(--space-3) 0; }
.avatar-text { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; color: #fff; font-family: var(--font-display); font-size: 12px; font-weight: var(--font-bold); border-radius: var(--radius-full); }
.avatar-img { width: 100%; height: 100%; border-radius: 50%; object-fit: cover; }
.comment-item__body { flex: 1; min-width: 0; }
.comment-item__header { display: flex; align-items: center; gap: var(--space-2); margin-bottom: 3px; }
.comment-item__name { font-size: var(--text-sm); font-weight: var(--font-semibold); color: var(--ink-700); }
.comment-item__time { font-size: var(--text-2xs); color: var(--ink-300); }
.comment-item__content { font-size: var(--text-sm); color: var(--ink-600); line-height: 1.6; word-break: break-word; }
.comment-item__footer { display: flex; gap: var(--space-4); margin-top: var(--space-2); }
.comment-item__action { display: flex; align-items: center; gap: 3px; font-size: var(--text-2xs); color: var(--ink-400); font-weight: var(--font-medium); transition: color var(--transition-fast); }
.comment-item__action:active { color: var(--ink-600); }
.comment-item__action--del:active { color: var(--coral-500); }

/* ── 楼中楼 ── */
.comment-item__replies { margin-top: var(--space-2); padding: var(--space-2); background: var(--bg-tertiary); border-radius: var(--radius-md); display: flex; flex-direction: column; gap: var(--space-2); }
.reply-item { display: flex; gap: var(--space-2); }
.reply-item__body { flex: 1; min-width: 0; }
.reply-item__header { display: flex; align-items: center; gap: var(--space-2); margin-bottom: 2px; }
.reply-item__name { font-size: var(--text-2xs); font-weight: var(--font-semibold); color: var(--ink-600); }
.reply-item__content { font-size: var(--text-xs); color: var(--ink-600); line-height: 1.6; word-break: break-word; }
.reply-item__replyto { color: var(--accent-600); font-weight: var(--font-medium); margin-right: 2px; }
</style>
