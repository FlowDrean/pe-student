<template>
  <view class="trade-create">
    <!-- Title -->
    <view class="trade-create__section card">
      <text class="trade-create__label">商品名称</text>
      <input
        class="trade-create__input"
        v-model="title"
        placeholder="请输入商品名称"
        placeholder-style="color: var(--ink-300)"
        maxlength="100"
      />
    </view>

    <!-- Description -->
    <view class="trade-create__section card">
      <text class="trade-create__label">商品描述</text>
      <textarea
        class="trade-create__textarea"
        v-model="description"
        placeholder="描述一下商品的情况..."
        placeholder-style="color: var(--ink-300)"
        maxlength="2000"
      />
      <text class="trade-create__char-count">{{ description.length }}/2000</text>
    </view>

    <!-- Price -->
    <view class="trade-create__section card">
      <text class="trade-create__label">价格</text>
      <view class="trade-create__price-row">
        <view class="trade-create__price-input-wrap">
          <text class="trade-create__price-prefix">¥</text>
          <input
            class="trade-create__price-input"
            v-model="price"
            type="digit"
            placeholder="0.00"
            placeholder-style="color: var(--ink-300)"
          />
        </view>
        <view class="trade-create__price-input-wrap">
          <text class="trade-create__price-prefix trade-create__price-prefix--orig">原价</text>
          <input
            class="trade-create__price-input"
            v-model="originalPrice"
            type="digit"
            placeholder="选填"
            placeholder-style="color: var(--ink-300)"
          />
        </view>
      </view>
    </view>

    <!-- Category -->
    <view class="trade-create__section card">
      <text class="trade-create__label">分类</text>
      <view class="trade-create__chips">
        <view
          v-for="cat in TRADE_CATEGORIES"
          :key="cat.key"
          class="trade-create__chip"
          :class="{ 'is-active': category === cat.key }"
          @tap="category = cat.key"
        >
          <text class="trade-create__chip-text">{{ cat.label }}</text>
        </view>
      </view>
    </view>

    <!-- Condition -->
    <view class="trade-create__section card">
      <text class="trade-create__label">成色</text>
      <view class="trade-create__chips">
        <view
          v-for="cond in CONDITIONS"
          :key="cond.key"
          class="trade-create__chip"
          :class="{ 'is-active': condition === cond.key }"
          @tap="condition = cond.key"
        >
          <text class="trade-create__chip-text">{{ cond.label }}</text>
        </view>
      </view>
    </view>

    <!-- Location -->
    <view class="trade-create__section card">
      <text class="trade-create__label">交易地点</text>
      <input
        class="trade-create__input"
        v-model="location"
        placeholder="例如：图书馆门口"
        placeholder-style="color: var(--ink-300)"
        maxlength="50"
      />
    </view>

    <!-- Contact -->
    <view class="trade-create__section card">
      <text class="trade-create__label">联系方式</text>
      <input
        class="trade-create__input"
        v-model="contact"
        placeholder="手机号或微信号"
        placeholder-style="color: var(--ink-300)"
        maxlength="50"
      />
    </view>

    <!-- Images -->
    <view class="trade-create__section card">
      <text class="trade-create__label">商品图片 ({{ images.length }}/9)</text>
      <view class="trade-create__images">
        <view
          v-for="(img, index) in images"
          :key="index"
          class="trade-create__image-item"
        >
          <image :src="img" class="trade-create__image" mode="aspectFill" />
          <view class="trade-create__image-delete" @tap="removeImage(index)">
            <text class="trade-create__image-delete-text">×</text>
          </view>
        </view>
        <view
          v-if="images.length < 9"
          class="trade-create__image-add"
          @tap="chooseImages"
        >
          <text class="trade-create__image-add-icon">+</text>
          <text class="trade-create__image-add-text">添加图片</text>
        </view>
      </view>
    </view>

    <!-- Submit -->
    <view class="trade-create__submit-wrap">
      <view
        class="trade-create__submit btn-primary"
        :class="{ 'is-disabled': submitting }"
        @tap="handleSubmit"
      >
        <text class="trade-create__submit-text">{{ submitting ? '发布中...' : '发布商品' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { tradeApi } from '@api/trade'
import { uploadImage } from '@api/upload'
import { TRADE_CATEGORIES, CONDITIONS, UPLOAD_MAX_IMAGES } from '@utils/constants'

const title = ref('')
const description = ref('')
const price = ref('')
const originalPrice = ref('')
const category = ref('other')
const condition = ref<number>(1)
const location = ref('')
const contact = ref('')
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
  if (!title.value.trim()) {
    return uni.showToast({ title: '请输入商品名称', icon: 'none' })
  }
  if (!description.value.trim()) {
    return uni.showToast({ title: '请输入商品描述', icon: 'none' })
  }
  if (!price.value || Number(price.value) < 0) {
    return uni.showToast({ title: '请输入有效价格', icon: 'none' })
  }

  submitting.value = true
  try {
    await tradeApi.create({
      title: title.value.trim(),
      description: description.value.trim(),
      price: Number(price.value),
      originalPrice: originalPrice.value ? Number(originalPrice.value) : undefined,
      category: category.value,
      condition: condition.value,
      location: location.value.trim(),
      contact: contact.value.trim(),
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
.trade-create {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding: 12px;
}

.trade-create__section {
  padding: 16px;
  margin-bottom: 12px;
}

.trade-create__label {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-700);
  margin-bottom: 10px;
  display: block;
}

/* ── Input / Textarea ── */
.trade-create__input {
  width: 100%;
  padding: 12px 16px;
  background: var(--bg-tertiary);
  border: 2px solid var(--ink-200);
  border-radius: var(--radius-md);
  font-size: 15px;
  color: var(--ink-800);
}

.trade-create__input:focus {
  border-color: var(--accent-500);
}

.trade-create__textarea {
  width: 100%;
  min-height: 120px;
  padding: 12px 16px;
  background: var(--bg-tertiary);
  border: 2px solid var(--ink-200);
  border-radius: var(--radius-md);
  font-size: 14px;
  color: var(--ink-800);
  line-height: 1.6;
}

.trade-create__textarea:focus {
  border-color: var(--accent-500);
}

.trade-create__char-count {
  display: block;
  text-align: right;
  font-size: 11px;
  color: var(--ink-300);
  margin-top: 4px;
  font-family: var(--font-mono);
}

/* ── Price Row ── */
.trade-create__price-row {
  display: flex;
  gap: 12px;
}

.trade-create__price-input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: var(--bg-tertiary);
  border: 2px solid var(--ink-200);
  border-radius: var(--radius-md);
}

.trade-create__price-input-wrap:focus-within {
  border-color: var(--accent-500);
}

.trade-create__price-prefix {
  font-size: 16px;
  font-weight: 700;
  color: var(--accent-500);
  font-family: var(--font-mono);
}

.trade-create__price-prefix--orig {
  font-size: 12px;
  color: var(--ink-400);
  font-weight: 500;
  font-family: var(--font-sans);
}

.trade-create__price-input {
  flex: 1;
  font-size: 16px;
  color: var(--ink-800);
  font-family: var(--font-mono);
  min-width: 0;
}

/* ── Chips ── */
.trade-create__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.trade-create__chip {
  padding: 6px 16px;
  border-radius: var(--radius-full);
  border: 2px solid var(--ink-200);
  background: var(--bg-primary);
  transition: all 0.15s ease;
}

.trade-create__chip:active {
  transform: scale(0.96);
}

.trade-create__chip.is-active {
  background: var(--accent-500);
  border-color: var(--accent-500);
}

.trade-create__chip-text {
  font-size: 13px;
  font-weight: 500;
  color: var(--ink-600);
}

.trade-create__chip.is-active .trade-create__chip-text {
  color: #fff;
  font-weight: 600;
}

/* ── Images ── */
.trade-create__images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.trade-create__image-item {
  width: calc((100% - 16px) / 3);
  aspect-ratio: 1;
  border-radius: var(--radius-md);
  overflow: hidden;
  position: relative;
  border: 2px solid var(--ink-100);
}

.trade-create__image {
  width: 100%;
  height: 100%;
  display: block;
}

.trade-create__image-delete {
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

.trade-create__image-delete-text {
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  line-height: 1;
}

.trade-create__image-add {
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

.trade-create__image-add:active {
  border-color: var(--accent-500);
  background: var(--accent-50);
}

.trade-create__image-add-icon {
  font-size: 28px;
  color: var(--ink-300);
  font-weight: 300;
  line-height: 1;
}

.trade-create__image-add-text {
  font-size: 11px;
  color: var(--ink-300);
}

/* ── Submit ── */
.trade-create__submit-wrap {
  padding: 8px 0 32px;
}

.trade-create__submit {
  width: 100%;
  padding: 14px;
  text-align: center;
  font-size: 16px;
}

.trade-create__submit.is-disabled {
  opacity: 0.6;
  pointer-events: none;
}

.trade-create__submit-text {
  color: #fff;
  font-weight: 600;
}
</style>
