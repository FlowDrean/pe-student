<template>
  <div class="create-trade page">
    <input v-model="title" class="input" placeholder="商品标题" maxlength="50" style="margin-bottom:12px" />
    <textarea v-model="desc" class="create-trade__textarea" placeholder="描述商品成色、使用情况等..." maxlength="1000"></textarea>
    <div style="display:flex;gap:12px;margin-bottom:12px">
      <input v-model.number="price" class="input" type="number" placeholder="售价 (¥)" style="flex:1" />
      <input v-model.number="originalPrice" class="input" type="number" placeholder="原价 (¥)" style="flex:1" />
    </div>
    <div class="category-row">
      <select v-model="category" class="input"><option value="">选择分类</option><option v-for="c in TRADE_CATEGORIES" :key="c.key" :value="c.key">{{ c.label }}</option></select>
      <select v-model.number="condition" class="input"><option :value="0">选择成色</option><option v-for="c in CONDITIONS" :key="c.key" :value="c.key">{{ c.label }}</option></select>
    </div>

    <div class="create-trade__images">
      <div v-for="(img, i) in images" :key="i" class="create-trade__img-item">
        <img :src="img" @click.stop="previewImage(i)" />
        <div v-if="!uploading" class="create-trade__img-delete" @click="removeImage(i)">✕</div>
        <div v-else class="create-trade__img-loading"><span class="spinner spinner--tiny"></span></div>
      </div>
      <div v-if="uploading" class="create-trade__img-item create-trade__img-item--uploading"><div class="spinner"></div></div>
      <div v-if="images.length < 9 && !uploading" class="create-trade__img-add" @click="triggerUpload">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="var(--ink-400)" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        <span>{{ images.length }}/9</span>
      </div>
      <input ref="fileInput" type="file" accept="image/jpeg,image/png,image/gif,image/webp" multiple style="display:none" @change="handleFileChange" />
    </div>

    <button class="btn btn-primary btn-block btn-lg" style="margin-top:20px" :disabled="submitting || uploading" @click="handleSubmit">
      {{ submitting ? '发布中...' : uploading ? '图片上传中...' : '发布商品' }}
    </button>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showImagePreview } from 'vant'
import { TRADE_CATEGORIES, CONDITIONS, UPLOAD_MAX_IMAGES, UPLOAD_MAX_SIZE } from '@utils/constants'
import { createTrade, uploadImage } from '@api'

const router = useRouter()
const title = ref('')
const desc = ref('')
const price = ref(0)
const originalPrice = ref(0)
const category = ref('')
const condition = ref(0)
const images = ref([])
const submitting = ref(false)
const uploading = ref(false)
const fileInput = ref(null)

const canSubmit = computed(() => title.value.trim() && price.value > 0 && category.value && !submitting.value && !uploading.value)

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

async function handleSubmit() {
  if (!canSubmit.value) {
    if (!title.value.trim()) return showToast('请输入商品标题')
    if (!price.value) return showToast('请输入售价')
    if (!category.value) return showToast('请选择分类')
    return
  }
  submitting.value = true
  try {
    await createTrade({
      title: title.value, description: desc.value, price: price.value,
      originalPrice: originalPrice.value || null, category: category.value,
      condition: condition.value || null, images: images.value.length > 0 ? images.value : null,
    })
    showToast('发布成功')
    router.back()
  } catch { showToast('发布失败，请重试') }
  finally { submitting.value = false }
}
</script>

<style scoped>
.create-trade { padding: var(--space-4) var(--content-padding); background: var(--bg-secondary); min-height: 100vh; }
.create-trade__textarea { width: 100%; min-height: 120px; padding: 12px 16px; background: var(--bg-primary); border: 2px solid var(--ink-200); border-radius: var(--radius-lg); font-size: var(--text-base); line-height: 1.8; resize: vertical; color: var(--ink-800); margin-bottom: 12px; font-family: var(--font-body); transition: all var(--transition-fast); }
.create-trade__textarea:focus { border-color: var(--accent-500); box-shadow: var(--shadow-glow-blue); outline: none; }
.create-trade__textarea::placeholder { color: var(--ink-300); font-style: italic; }
.category-row { display: flex; gap: 12px; }
.category-row select { flex: 1; appearance: none; background-image: url("data:image/svg+xml,%3Csvg width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%236e7080' stroke-width='2.5'%3E%3Cpolyline points='6 9 12 15 18 9'/%3E%3C/svg%3E"); background-repeat: no-repeat; background-position: right 12px center; padding-right: 36px; }
.create-trade__images { display: flex; flex-wrap: wrap; gap: var(--space-2); margin: var(--space-4) 0; }
.create-trade__img-item { position: relative; width: 80px; height: 80px; border-radius: var(--radius-md); overflow: hidden; border: 2px solid var(--ink-100); }
.create-trade__img-item img { width: 100%; height: 100%; object-fit: cover; }
.create-trade__img-delete { position: absolute; top: 2px; right: 2px; width: 20px; height: 20px; background: rgba(0,0,0,0.7); color: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 10px; cursor: pointer; }
.create-trade__img-add { width: 80px; height: 80px; border: 2px dashed var(--ink-300); border-radius: var(--radius-md); display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 2px; font-size: var(--text-2xs); color: var(--ink-400); cursor: pointer; transition: all var(--transition-fast); }
.create-trade__img-add:active { border-color: var(--accent-500); background: var(--accent-50); }
.create-trade__img-loading { position: absolute; inset: 0; background: rgba(255,255,255,0.7); display: flex; align-items: center; justify-content: center; }
.create-trade__img-item--uploading { display: flex; align-items: center; justify-content: center; background: var(--ink-50); border: 2px dashed var(--ink-300); }

.spinner { width: 28px; height: 28px; border: 3px solid var(--ink-200); border-top-color: var(--accent-600); border-radius: 50%; animation: spin 0.7s linear infinite; }
.spinner--tiny { width: 20px; height: 20px; border-width: 2px; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
