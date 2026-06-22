import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface Post {
  id: number
  title: string
  content: string
  images?: string[]
  category?: string
  tags?: string[]
  likes: number
  comments: number
  views: number
  isLiked: boolean
  isFavorited: boolean
  userId: number
  userNickname?: string
  userAvatar?: string
  createdAt: string
  updatedAt?: string
  [key: string]: any
}

export interface Comment {
  id: number
  postId: number
  content: string
  userId: number
  userNickname?: string
  userAvatar?: string
  createdAt: string
  [key: string]: any
}

export const usePostStore = defineStore('post', () => {
  // ── 状态 ──
  const posts = ref<Post[]>([])
  const currentPost = ref<Post | null>(null)
  const comments = ref<Comment[]>([])
  const categories = ref<any[]>([])
  const currentPage = ref(1)
  const hasMore = ref(true)
  const loading = ref(false)

  // ── 方法 ──
  function setPosts(list: any, append = false) {
    const arr = Array.isArray(list) ? list : (list?.list || list?.records || [])
    if (append) {
      posts.value = [...posts.value, ...arr]
    } else {
      posts.value = arr
    }
  }

  function setCurrentPost(post: Post) {
    currentPost.value = post
  }

  function setComments(list: any) {
    // 兼容后端返回数组或 { list/records: [...] } 对象
    if (Array.isArray(list)) {
      comments.value = list
    } else if (list?.list) {
      comments.value = list.list
    } else if (list?.records) {
      comments.value = list.records
    } else {
      comments.value = []
    }
  }

  function addComment(comment: Comment) {
    if (!Array.isArray(comments.value)) {
      comments.value = []
    }
    comments.value.unshift(comment)
    if (currentPost.value) {
      currentPost.value.comments++
    }
  }

  function removeComment(id: number) {
    comments.value = comments.value.filter((c) => c.id !== id)
    if (currentPost.value) {
      currentPost.value.comments--
    }
  }

  /** 用后端返回的真实评论数据替换乐观更新的临时评论 */
  function replaceComment(oldId: number, newComment: any) {
    const index = comments.value.findIndex((c) => c.id === oldId)
    if (index !== -1) {
      comments.value[index] = {
        ...comments.value[index],
        ...newComment,
        id: newComment.id || oldId,
      }
    }
  }

  function toggleCommentLike(commentId: number) {
    const comment = comments.value.find((c) => c.id === commentId)
    if (comment) {
      comment.isLiked = !comment.isLiked
      comment.likes = (comment.likes || 0) + (comment.isLiked ? 1 : -1)
    }
  }

  function toggleLike(postId: number) {
    const post = posts.value.find((p) => p.id === postId)
    if (post) {
      post.isLiked = !post.isLiked
      post.likes += post.isLiked ? 1 : -1
    }
    if (currentPost.value?.id === postId) {
      currentPost.value.isLiked = !currentPost.value.isLiked
      currentPost.value.likes += currentPost.value.isLiked ? 1 : -1
    }
  }

  function toggleFavorite(postId: number) {
    const post = posts.value.find((p) => p.id === postId)
    if (post) {
      post.isFavorited = !post.isFavorited
    }
    if (currentPost.value?.id === postId) {
      currentPost.value.isFavorited = !currentPost.value.isFavorited
    }
  }

  function setPage(page: number) {
    currentPage.value = page
  }

  function setHasMore(val: boolean) {
    hasMore.value = val
  }

  function reset() {
    posts.value = []
    currentPage.value = 1
    hasMore.value = true
  }

  return {
    posts,
    currentPost,
    comments,
    categories,
    currentPage,
    hasMore,
    loading,
    setPosts,
    setCurrentPost,
    setComments,
    addComment,
    removeComment,
    replaceComment,
    toggleCommentLike,
    toggleLike,
    toggleFavorite,
    setPage,
    setHasMore,
    reset,
  }
})
