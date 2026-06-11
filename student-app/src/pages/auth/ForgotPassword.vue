<template>
  <div class="forgot-page">
    <h2 class="forgot-page__title">找回密码</h2>
    <p class="forgot-page__desc">输入注册手机号，我们将发送验证码帮助您重置密码</p>
    <form @submit.prevent="handleSubmit" class="forgot-page__form">
      <div class="form-group">
        <input v-model="phone" class="dark-input" type="tel" placeholder="手机号" maxlength="11" />
      </div>
      <div class="form-group input-wrapper">
        <input v-model="code" class="dark-input dark-input--suffix" type="text" placeholder="验证码" maxlength="6" />
        <button type="button" class="code-btn" :disabled="countdown > 0" @click="sendCode">{{ countdown > 0 ? `${countdown}s` : '获取验证码' }}</button>
      </div>
      <div class="form-group">
        <input v-model="password" class="dark-input" type="password" placeholder="新密码（6-20位）" maxlength="20" />
      </div>
      <button type="submit" class="forgot-page__submit">重置密码</button>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { showToast } from 'vant'

const phone = ref('')
const code = ref('')
const password = ref('')
const countdown = ref(0)

function sendCode() {
  if (!phone.value) return showToast('请先输入手机号')
  countdown.value = 60
  const timer = setInterval(() => { countdown.value--; if (countdown.value <= 0) clearInterval(timer) }, 1000)
}

function handleSubmit() {
  showToast('密码重置成功')
}
</script>

<style scoped>
.forgot-page {
  padding: var(--space-6) 0;
}

.forgot-page__title {
  font-family: var(--font-display);
  font-size: var(--text-3xl);
  font-weight: var(--font-black);
  color: #fff;
  text-align: center;
  margin-bottom: var(--space-2);
  letter-spacing: 0.02em;
}

.forgot-page__desc {
  font-size: var(--text-sm);
  color: rgba(255, 255, 255, 0.4);
  text-align: center;
  margin-bottom: var(--space-6);
  line-height: 1.6;
}

.forgot-page__form {
  animation: slideUp 0.5s ease-out 0.1s both;
}

.form-group {
  margin-bottom: var(--space-3);
}

.input-wrapper {
  position: relative;
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

.dark-input--suffix {
  padding-right: 100px;
}

.code-btn {
  position: absolute;
  right: 4px;
  top: 50%;
  transform: translateY(-50%);
  padding: 5px 12px;
  border-radius: var(--radius-sm);
  background: rgba(255, 255, 255, 0.12);
  color: rgba(255, 255, 255, 0.7);
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  white-space: nowrap;
  transition: all var(--transition-fast);
}

.code-btn:disabled {
  background: rgba(255, 255, 255, 0.04);
  color: rgba(255, 255, 255, 0.2);
}

.forgot-page__submit {
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
  margin-top: var(--space-3);
}

.forgot-page__submit:active {
  transform: translateY(2px);
  box-shadow: 0 2px 0 0 rgba(0, 0, 0, 0.15);
}
</style>
