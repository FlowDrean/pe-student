/* ============================================
   帖子状态管理
   ============================================ */

import { defineStore } from 'pinia'
import { ref } from 'vue'

export const usePostStore = defineStore('post', () => {
  // ── 状态 ──
  const posts = ref([])
  const currentPost = ref(null)
  const comments = ref([])
  const categories = ref([])
  const currentPage = ref(1)
  const hasMore = ref(true)
  const loading = ref(false)

  // ── 方法 ──
  function setPosts(list, append = false) {
    if (append) {
      posts.value = [...posts.value, ...list]
    } else {
      posts.value = list
    }
  }

  function setCurrentPost(post) {
    currentPost.value = post
  }

  function setComments(list) {
    comments.value = list
  }

  function addComment(comment) {
    comments.value.unshift(comment)
    if (currentPost.value) {
      currentPost.value.comments++
    }
  }

  function removeComment(id) {
    comments.value = comments.value.filter((c) => c.id !== id)
    if (currentPost.value) {
      currentPost.value.comments--
    }
  }

  function toggleLike(postId) {
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

  function toggleFavorite(postId) {
    const post = posts.value.find((p) => p.id === postId)
    if (post) {
      post.isFavorited = !post.isFavorited
    }
    if (currentPost.value?.id === postId) {
      currentPost.value.isFavorited = !currentPost.value.isFavorited
    }
  }

  function setPage(page) {
    currentPage.value = page
  }

  function setHasMore(val) {
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
    toggleLike,
    toggleFavorite,
    setPage,
    setHasMore,
    reset,
  }
})
