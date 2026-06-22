<template>
  <view class="post-edit">
    <!-- Loading state -->
    <Loading v-if="loading" variant="skeleton" :count="3" />

    <template v-else>
      <!-- Title Input -->
      <view class="post-edit__section card">
        <text class="post-edit__label">标题</text>
        <input
          class="post-edit__input"
          v-model="title"
          placeholder="请输入帖子标题"
          placeholder-style="color: var(--ink-300)"
          maxlength="100"
        />
      </view>

      <!-- Content Textarea -->
      <view class="post-edit__section card">
        <text class="post-edit__label">内容</text>
        <textarea
          class="post-edit__textarea"
          v-model="content"
          placeholder="分享你的想法..."
          placeholder-style="color: var(--ink-300)"
          maxlength="5000"
        />
        <text class="post-edit__char-count">{{ content.length }}/5000</text>
      </view>

      <!-- Category Picker -->
      <view class="post-edit__section card">
        <text class="post-edit__label">分类</text>
        <view class="post-edit__chips">
          <view
            v-for="cat in POST_CATEGORIES"
            :key="cat.key"
            class="post-edit__chip"
            :class="{ 'is-active': category === cat.key }"
            :style="category === cat.key ? { background: cat.color, borderColor: cat.color, color: '#fff' } : { borderColor: 'var(--ink-200)', color: 'var(--ink-500)' }"
            @tap="category = cat.key"
          >
            <text class="post-edit__chip-text">{{ cat.label }}</text>
          </view>
        </view>
      </view>

      <!-- Image Upload -->
      <view class="post-edit__section card">
        <text class="post-edit__label">图片 ({{ images.length }}/9)</text>
        <view class="post-edit__images">
          <view
            v-for="(img, index) in images"
            :key="index"
            class="post-edit__image-item"
          >
            <image :src="img" class="post-edit__image" mode="aspectFill" />
            <view class="post-edit__image-delete" @tap="removeImage(index)">
              <text class="post-edit__image-delete-text">×</text>
            </view>
          </view>
          <view
            v-if="images.length < 9"
            class="post-edit__image-add"
            @tap="chooseImages"
          >
            <text class="post-edit__image-add-icon">+</text>
            <text class="post-edit__image-add-text">添加图片</text>
          </view>
        </view>
      </view>

      <!-- Save Button -->
      <view class="post-edit__submit-wrap">
        <view
          class="post-edit__submit btn-primary"
          :class="{ 'is-disabled': saving }"
          @tap="handleSave"
        >
          <text class="post-edit__submit-text">{{ saving ? '保存中...' : '保存' }}</text>
        </view>
      </view>

      <!-- Delete Button -->
      <view class="post-edit__delete-wrap">
        <view class="post-edit__delete" @tap="handleDelete">
          <text class="post-edit__delete-text">删除帖子</text>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { postApi } from '@api/post'
import { uploadImage } from '@api/upload'
import { POST_CATEGORIES, UPLOAD_MAX_IMAGES } from '@utils/constants'
import Loading from '@components/common/Loading.vue'

const postId = ref<number>(0)
const title = ref('')
const content = ref('')
const category = ref('other')
const images = ref<string[]>([])
const saving = ref(false)
const loading = ref(true)

// ---- Fetch existing post data ----
async function fetchPost() {
  if (!postId.value) return
  loading.value = true
  try {
    const data: any = await postApi.getDetail(postId.value)
    title.value = data.title || ''
    content.value = data.content || ''
    category.value = data.category || 'other'
    images.value = data.images || []
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
    setTimeout(() => uni.navigateBack(), 800)
  } finally {
    loading.value = false
  }
}

