<template>
  <tabbar>
    <view class="post-create">
    <!-- Title Input -->
    <view class="post-create__section card">
      <text class="post-create__label">标题</text>
      <input
        class="post-create__input"
        v-model="title"
        placeholder="请输入帖子标题"
        placeholder-style="color: var(--ink-300)"
        maxlength="100"
      />
    </view>

    <!-- Content Textarea -->
    <view class="post-create__section card">
      <text class="post-create__label">内容</text>
      <textarea
        class="post-create__textarea"
        v-model="content"
        placeholder="分享你的想法..."
        placeholder-style="color: var(--ink-300)"
        maxlength="5000"
      />
      <text class="post-create__char-count">{{ content.length }}/5000</text>
    </view>

    <!-- Category Picker -->
    <view class="post-create__section card">
      <text class="post-create__label">分类</text>
      <view class="post-create__chips">
        <view
          v-for="cat in POST_CATEGORIES"
          :key="cat.key"
          class="post-create__chip"
          :class="{ 'is-active': category === cat.key }"
          :style="category === cat.key ? { background: cat.color, borderColor: cat.color, color: '#fff' } : { borderColor: 'var(--ink-200)', color: 'var(--ink-500)' }"
          @tap="category = cat.key"
        >
          <text class="post-create__chip-text">{{ cat.label }}</text>
        </view>
      </view>
    </view>

    <!-- Image Upload -->
    <view class="post-create__section card">
      <text class="post-create__label">图片 ({{ images.length }}/9)</text>
      <view class="post-create__images">
        <view
          v-for="(img, index) in images"
          :key="index"
          class="post-create__image-item"
        >
          <image :src="img" class="post-create__image" mode="aspectFill" />
          <view class="post-create__image-delete" @tap="removeImage(index)">
            <text class="post-create__image-delete-text">×</text>
          </view>
        </view>
        <view
          v-if="images.length < 9"
          class="post-create__image-add"
          @tap="chooseImages"
        >
          <text class="post-create__image-add-icon">+</text>
          <text class="post-create__image-add-text">添加图片</text>
        </view>
      </view>
    </view>

    <!-- Submit Button -->
    <view class="post-create__submit-wrap">
      <view
        class="post-create__submit btn-primary"
        :class="{ 'is-disabled': submitting }"
        @tap="handleSubmit"
      >
        <text class="post-create__submit-text">{{ submitting ? '发布中...' : '发布帖子' }}</text>
      </view>
    </view>
  </view>
  </tabbar>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { postApi } from '@api/post'
import { uploadImage } from '@api/upload'
import { POST_CATEGORIES, UPLOAD_MAX_IMAGES } from '@utils/constants'
import tabbar from '@layouts/tabbar.vue'

const title = ref('')
const content = ref('')
const category = ref('other')
const images = ref<string[]>([])
const submitting = ref(false)

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

// ---- Submit ----
async function handleSubmit() {
  // Validate
  if (!title.value.trim()) {
    return uni.showToast({ title: '请输入标题', icon: 'none' })
  }
  if (!content.value.trim()) {
    return uni.showToast({ title: '请输入内容', icon: 'none' })
  }

  submitting.value = true
  try {
    await postApi.create({
      title: title.value.trim(),
      content: content.value.trim(),
      category: category.value,
      images: images.value,
    })
    uni.showToast({ title: '发布成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 800)
  } catch {
    // error handled by interceptor
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.post-create {
  flex: 1;
  background: var(--bg-secondary);
  padding: 12px;
}

.post-create__section {
  padding: 16px;
  margin-bottom: 12px;
}

.post-create__label {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-700);
  margin-bottom: 10px;
  display: block;
}

/* ── Input ── */
.post-create__input {
  width: 100%;
  padding: 12px 16px;
  background: var(--bg-tertiary);
  border: 2px solid var(--ink-200);
  border-radius: var(--radius-md);
  font-size: 15px;
  color: var(--ink-800);
}

.post-create__input:focus {
  border-color: var(--accent-500);
}

/* ── Textarea ── */
.post-create__textarea {
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

.post-create__textarea:focus {
  border-color: var(--accent-500);
}

.post-create__char-count {
  display: block;
  text-align: right;
  font-size: 11px;
  color: var(--ink-300);
  margin-top: 4px;
  font-family: var(--font-mono);
}

/* ── Chips ── */
.post-create__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.post-create__chip {
  padding: 6px 16px;
  border-radius: var(--radius-full);
  border: 2px solid var(--ink-200);
  background: var(--bg-primary);
  transition: all 0.15s ease;
}

.post-create__chip:active {
  transform: scale(0.96);
}

.post-create__chip.is-active {
  font-weight: 600;
}

.post-create__chip-text {
  font-size: 13px;
  font-weight: 500;
}

/* ── Images ── */
.post-create__images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.post-create__image-item {
  width: calc((100% - 16px) / 3);
  aspect-ratio: 1;
  border-radius: var(--radius-md);
  overflow: hidden;
  position: relative;
  border: 2px solid var(--ink-100);
}

.post-create__image {
  width: 100%;
  height: 100%;
  display: block;
}

.post-create__image-delete {
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

.post-create__image-delete-text {
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  line-height: 1;
}

.post-create__image-add {
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

.post-create__image-add:active {
  border-color: var(--accent-500);
  background: var(--accent-50);
}

.post-create__image-add-icon {
  font-size: 28px;
  color: var(--ink-300);
  font-weight: 300;
  line-height: 1;
}

.post-create__image-add-text {
  font-size: 11px;
  color: var(--ink-300);
}

/* ── Submit ── */
.post-create__submit-wrap {
  padding: 8px 0 32px;
}

.post-create__submit {
  width: 100%;
  padding: 14px;
  text-align: center;
  font-size: 16px;
}

.post-create__submit.is-disabled {
  opacity: 0.6;
  pointer-events: none;
}

.post-create__submit-text {
  color: #fff;
  font-weight: 600;
}
</style>
