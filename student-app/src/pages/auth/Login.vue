<template>
  <div class="login-page">
    <!-- Logo & 标题 -->
    <div class="login-page__header">
      <div class="login-page__logo">
        <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
          <rect x="2" y="2" width="44" height="44" rx="10" fill="rgba(255,255,255,0.06)" stroke="rgba(255,255,255,0.12)" stroke-width="2" />
          <path d="M24 13c-5.523 0-10 4.477-10 10s2.985 8.75 6.667 9.75v-6.417h-2.084l2.5-3.333 2.5 3.333H21.5v6.417c3.682-1 6.667-4.75 6.667-9.75S29.523 13 24 13z" fill="rgba(255,255,255,0.9)" />
        </svg>
      </div>
      <h1 class="login-page__title">欢迎回来</h1>
      <p class="login-page__subtitle">连接每一份校园温暖</p>
    </div>

    <!-- 表单 -->
    <form class="login-page__form" @submit.prevent="handleLogin">
      <div class="form-group">
        <div class="input-wrapper">
          <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.35)" stroke-width="2">
            <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" />
            <circle cx="12" cy="7" r="4" />
          </svg>
          <input
            v-model="form.username"
            class="dark-input dark-input--icon"
            type="text"
            placeholder="手机号 / 用户名"
            autocomplete="username"
          />
        </div>
      </div>

      <div class="form-group">
        <div class="input-wrapper">
          <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.35)" stroke-width="2">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
            <path d="M7 11V7a5 5 0 0110 0v4" />
          </svg>
          <input
            v-model="form.password"
            class="dark-input dark-input--icon"
            :type="showPwd ? 'text' : 'password'"
            placeholder="密码"
            autocomplete="current-password"
          />
          <button type="button" class="input-suffix" @click="showPwd = !showPwd">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" :stroke="showPwd ? 'rgba(255,255,255,0.7)' : 'rgba(255,255,255,0.3)'" stroke-width="2">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
              <circle cx="12" cy="12" r="3" />
            </svg>
          </button>
        </div>
      </div>

      <div class="form-extra">
        <label class="checkbox-label">
          <input v-model="rememberMe" type="checkbox" class="checkbox" />
          <span>记住我</span>
        </label>
        <router-link to="/auth/forgot-password" class="link">忘记密码？</router-link>
      </div>

      <button
        type="submit"
        class="login-page__submit"
        :disabled="loading"
      >
        <span v-if="loading" class="spinner-submit"></span>
        <span v-else>{{ loading ? '登录中...' : '登 录' }}</span>
      </button>
    </form>

    <!-- 底部 -->
    <div class="login-page__footer">
      <span>还没有账号？</span>
      <router-link to="/auth/register" class="link link--bold">立即注册</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '@stores/user'
import { login, getUserProfile } from '@api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const showPwd = ref(false)
const rememberMe = ref(false)

const form = reactive({
  username: '',
  password: '',
})

async function handleLogin() {
  if (!form.username) return showToast('请输入手机号或用户名')
  if (!form.password) return showToast('请输入密码')

  loading.value = true
  try {
    const data = await login({ username: form.username, password: form.password })
    userStore.setToken(data.token, data.refreshToken)

    try {
      const userInfo = await getUserProfile()
      userStore.setUserInfo(userInfo)
    } catch {
      userStore.setUserInfo({
        id: data.userId,
        username: form.username,
      })
    }

    showToast('登录成功')
    const redirect = route.query.redirect || '/'
    router.replace(redirect)
  } catch {
    // 错误已在请求拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  width: 100%;
  padding: var(--space-6) 0;
}

/* ── 头部 ── */
.login-page__header {
  text-align: center;
  margin-bottom: var(--space-8);
  animation: slideDown 0.5s ease-out;
}

.login-page__logo {
  display: flex;
  justify-content: center;
  margin-bottom: var(--space-5);
}

.login-page__title {
  font-family: var(--font-display);
  font-size: var(--text-3xl);
  font-weight: var(--font-black);
  color: #fff;
  margin-bottom: var(--space-2);
  letter-spacing: 0.02em;
}

.login-page__subtitle {
  font-size: var(--text-sm);
  color: rgba(255, 255, 255, 0.4);
  font-weight: var(--font-light);
}

/* ── 暗色输入框 ── */
.form-group {
  margin-bottom: var(--space-4);
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 14px;
  z-index: 1;
  pointer-events: none;
}

.dark-input {
  width: 100%;
  padding: 13px 14px;
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

.dark-input--icon {
  padding-left: 42px;
}

.input-suffix {
  position: absolute;
  right: 12px;
  display: flex;
  align-items: center;
  padding: 4px;
  opacity: 0.7;
  transition: opacity var(--transition-fast);
}

.input-suffix:hover {
  opacity: 1;
}

/* ── 表单底部 ── */
.form-extra {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-6);
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: rgba(255, 255, 255, 0.45);
  cursor: pointer;
}

.checkbox {
  accent-color: var(--accent-500);
  width: 15px;
  height: 15px;
}

.link {
  font-size: var(--text-sm);
  color: rgba(255, 255, 255, 0.55);
  text-decoration: none;
  transition: color var(--transition-fast);
}

.link:hover {
  color: rgba(255, 255, 255, 0.85);
}

.link--bold {
  font-weight: var(--font-semibold);
  color: var(--accent-400);
}

/* ── 登录表单 ── */
.login-page__form {
  animation: slideUp 0.5s ease-out 0.1s both;
}

/* ── 提交按钮 ── */
.login-page__submit {
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
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-page__submit:active {
  transform: translateY(2px);
  box-shadow: 0 2px 0 0 rgba(0, 0, 0, 0.15);
}

.login-page__submit:disabled {
  opacity: 0.5;
  transform: none;
  box-shadow: 0 4px 0 0 rgba(0, 0, 0, 0.15);
}

/* ── 底部 ── */
.login-page__footer {
  margin-top: var(--space-8);
  display: flex;
  justify-content: center;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: rgba(255, 255, 255, 0.35);
  animation: fadeIn 0.4s ease-out 0.3s both;
}

/* 加载旋转 */
.spinner-submit {
  width: 20px;
  height: 20px;
  border: 2.5px solid var(--ink-200);
  border-top-color: var(--ink-600);
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
