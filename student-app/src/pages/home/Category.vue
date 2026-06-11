<template>
  <div class="category-page">
    <PostCard v-for="post in posts" :key="post.id" :post="post" @click="goDetail(post.id)" />
    <EmptyState v-if="!posts.length" title="暂无相关内容" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PostCard from '@components/post/PostCard.vue'
import EmptyState from '@components/common/EmptyState.vue'
import { POST_CATEGORIES } from '@utils/constants'

const route = useRoute()
const router = useRouter()
const posts = ref([])

onMounted(() => {
  const type = route.params.type
  const cat = POST_CATEGORIES.find(c => c.key === type)
  document.title = cat ? `${cat.label} - 校园互助` : '分类 - 校园互助'

  posts.value = [
    { id: 1, title: `关于${cat?.label || type}的帖子`, content: '示例内容...', category: type, likes: 45, comments: 12, views: 345, user: { id: 2, nickname: '用户' }, createdAt: new Date().toISOString() },
  ]
})

function goDetail(id) { router.push(`/post/${id}`) }
</script>

<style scoped>
.category-page { padding: var(--space-3) var(--content-padding); display: flex; flex-direction: column; gap: var(--space-3); }
</style>
