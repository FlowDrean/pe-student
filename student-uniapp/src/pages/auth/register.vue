<script setup lang="ts">
import { ref } from 'vue'
import AuthLayout from '@layouts/auth.vue'
import { authApi } from '@api/auth'

const username = ref('')
const phone = ref('')
const password = ref('')
const school = ref('')
const agreeTerms = ref(false)
const agreePrivacy = ref(false)
const loading = ref(false)

async function handleRegister() {
  if (!username.value.trim()) {
    uni.showToast({ title: '请输入用户名', icon: 'none' })
    return
  }
  if (!phone.value.trim() || phone.value.length < 11) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  if (!password.value || password.value.length < 6) {
    uni.showToast({ title: '密码至少6位', icon: 'none' })
    return
  }
  if (!agreeTerms.value || !agreePrivacy.value) {
    uni.showToast({ title: '请阅读并同意相关协议', icon: 'none' })
    return
  }

  loading.value = true
  try {
    await authApi.register({
      username: username.value.trim(),
      password: password.value,
      phone: phone.value.trim(),
      school: school.value.trim() || undefined,
    })
    uni.showToast({ title: '注册成功，请登录', icon: 'success' })
    setTimeout(() => {
      uni.redirectTo({ url: '/pages/auth/login' })
    }, 800)
  } catch (err: any) {
    const msg = err?.data?.message || err?.message || '注册失败，请重试'
    uni.showToast({ title: msg, icon: 'none' })
  } finally {
    loading.value = false
  }
}

function goToLogin() {
  uni.redirectTo({ url: '/pages/auth/login' })
}
</script>

<template>
  <AuthLayout title="PE-Student" subtitle="创建你的账号">
    <view class="register-form">
      <!-- 用户名 -->
      <view class="input-group">
        <wd-icon name="user" size="20px" color="var(--ink-400)" />
        <input
          v-model="username"
          class="form-input"
          type="text"
          placeholder="用户名"
          placeholder-class="input-placeholder"
          maxlength="20"
        />
      </view>

      <!-- 手机号 -->
      <view class="input-group">
        <wd-icon name="phone" size="20px" color="var(--ink-400)" />
        <input
          v-model="phone"
          class="form-input"
          type="number"
          placeholder="手机号"
          placeholder-class="input-placeholder"
          maxlength="11"
        />
      </view>

      <!-- 密码 -->
      <view class="input-group">
        <wd-icon name="lock-on" size="20px" color="var(--ink-400)" />
        <input
          v-model="password"
          class="form-input"
          type="password"
          placeholder="密码（至少6位）"
          placeholder-class="input-placeholder"
          maxlength="20"
        />
      </view>

      <!-- 学校 -->
      <view class="input-group">
        <wd-icon name="home" size="20px" color="var(--ink-400)" />
        <input
          v-model="school"
          class="form-input"
          type="text"
          placeholder="学校（选填）"
          placeholder-class="input-placeholder"
          maxlength="30"
        />
      </view>

      <!-- 协议勾选 -->
      <view class="agreement-section">
        <view class="agree-row" @tap="agreeTerms = !agreeTerms">
          <view class="check-box" :class="{ 'check-box--active': agreeTerms }">
            <wd-icon v-if="agreeTerms" name="check" size="16px" color="#fff" />
          </view>
          <text class="agree-text">
            我已阅读并同意 <text class="agree-link">《用户协议》</text>
          </text>
        </view>
        <view class="agree-row" @tap="agreePrivacy = !agreePrivacy">
          <view class="check-box" :class="{ 'check-box--active': agreePrivacy }">
            <wd-icon v-if="agreePrivacy" name="check" size="16px" color="#fff" />
          </view>
          <text class="agree-text">
            我已阅读并同意 <text class="agree-link">《隐私政策》</text>
          </text>
        </view>
      </view>

      <!-- 注册按钮 -->
      <button
        class="register-btn"
        :class="{ 'register-btn--loading': loading }"
        :disabled="loading"
        @tap="handleRegister"
      >
        <text v-if="loading" class="btn-loading-text">注册中...</text>
        <text v-else class="btn-text">注 册</text>
      </button>

      <!-- 返回登录 -->
      <text class="back-link" @tap="goToLogin">已有账号？立即登录</text>
    </view>
  </AuthLayout>
</template>

<style scoped>
.register-form {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 14px;
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

/* ── 协议勾选 ── */
.agreement-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 4px 0;
}

.agree-row {
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
  flex-shrink: 0;
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

.agree-text {
  font-size: 12px;
  color: var(--ink-400);
  line-height: 1.5;
}

.agree-link {
  color: var(--accent-500);
}

/* ── 注册按钮 ── */
.register-btn {
  width: 100%;
  height: 48px;
  background: var(--accent-500);
  border: 2px solid var(--accent-500);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 6px;
  transition: opacity 0.2s;
}

.register-btn--loading {
  opacity: 0.7;
}

.register-btn::after {
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

/* ── 返回链接 ── */
.back-link {
  font-size: 13px;
  color: var(--ink-400);
  text-align: center;
  margin-top: 4px;
  transition: color 0.2s;
}

.back-link:active {
  color: var(--accent-500);
}
</style>
