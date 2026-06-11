<template>
  <div class="register-page">
    <div class="register-page__header">
      <h1 class="register-page__title">创建账号</h1>
      <p class="register-page__subtitle">加入校园互助大家庭</p>
    </div>

    <form class="register-page__form" @submit.prevent="handleRegister">
      <div class="form-group">
        <input
          v-model="form.username"
          class="dark-input"
          type="text"
          placeholder="用户名（2-20位字母、数字或汉字）"
          maxlength="20"
        />
      </div>

      <div class="form-group">
        <input
          v-model="form.phone"
          class="dark-input"
          type="tel"
          placeholder="手机号"
          maxlength="11"
        />
      </div>

      <div class="form-group">
        <div class="input-wrapper">
          <input
            v-model="form.password"
            class="dark-input"
            :type="showPwd ? 'text' : 'password'"
            placeholder="密码（6-20位）"
            maxlength="20"
          />
          <button type="button" class="input-suffix" @click="showPwd = !showPwd">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" :stroke="showPwd ? 'rgba(255,255,255,0.7)' : 'rgba(255,255,255,0.3)'" stroke-width="2">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" /><circle cx="12" cy="12" r="3" />
            </svg>
          </button>
        </div>
      </div>

      <div class="form-group">
        <input
          v-model="form.school"
          class="dark-input"
          type="text"
          placeholder="学校名称（选填）"
        />
      </div>

      <div class="form-agreement">
        <label class="checkbox-label">
          <input v-model="agreed" type="checkbox" class="checkbox" />
          <span>我已阅读并同意</span>
        </label>
        <a href="#" class="link">《用户协议》</a>
        <span>和</span>
        <a href="#" class="link">《隐私政策》</a>
      </div>

      <button
        type="submit"
        class="register-page__submit"
        :disabled="loading || !agreed"
      >
        {{ loading ? '注册中...' : '注 册' }}
      </button>
    </form>

    <div class="register-page__footer">
      <span>已有账号？</span>
      <router-link to="/auth/login" class="link link--bold">立即登录</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '@stores/user'
import { register, getUserProfile } from '@api'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const showPwd = ref(false)
const agreed = ref(false)

const form = reactive({
  username: '',
  phone: '',
  password: '',
  school: '',
})

async function handleRegister() {
  if (!form.username) return showToast('请输入用户名')
  if (!form.phone) return showToast('请输入手机号')
  if (!/^1[3-9]\d{9}$/.test(form.phone)) return showToast('手机号格式不正确')
  if (!form.password) return showToast('请输入密码')
  if (form.password.length < 6) return showToast('密码至少6位')
  if (!agreed) return showToast('请阅读并同意用户协议')

  loading.value = true
  try {
    const data = await register({
      username: form.username,
      phone: form.phone,
      password: form.password,
      school: form.school,
    })
    userStore.setToken(data.token, data.refreshToken)

    try {
      const userInfo = await getUserProfile()
      userStore.setUserInfo(userInfo)
    } catch {
      userStore.setUserInfo({
        id: data.userId,
        username: form.username,
        phone: form.phone,
        school: form.school,
      })
    }

    showToast('注册成功')
    router.replace('/')
  } catch {
    // 错误已在请求拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  width: 100%;
  padding: var(--space-6) 0;
}

.register-page__header {
  text-align: center;
  margin-bottom: var(--space-6);
  animation: slideDown 0.5s ease-out;
}

.register-page__title {
  font-family: var(--font-display);
  font-size: var(--text-3xl);
  font-weight: var(--font-black);
  color: #fff;
  margin-bottom: var(--space-2);
  letter-spacing: 0.02em;
}

.register-page__subtitle {
  font-size: var(--text-sm);
  color: rgba(255, 255, 255, 0.4);
  font-weight: var(--font-light);
}

.register-page__form {
  animation: slideUp 0.5s ease-out 0.1s both;
}

.form-group {
  margin-bottom: var(--space-3);
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.dark-input {
  width: 100%;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.06);
  border: 2px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  color: #fff;
  font-family: var(--font-body);
  transition: all var(--transition-fast);
}

.dark-input::placeholder {
  color: rgba(255, 255, 255, 0.25);
  font-style: italic;
}

.dark-input:focus {
  border-color: rgba(255, 255, 255, 0.4);
  background: rgba(255, 255, 255, 0.09);
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.06);
}

.input-suffix {
  position: absolute;
  right: 12px;
  display: flex;
  align-items: center;
  padding: 4px;
  opacity: 0.7;
}

.form-agreement {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 2px;
  font-size: var(--text-xs);
  color: rgba(255, 255, 255, 0.3);
  margin-bottom: var(--space-5);
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  cursor: pointer;
  color: rgba(255, 255, 255, 0.45);
}

.checkbox {
  accent-color: var(--accent-500);
  width: 14px;
  height: 14px;
}

.link {
  font-size: var(--text-xs);
  color: rgba(255, 255, 255, 0.5);
  text-decoration: none;
}

.link--bold {
  font-weight: var(--font-semibold);
  color: var(--accent-400);
}

.register-page__submit {
  width: 100%;
  height: 50px;
  border-radius: var(--radius-md);
  border: none;
  background: #fff;
  color: var(--ink-900);
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  font-family: var(--font-body);
  letter-spacing: 0.06em;
  cursor: pointer;
  transition: all var(--transition-fast);
  box-shadow: 0 4px 0 0 rgba(0, 0, 0, 0.15);
}

.register-page__submit:active {
  transform: translateY(2px);
  box-shadow: 0 2px 0 0 rgba(0, 0, 0, 0.15);
}

.register-page__submit:disabled {
  opacity: 0.5;
  transform: none;
  box-shadow: 0 4px 0 0 rgba(0, 0, 0, 0.15);
}

.register-page__footer {
  margin-top: var(--space-6);
  display: flex;
  justify-content: center;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: rgba(255, 255, 255, 0.35);
  animation: fadeIn 0.4s ease-out 0.3s both;
}
</style>
