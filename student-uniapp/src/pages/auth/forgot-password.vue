<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import AuthLayout from '@layouts/auth.vue'

const phone = ref('')
const code = ref('')
const newPassword = ref('')
const countdown = ref(0)
const loading = ref(false)
let timer: ReturnType<typeof setInterval> | null = null

function sendCode() {
  if (!phone.value.trim() || phone.value.length < 11) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  if (countdown.value > 0) return

  countdown.value = 60
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      if (timer) clearInterval(timer)
      timer = null
    }
  }, 1000)

  // TODO: call send SMS API
  uni.showToast({ title: '验证码已发送', icon: 'success' })
}

async function handleReset() {
  if (!phone.value.trim()) {
    uni.showToast({ title: '请输入手机号', icon: 'none' })
    return
  }
  if (!code.value.trim()) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }
  if (!newPassword.value || newPassword.value.length < 6) {
    uni.showToast({ title: '新密码至少6位', icon: 'none' })
    return
  }

  loading.value = true
  try {
    // TODO: call reset password API with phone, code, newPassword
    uni.showToast({ title: '密码重置成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1000)
  } catch (err: any) {
    const msg = err?.data?.message || err?.message || '重置失败，请重试'
    uni.showToast({ title: msg, icon: 'none' })
  } finally {
    loading.value = false
  }
}

function goToLogin() {
  uni.navigateBack()
}

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})
</script>

<template>
  <AuthLayout title="找回密码" subtitle="通过手机号重置密码">
    <view class="reset-form">
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

      <!-- 验证码 -->
      <view class="code-row">
        <view class="input-group code-input-group">
          <wd-icon name="mail" size="20px" color="var(--ink-400)" />
          <input
            v-model="code"
            class="form-input"
            type="number"
            placeholder="验证码"
            placeholder-class="input-placeholder"
            maxlength="6"
          />
        </view>
        <view
          class="code-btn"
          :class="{ 'code-btn--disabled': countdown > 0 }"
          @tap="sendCode"
        >
          <text class="code-btn-text">
            {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
          </text>
        </view>
      </view>

      <!-- 新密码 -->
      <view class="input-group">
        <wd-icon name="lock-on" size="20px" color="var(--ink-400)" />
        <input
          v-model="newPassword"
          class="form-input"
          type="password"
          placeholder="新密码（至少6位）"
          placeholder-class="input-placeholder"
          maxlength="20"
        />
      </view>

      <!-- 重置按钮 -->
      <button
        class="reset-btn"
        :class="{ 'reset-btn--loading': loading }"
        :disabled="loading"
        @tap="handleReset"
      >
        <text v-if="loading" class="btn-loading-text">提交中...</text>
        <text v-else class="btn-text">重置密码</text>
      </button>

      <!-- 返回登录 -->
      <text class="back-link" @tap="goToLogin">返回登录</text>
    </view>
  </AuthLayout>
</template>

<style scoped>
.reset-form {
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

/* ── 验证码行 ── */
.code-row {
  display: flex;
  gap: 10px;
}

.code-input-group {
  flex: 1;
}

.code-btn {
  height: 46px;
  padding: 0 18px;
  background: rgba(7, 193, 96, 0.12);
  border: 2px solid rgba(7, 193, 96, 0.25);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: opacity 0.2s;
}

.code-btn--disabled {
  opacity: 0.45;
}

.code-btn-text {
  color: var(--accent-500);
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}

/* ── 重置按钮 ── */
.reset-btn {
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

.reset-btn--loading {
  opacity: 0.7;
}

.reset-btn::after {
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
