<script setup lang="ts">
import { ref } from 'vue'
import AuthLayout from '@layouts/auth.vue'
import { authApi } from '@api/auth'
import { useUserStore } from '@stores/user'

const userStore = useUserStore()

const username = ref('')
const password = ref('')
const rememberMe = ref(false)
const loading = ref(false)

async function handleLogin() {
  if (!username.value.trim()) {
    uni.showToast({ title: '请输入用户名', icon: 'none' })
    return
  }
  if (!password.value) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return
  }

  loading.value = true
  try {
    const res = await authApi.login({
      username: username.value.trim(),
      password: password.value,
    })
    userStore.setToken(res.token, res.refreshToken)
    userStore.setUserInfo({
      id: Number(res.userId) || 0,
      username: res.username || username.value.trim(),
      nickname: res.nickname || res.username || username.value.trim(),
    })

    if (rememberMe.value) {
      uni.setStorageSync('student_app_remembered_username', username.value.trim())
    } else {
      uni.removeStorageSync('student_app_remembered_username')
    }

    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/index/index' })
    }, 500)
  } catch (err: any) {
    const msg = err?.data?.message || err?.message || '登录失败，请重试'
    uni.showToast({ title: msg, icon: 'none' })
  } finally {
    loading.value = false
  }
}

function goToRegister() {
  uni.navigateTo({ url: '/pages/auth/register' })
}

function goToForgot() {
  uni.navigateTo({ url: '/pages/auth/forgot-password' })
}

// 恢复记住的用户名
try {
  const saved = uni.getStorageSync('student_app_remembered_username')
  if (saved) {
    username.value = saved
    rememberMe.value = true
  }
} catch (_) { /* ignore */ }
</script>

<template>
  <AuthLayout title="PE-Student" subtitle="校园互助平台" :showBack="false">
    <view class="login-form">
      <!-- 用户名 -->
      <view class="input-group">
        <wd-icon name="user" size="20px" color="var(--ink-400)" />
        <input
          v-model="username"
          class="form-input"
          type="text"
          placeholder="用户名"
          placeholder-class="input-placeholder"
          maxlength="30"
          confirm-type="next"
        />
      </view>

      <!-- 密码 -->
      <view class="input-group">
        <wd-icon name="lock-on" size="20px" color="var(--ink-400)" />
        <input
          v-model="password"
          class="form-input"
          type="password"
          placeholder="密码"
          placeholder-class="input-placeholder"
          maxlength="20"
          confirm-type="done"
          @confirm="handleLogin"
        />
      </view>

      <!-- 记住我 -->
      <view class="inline-options">
        <view class="remember-row" @tap="rememberMe = !rememberMe">
          <view class="check-box" :class="{ 'check-box--active': rememberMe }">
            <wd-icon v-if="rememberMe" name="check" size="16px" color="#fff" />
          </view>
          <text class="remember-text">记住我</text>
        </view>
      </view>

      <!-- 登录按钮 -->
      <button
        class="login-btn"
        :class="{ 'login-btn--loading': loading }"
        :disabled="loading"
        @tap="handleLogin"
      >
        <text v-if="loading" class="btn-loading-text">登录中...</text>
        <text v-else class="btn-text">登 录</text>
      </button>

      <!-- 底部链接 -->
      <view class="auth-links">
        <text class="auth-link" @tap="goToRegister">注册新账号</text>
        <view class="link-divider" />
        <text class="auth-link" @tap="goToForgot">忘记密码</text>
      </view>
    </view>
  </AuthLayout>
</template>

<style scoped>
.login-form {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ── 输入框组 ── */
.input-group {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.05);
  border: 2px solid var(--ink-700);
  border-radius: var(--radius-md);
  padding: 0 14px;
  transition: border-color 0.2s;
}

.input-group:focus-within {
  border-color: var(--accent-500);
}

.input-icon {
  font-size: 14px;
  margin-right: 10px;
  color: var(--ink-500);
}

.form-input {
  flex: 1;
  height: 46px;
  font-size: 15px;
  color: #fff;
  background: transparent;
  border: none;
  outline: none;
}

.input-placeholder {
  color: var(--ink-500);
  font-size: 14px;
}

/* ── 记住我 ── */
.inline-options {
  display: flex;
  align-items: center;
}

.remember-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.check-box {
  width: 18px;
  height: 18px;
  border: 2px solid var(--ink-600);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.check-box--active {
  background: var(--accent-500);
  border-color: var(--accent-500);
}

.check-mark {
  font-size: 12px;
  color: #fff;
  font-weight: 700;
}

.remember-text {
  font-size: 13px;
  color: var(--ink-400);
}

/* ── 登录按钮 ── */
.login-btn {
  width: 100%;
  height: 48px;
  background: var(--accent-500);
  border: 2px solid var(--accent-500);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 4px;
  transition: opacity 0.2s;
}

.login-btn--loading {
  opacity: 0.7;
}

.login-btn::after {
  border: none;
}

.btn-text {
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 2px;
}

.btn-loading-text {
  font-size: 14px;
  color: #fff;
}

/* ── 底部链接 ── */
.auth-links {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  margin-top: 8px;
}

.auth-link {
  font-size: 13px;
  color: var(--ink-400);
  transition: color 0.2s;
}

.auth-link:active {
  color: var(--accent-500);
}

.link-divider {
  width: 1px;
  height: 12px;
  background: var(--ink-600);
}
</style>
