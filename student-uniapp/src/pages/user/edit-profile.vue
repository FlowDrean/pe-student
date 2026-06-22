<template>
  <view class="edit-profile-page">
    <scroll-view class="edit-profile-page__body" scroll-y>
      <!-- Loading -->
      <view v-if="loading" class="edit-profile-page__loading">
        <text class="edit-profile-page__loading-text">加载中...</text>
      </view>

      <template v-else>
        <!-- Avatar -->
        <view class="edit-profile-page__section card" @tap="changeAvatar">
          <text class="edit-profile-page__section-label">头像</text>
          <view class="edit-profile-page__avatar" :style="{ background: avatarColor }">
            <text class="edit-profile-page__avatar-text">{{ firstChar }}</text>
          </view>
          <wd-icon name="chevron-right" size="16" color="var(--ink-300)" />
        </view>

        <!-- Form Fields -->
        <view class="edit-profile-page__form card">
          <!-- Nickname -->
          <view class="edit-profile-page__field">
            <text class="edit-profile-page__field-label">昵称</text>
            <input
              v-model="form.nickname"
              class="edit-profile-page__field-input"
              placeholder="请输入昵称"
              :maxlength="20"
            />
          </view>

          <view class="edit-profile-page__divider" />

          <!-- School -->
          <view class="edit-profile-page__field">
            <text class="edit-profile-page__field-label">学校</text>
            <input
              v-model="form.school"
              class="edit-profile-page__field-input"
              placeholder="请输入学校"
              :maxlength="30"
            />
          </view>

          <view class="edit-profile-page__divider" />

          <!-- Major -->
          <view class="edit-profile-page__field">
            <text class="edit-profile-page__field-label">专业</text>
            <input
              v-model="form.major"
              class="edit-profile-page__field-input"
              placeholder="请输入专业"
              :maxlength="30"
            />
          </view>

          <view class="edit-profile-page__divider" />

          <!-- Gender -->
          <view class="edit-profile-page__field">
            <text class="edit-profile-page__field-label">性别</text>
            <view class="edit-profile-page__gender-group">
              <view
                v-for="option in genderOptions"
                :key="option.value"
                class="edit-profile-page__gender-option"
                :class="{ 'edit-profile-page__gender-option--active': form.gender === option.value }"
                @tap="form.gender = option.value"
              >
                <view class="edit-profile-page__gender-radio">
                  <view v-if="form.gender === option.value" class="edit-profile-page__gender-radio-dot" />
                </view>
                <text class="edit-profile-page__gender-text">{{ option.label }}</text>
              </view>
            </view>
          </view>

          <view class="edit-profile-page__divider" />

          <!-- Bio -->
          <view class="edit-profile-page__field edit-profile-page__field--bio">
            <text class="edit-profile-page__field-label">个人简介</text>
            <textarea
              v-model="form.bio"
              class="edit-profile-page__field-textarea"
              placeholder="介绍一下自己..."
              :maxlength="200"
              :auto-height="true"
            />
            <text class="edit-profile-page__char-count">{{ form.bio.length }}/200</text>
          </view>
        </view>

        <!-- Save Button -->
        <view class="edit-profile-page__save-btn" @tap="saveProfile">
          <text class="edit-profile-page__save-btn-text">保存</text>
        </view>

        <view class="edit-profile-page__safe-bottom" />
      </template>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { userApi } from '@api/user'
import { useUserStore } from '@stores/user'

const userStore = useUserStore()

const loading = ref(true)
const submitting = ref(false)

const form = reactive({
  nickname: '',
  school: '',
  major: '',
  gender: 0,
  bio: '',
})

const genderOptions = [
  { value: 0, label: '保密' },
  { value: 1, label: '男' },
  { value: 2, label: '女' },
]

const avatarColor = computed(() => {
  const colors = ['#07C160', '#f97316', '#059669', '#f59e0b', '#dc2626', '#7c3aed']
  const idx = (userStore.userId || 0) % colors.length
  return colors[idx]
})

const firstChar = computed(() => {
  const name = form.nickname || userStore.userInfo?.nickname || userStore.userInfo?.username || '用'
  return name.charAt(0)
})

