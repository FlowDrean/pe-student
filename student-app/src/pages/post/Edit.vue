<template>
  <div class="create-page page">
    <Loading v-if="loading" variant="skeleton" :count="2" />
    <template v-else-if="post">
      <input
        v-model="title"
        class="input"
        type="text"
        placeholder="标题（选填）"
        maxlength="100"
        style="margin-bottom: 12px"
      />
      <textarea
        v-model="content"
        class="create-page__textarea"
        placeholder="分享你的校园生活..."
        rows="6"
        maxlength="2000"
      ></textarea>

      <div class="create-page__images">
        <div v-for="(img, i) in images" :key="i" class="create-page__img-item">
          <img :src="img" @click.stop="previewImage(i)" />
          <div v-if="!uploading" class="create-page__img-delete" @click="removeImage(i)">✕</div>
          <div v-else class="create-page__img-loading"><span class="spinner spinner--tiny"></span></div>
        </div>
        <div v-if="uploading" class="create-page__img-item create-page__img-item--uploading"><div class="spinner"></div></div>
        <div v-if="images.length < 9 && !uploading" class="create-page__img-add" @click="triggerUpload">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="var(--ink-400)" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          <span>{{ images.length }}/9</span>
        </div>
        <input ref="fileInput" type="file" accept="image/jpeg,image/png,image/gif,image/webp" multiple style="display:none" @change="handleFileChange" />
      </div>

      <div class="create-page__category">
        <span class="create-page__label">分类</span>
        <div class="category-options">
          <button v-for="cat in POST_CATEGORIES" :key="cat.key" class="category-opt" :class="{ active: category === cat.key }" @click="category = cat.key">
            <SvgIcon :name="cat.icon" :size="14" /> {{ cat.label }}
          </button>
        </div>
      </div>

      <div class="create-page__actions">
        <button class="btn btn-primary btn-block btn-lg" :disabled="!canSubmit || submitting || uploading" @click="handleSave">
          {{ submitting ? '保存中...' : uploading ? '图片上传中...' : '保存修改' }}
        </button>
      </div>
    </template>
    <EmptyState v-else title="帖子不存在" description="可能已被删除或无权编辑" :show-action="false" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showImagePreview } from 'vant'
import { POST_CATEGORIES, UPLOAD_MAX_IMAGES, UPLOAD_MAX_SIZE } from '@utils/constants'
import { getPostDetail, updatePost, uploadImage } from '@api'
import Loading from '@components/common/Loading.vue'
import EmptyState from '@components/common/EmptyState.vue'
import SvgIcon from '@components/common/SvgIcon.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const post = ref(null)
const title = ref('')
const content = ref('')
const category = ref('life')
const images = ref([])
const submitting = ref(false)
const uploading = ref(false)
const fileInput = ref(null)

const canSubmit = computed(() => content.value.trim().length > 0 && !submitting.value && !uploading.value)

onMounted(async () => {
  try {
    const data = await getPostDetail(route.params.id)
    post.value = data
    title.value = data.title || ''
    content.value = data.content || ''
    category.value = data.category || 'life'
    images.value = Array.isArray(data.images) ? [...data.images] : []
  } catch {
    post.value = null
  } finally {
    loading.value = false
  }
})

function triggerUpload() { fileInput.value?.click() }

async function handleFileChange(e) {
  const files = Array.from(e.target.files || [])
  if (!files.length) return
  if (images.value.length + files.length > UPLOAD_MAX_IMAGES) { showToast(`最多上传${UPLOAD_MAX_IMAGES}张图片`); fileInput.value.value = ''; return }
  uploading.value = true
  try {
    for (const file of files) {
      if (file.size > UPLOAD_MAX_SIZE) { showToast(`图片「${file.name}」超过10MB，已跳过`); continue }
      if (!file.type.startsWith('image/')) { showToast(`文件「${file.name}」不是图片，已跳过`); continue }
      const result = await uploadImage(file)
      if (result && result.url) images.value.push(result.url)
    }
  } catch { showToast('图片上传失败，请重试') }
  finally { uploading.value = false; fileInput.value.value = '' }
}

function removeImage(i) { images.value.splice(i, 1) }
function previewImage(index) { showImagePreview({ images: images.value, startPosition: index, closeable: true }) }

async function handleSave() {
  if (!canSubmit.value || !post.value) return
  submitting.value = true
  try {
    await updatePost(post.value.id, {
      title: title.value,
      content: content.value,
      category: category.value,
      images: images.value,
    })
    showToast('保存成功')
    router.back()
  } catch { showToast('保存失败，请重试') }
  finally { submitting.value = false }
}
</script>

<style scoped>
.create-page { padding: var(--space-4) var(--content-padding); background: var(--bg-secondary); min-height: 100vh; }
.create-page__textarea { width: 100%; min-height: 160px; padding: 14px 16px; background: var(--bg-primary); border: 2px solid var(--ink-200); border-radius: var(--radius-lg); font-size: var(--text-base); line-height: 1.8; resize: vertical; color: var(--ink-800); font-family: var(--font-body); transition: all var(--transition-fast); }
.create-page__textarea:focus { border-color: var(--accent-500); box-shadow: var(--shadow-glow-blue); outline: none; }
.create-page__textarea::placeholder { color: var(--ink-300); font-style: italic; }
.create-page__label { font-size: var(--text-sm); font-weight: var(--font-semibold); color: var(--ink-600); margin-bottom: var(--space-3); display: block; letter-spacing: 0.02em; }
.create-page__images { display: flex; flex-wrap: wrap; gap: var(--space-2); margin: var(--space-4) 0; }
.create-page__img-item { position: relative; width: 80px; height: 80px; border-radius: var(--radius-md); overflow: hidden; border: 2px solid var(--ink-100); }
.create-page__img-item img { width: 100%; height: 100%; object-fit: cover; }
.create-page__img-delete { position: absolute; top: 2px; right: 2px; width: 20px; height: 20px; background: rgba(0,0,0,0.7); color: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 10px; cursor: pointer; }
.create-page__img-add { width: 80px; height: 80px; border: 2px dashed var(--ink-300); border-radius: var(--radius-md); display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 2px; font-size: var(--text-2xs); color: var(--ink-400); cursor: pointer; transition: all var(--transition-fast); }
.create-page__img-add:active { border-color: var(--accent-500); background: var(--accent-50); }
.create-page__img-loading { position: absolute; inset: 0; background: rgba(255,255,255,0.7); display: flex; align-items: center; justify-content: center; }
.create-page__img-item--uploading { display: flex; align-items: center; justify-content: center; background: var(--ink-50); border: 2px dashed var(--ink-300); }
.create-page__category { margin: var(--space-4) 0; }
.category-options { display: flex; flex-wrap: wrap; gap: var(--space-2); }
.category-opt { display: flex; align-items: center; gap: 4px; padding: 8px 16px; border-radius: var(--radius-full); border: 2px solid var(--ink-100); background: var(--bg-primary); font-size: var(--text-sm); font-weight: var(--font-medium); color: var(--ink-500); transition: all var(--transition-fast); }
.category-opt:active { transform: scale(0.95); }
.category-opt.active { background: var(--ink-900); border-color: var(--ink-900); color: #fff; font-weight: var(--font-semibold); }
.create-page__actions { margin-top: var(--space-5); }

.spinner { width: 28px; height: 28px; border: 3px solid var(--ink-200); border-top-color: var(--accent-600); border-radius: 50%; animation: spin 0.7s linear infinite; }
.spinner--tiny { width: 20px; height: 20px; border-width: 2px; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