// ---- Choose images ----
function chooseImages() {
  const remaining = UPLOAD_MAX_IMAGES - images.value.length
  uni.chooseImage({
    count: remaining,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const filePaths = res.tempFilePaths
      uni.showLoading({ title: '上传中...', mask: true })
      try {
        const urls: string[] = []
        for (const fp of filePaths) {
          const result = await uploadImage(fp)
          urls.push(result.url)
        }
        images.value = [...images.value, ...urls]
      } catch {
        uni.showToast({ title: '图片上传失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    },
  })
}

function removeImage(index: number) {
  images.value.splice(index, 1)
}

// ---- Save ----
async function handleSave() {
  if (!title.value.trim()) {
    return uni.showToast({ title: '请输入标题', icon: 'none' })
  }
  if (!content.value.trim()) {
    return uni.showToast({ title: '请输入内容', icon: 'none' })
  }

  saving.value = true
  try {
    await postApi.update(postId.value, {
      title: title.value.trim(),
      content: content.value.trim(),
      category: category.value,
      images: images.value,
    })
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 800)
  } catch {
    // error handled by interceptor
  } finally {
    saving.value = false
  }
}

// ---- Delete ----
function handleDelete() {
  uni.showModal({
    title: '确认删除',
    content: '删除后将无法恢复，确定要删除这篇帖子吗？',
    confirmText: '删除',
    confirmColor: '#dc2626',
    success: async (res) => {
      if (res.confirm) {
        try {
          await postApi.delete(postId.value)
          uni.showToast({ title: '已删除', icon: 'success' })
          setTimeout(() => {
            uni.navigateBack()
          }, 800)
        } catch {
          // error handled by interceptor
        }
      }
    },
  })
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
.post-edit {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding: 12px;
}

.post-edit__section {
  padding: 16px;
  margin-bottom: 12px;
}

.post-edit__label {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-700);
  margin-bottom: 10px;
  display: block;
}

/* ── Input ── */
.post-edit__input {
  width: 100%;
  padding: 12px 16px;
  background: var(--bg-tertiary);
  border: 2px solid var(--ink-200);
  border-radius: var(--radius-md);
  font-size: 15px;
  color: var(--ink-800);
}

.post-edit__input:focus {
  border-color: var(--accent-500);
}

/* ── Textarea ── */
.post-edit__textarea {
  width: 100%;
  min-height: 150px;
  padding: 12px 16px;
  background: var(--bg-tertiary);
  border: 2px solid var(--ink-200);
  border-radius: var(--radius-md);
  font-size: 14px;
  color: var(--ink-800);
  line-height: 1.6;
}

.post-edit__textarea:focus {
  border-color: var(--accent-500);
}

.post-edit__char-count {
  display: block;
  text-align: right;
  font-size: 11px;
  color: var(--ink-300);
  margin-top: 4px;
  font-family: var(--font-mono);
}

/* ── Chips ── */
.post-edit__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.post-edit__chip {
  padding: 6px 16px;
  border-radius: var(--radius-full);
  border: 2px solid var(--ink-200);
  background: var(--bg-primary);
  transition: all 0.15s ease;
}

.post-edit__chip:active {
  transform: scale(0.96);
}

.post-edit__chip.is-active {
  font-weight: 600;
}

.post-edit__chip-text {
  font-size: 13px;
  font-weight: 500;
}

/* ── Images ── */
.post-edit__images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.post-edit__image-item {
  width: calc((100% - 16px) / 3);
  aspect-ratio: 1;
  border-radius: var(--radius-md);
  overflow: hidden;
  position: relative;
  border: 2px solid var(--ink-100);
}

.post-edit__image {
  width: 100%;
  height: 100%;
  display: block;
}

.post-edit__image-delete {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
}

.post-edit__image-delete-text {
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  line-height: 1;
}

.post-edit__image-add {
  width: calc((100% - 16px) / 3);
  aspect-ratio: 1;
  border-radius: var(--radius-md);
  border: 2px dashed var(--ink-200);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  background: var(--bg-tertiary);
  transition: all 0.15s ease;
}

.post-edit__image-add:active {
  border-color: var(--accent-500);
  background: var(--accent-50);
}

.post-edit__image-add-icon {
  font-size: 28px;
  color: var(--ink-300);
  font-weight: 300;
  line-height: 1;
}

.post-edit__image-add-text {
  font-size: 11px;
  color: var(--ink-300);
}

/* ── Save ── */
.post-edit__submit-wrap {
  padding: 8px 0 12px;
}

.post-edit__submit {
  width: 100%;
  padding: 14px;
  text-align: center;
  font-size: 16px;
}

.post-edit__submit.is-disabled {
  opacity: 0.6;
  pointer-events: none;
}

.post-edit__submit-text {
  color: #fff;
  font-weight: 600;
}

/* ── Delete ── */
.post-edit__delete-wrap {
  padding: 0 0 32px;
}

.post-edit__delete {
  width: 100%;
  padding: 14px;
  text-align: center;
  border-radius: var(--radius-full);
  border: 2px solid var(--error);
  background: var(--bg-primary);
  box-shadow: var(--shadow-card);
}

.post-edit__delete:active {
  background: #fef2f2;
  transform: scale(0.98);
}

.post-edit__delete-text {
  font-size: 15px;
  font-weight: 600;
  color: var(--error);
}
</style>
