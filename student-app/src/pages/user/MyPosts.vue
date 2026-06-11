<template>
  <div class="list-page">
    <Loading v-if="loading" variant="skeleton" :count="3" />
    <template v-else>
      <PostCard v-for="post in posts" :key="post.id" :post="post" @click="goDetail(post.id)" />
      <EmptyState v-if="!posts.length" title="暂无帖子" description="快去发布第一条帖子吧" action-text="去发布" @action="goCreate" />
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@stores/user'
import { getUserPosts } from '@api/user'
import PostCard from '@components/post/PostCard.vue'
import Loading from '@components/common/Loading.vue'
import EmptyState from '@components/common/EmptyState.vue'

const router = useRouter()
const userStore = useUserStore()

const posts = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const data = await getUserPosts(userStore.userId, { page: 1, size: 50 })
    posts.value = data.list || []
  } catch {
    // 加载失败静默处理
  } finally {
    loading.value = false
  }
})

function goDetail(id) { router.push(`/post/${id}`) }
function goCreate() { router.push('/post/create') }
</script>

<style scoped>
.list-page { padding: var(--space-3) var(--content-padding); display: flex; flex-direction: column; gap: var(--space-3); min-height: 50vh; }
</style>
