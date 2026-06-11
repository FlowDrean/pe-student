<template>
  <div class="edit-page page">
    <div class="edit-page__avatar" @click="triggerUpload">
      <div class="avatar avatar-xl" :style="{ background: avatarPreview ? 'transparent' : 'var(--accent-600)' }">
        <img v-if="avatarPreview" :src="avatarPreview" class="avatar-img" />
        <span v-else class="avatar-inner">{{ (form.nickname || userStore.nickname)?.charAt(0) || '?' }}</span>
      </div>
      <span class="edit-page__change-avatar">{{ uploading ? '上传中...' : '更换头像' }}</span>
      <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleAvatarChange" />
    </div>

    <div class="form-group">
      <label class="form-label">昵称</label>
      <input v-model="form.nickname" class="input" placeholder="你的昵称" maxlength="20" />
    </div>
    <div class="form-group">
      <label class="form-label">学校</label>
      <input v-model="form.school" class="input" placeholder="学校名称" />
    </div>
    <div class="form-group">
      <label class="form-label">专业</label>
      <input v-model="form.major" class="input" placeholder="专业" />
    </div>
    <div class="form-group">
      <label class="form-label">性别</label>
      <div class="gender-options">
        <button v-for="opt in genderOptions" :key="opt.value" type="button" class="gender-btn" :class="{ 'gender-btn--active': form.gender === opt.value }" @click="form.gender = opt.value">{{ opt.label }}</button>
      </div>
    </div>
    <div class="form-group">
      <label class="form-label">个人简介</label>
      <textarea v-model="form.bio" class="edit-textarea" placeholder="介绍一下自己吧..." maxlength="200"></textarea>
      <span class="form-hint">{{ (form.bio || '').length }}/200</span>
    </div>
    <div class="edit-page__actions">
      <button class="btn btn-primary btn-block btn-lg" :disabled="saving || uploading" @click="handleSave">
        {{ saving ? '保存中...' : uploading ? '上传中...' : '保存' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '@stores/user'
import { getUserProfile, updateUserProfile, uploadImage } from '@api'

const router = useRouter()
const userStore = useUserStore()
const fileInput = ref(null)
const saving = ref(false)
const uploading = ref(false)
const avatarPreview = ref('')

const genderOptions = [{ value: 0, label: '保密' }, { value: 1, label: '男' }, { value: 2, label: '女' }]

const form = reactive({ nickname: '', avatar: '', school: '', major: '', bio: '', gender: 0 })

onMounted(async () => {
  try {
    const profile = await getUserProfile()
    Object.assign(form, { nickname: profile.nickname || '', avatar: profile.avatar || '', school: profile.school || '', major: profile.major || '', bio: profile.bio || '', gender: profile.gender ?? 0 })
    if (profile.avatar) avatarPreview.value = profile.avatar
  } catch {
    const u = userStore.userInfo
    if (u) { Object.assign(form, { nickname: u.nickname || '', avatar: u.avatar || '', school: u.school || '', major: u.major || '', bio: u.bio || '', gender: u.gender ?? 0 }); if (u.avatar) avatarPreview.value = u.avatar }
  }
})

function triggerUpload() { if (uploading.value) return; fileInput.value?.click() }

async function handleAvatarChange(e) {
  const file = e.target.files?.[0]; if (!file) return
  const reader = new FileReader(); reader.onload = (ev) => { avatarPreview.value = ev.target.result }; reader.readAsDataURL(file)
  uploading.value = true
  try { const result = await uploadImage(file); form.avatar = result.url; showToast('头像上传成功') }
  catch { avatarPreview.value = form.avatar || '' }
  finally { uploading.value = false; e.target.value = '' }
}

async function handleSave() {
  saving.value = true
  try {
    const data = {}
    if (form.nickname) data.nickname = form.nickname
    if (form.avatar) data.avatar = form.avatar
    if (form.school !== undefined) data.school = form.school
    if (form.major !== undefined) data.major = form.major
    if (form.bio !== undefined) data.bio = form.bio
    if (form.gender !== undefined) data.gender = form.gender
    await updateUserProfile(data)
    userStore.setUserInfo({ nickname: form.nickname, avatar: form.avatar, school: form.school, major: form.major, bio: form.bio, gender: form.gender })
    showToast('保存成功'); router.back()
  } catch { /* error handled by interceptor */ }
  finally { saving.value = false }
}
</script>

<style scoped>
.edit-page { padding: var(--space-4) var(--content-padding); padding-bottom: var(--space-8); background: var(--bg-secondary); min-height: 100vh; }
.edit-page__avatar { display: flex; flex-direction: column; align-items: center; gap: var(--space-3); padding: var(--space-6) 0; cursor: pointer; }
.avatar-inner { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; color: #fff; font-family: var(--font-display); font-size: var(--text-2xl); font-weight: var(--font-black); border-radius: 50%; }
.avatar-img { width: 100%; height: 100%; border-radius: 50%; object-fit: cover; }
.edit-page__change-avatar { font-size: var(--text-sm); color: var(--accent-600); font-weight: var(--font-semibold); }
.edit-page__change-avatar:active { opacity: 0.7; }
.form-group { margin-bottom: var(--space-4); }
.form-label { display: block; font-size: var(--text-sm); font-weight: var(--font-semibold); color: var(--ink-600); margin-bottom: var(--space-2); letter-spacing: 0.02em; }
.gender-options { display: flex; gap: var(--space-2); }
.gender-btn { flex: 1; padding: 10px 0; border-radius: var(--radius-md); border: 2px solid var(--ink-200); background: var(--bg-primary); font-size: var(--text-sm); font-weight: var(--font-medium); color: var(--ink-500); transition: all var(--transition-fast); cursor: pointer; }
.gender-btn:active { transform: scale(0.96); }
.gender-btn--active { background: var(--ink-900); border-color: var(--ink-900); color: #fff; font-weight: var(--font-semibold); }
.edit-textarea { width: 100%; min-height: 80px; padding: 12px 14px; background: var(--bg-primary); border: 2px solid var(--ink-200); border-radius: var(--radius-lg); font-size: var(--text-base); font-family: var(--font-body); resize: vertical; transition: all var(--transition-fast); }
.edit-textarea:focus { border-color: var(--accent-500); box-shadow: var(--shadow-glow-blue); outline: none; }
.form-hint { display: block; text-align: right; font-size: var(--text-2xs); color: var(--ink-400); margin-top: var(--space-1); }
.edit-page__actions { padding: 20px 0; }
</style>