onMounted(async () => {
  try {
    // Try to fetch latest profile
    const data = await userApi.getProfile()
    if (data) {
      userStore.setUserInfo(data)
    }
  } catch {
    // use store data
  }
  const info = userStore.userInfo
  if (info) {
    form.nickname = info.nickname || ''
    form.school = (info as any).school || ''
    form.major = (info as any).major || ''
    form.gender = info.gender ?? 0
    form.bio = info.bio || ''
  }
  loading.value = false
})

async function changeAvatar() {
  try {
    const res = await uni.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
    })
    const filePath = res.tempFilePaths[0]
    uni.showLoading({ title: '上传中...' })
    const result = await userApi.uploadAvatar(filePath)
    if (result) {
      userStore.updateField('avatar', (result as any).url || result)
      uni.showToast({ title: '头像更新成功', icon: 'success' })
    }
  } catch (e: any) {
    if (e?.errMsg?.includes('cancel')) return
    uni.showToast({ title: '上传失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

async function saveProfile() {
  if (submitting.value) return
  if (!form.nickname.trim()) {
    uni.showToast({ title: '请输入昵称', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await userApi.updateProfile({
      nickname: form.nickname.trim(),
      school: form.school.trim(),
      major: form.major.trim(),
      gender: form.gender,
      bio: form.bio.trim(),
    })
    // Refresh store
    const data = await userApi.getProfile()
    if (data) {
      userStore.setUserInfo(data)
    }
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1200)
  } catch {
    // handled
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.edit-profile-page {
  min-height: 100vh;
  background: var(--bg-secondary);
}

.edit-profile-page__body {
  height: 100vh;
  padding: 16px;
}

/* ── Loading ── */
.edit-profile-page__loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
}

.edit-profile-page__loading-text {
  font-size: 15px;
  color: var(--ink-300);
}

/* ── Avatar Section ── */
.edit-profile-page__section {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  margin-bottom: 16px;
}

.edit-profile-page__section:active {
  background: var(--ink-50);
}

.edit-profile-page__section-label {
  flex: 1;
  font-size: 15px;
  font-weight: 500;
  color: var(--ink-700);
}

.edit-profile-page__avatar {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
}

.edit-profile-page__avatar-text {
  color: #fff;
  font-size: 22px;
  font-weight: 700;
}

.edit-profile-page__section-arrow {
  font-size: 18px;
  color: var(--ink-300);
}

/* ── Form ── */
.edit-profile-page__form {
  margin-bottom: 24px;
  overflow: hidden;
}

.edit-profile-page__field {
  display: flex;
  flex-direction: column;
  padding: 16px;
  gap: 8px;
}

.edit-profile-page__field-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-400);
}

.edit-profile-page__field-input {
  font-size: 15px;
  color: var(--ink-800);
  padding: 0;
}

.edit-profile-page__field-input::placeholder {
  color: var(--ink-300);
}

.edit-profile-page__field-textarea {
  width: 100%;
  min-height: 80px;
  font-size: 15px;
  color: var(--ink-800);
  padding: 0;
  line-height: 1.6;
}

.edit-profile-page__char-count {
  font-size: 12px;
  color: var(--ink-300);
  text-align: right;
}

.edit-profile-page__divider {
  height: 1px;
  background: var(--ink-50);
  margin: 0 16px;
}

/* ── Gender ── */
.edit-profile-page__gender-group {
  display: flex;
  gap: 24px;
}

.edit-profile-page__gender-option {
  display: flex;
  align-items: center;
  gap: 6px;
}

.edit-profile-page__gender-radio {
  width: 20px;
  height: 20px;
  border: 2px solid var(--ink-200);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
}

.edit-profile-page__gender-option--active .edit-profile-page__gender-radio {
  border-color: var(--accent-500);
}

.edit-profile-page__gender-radio-dot {
  width: 10px;
  height: 10px;
  background: var(--accent-500);
  border-radius: var(--radius-full);
}

.edit-profile-page__gender-text {
  font-size: 14px;
  color: var(--ink-600);
}

/* ── Save ── */
.edit-profile-page__save-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 14px;
  background: var(--accent-500);
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-md);
}

.edit-profile-page__save-btn:active {
  background: var(--accent-600);
  transform: scale(0.97);
}

.edit-profile-page__save-btn-text {
  color: #fff;
  font-size: 16px;
  font-weight: 700;
}

.edit-profile-page__safe-bottom {
  height: 40px;
}
</style>
