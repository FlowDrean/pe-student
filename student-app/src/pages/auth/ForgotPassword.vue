<template>
  <div class="forgot-page">
    <h2 class="forgot-page__title">找回密码</h2>
    <p class="forgot-page__desc">输入注册手机号，我们将发送验证码帮助您重置密码</p>

    <!-- 短信验证码能力尚未接入，暂时无法通过自助方式重置密码 -->
    <div class="notice">
      <svg class="notice__icon" width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.6)" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      <h3 class="notice__title">功能暂未开放</h3>
      <p class="notice__text">
        短信验证码服务尚未接入，暂时无法在线重置密码。<br />
        如忘记密码，请联系平台管理员或发送邮件说明注册手机号进行人工申诉。
      </p>
      <button class="notice__back" @click="goLogin">返回登录</button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function goLogin() {
  router.push('/auth/login')
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

.notice {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-6) var(--space-4);
  background: rgba(255, 255, 255, 0.06);
  border: 2px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-lg);
  animation: slideUp 0.5s ease-out 0.1s both;
  text-align: center;
}

.notice__title {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: #fff;
  margin-top: var(--space-1);
}

.notice__text {
  font-size: var(--text-sm);
  color: rgba(255, 255, 255, 0.5);
  line-height: 1.8;
}

.notice__back {
  margin-top: var(--space-3);
  padding: 10px 32px;
  border-radius: var(--radius-md);
  border: none;
  background: #fff;
  color: var(--ink-900);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  font-family: var(--font-body);
  letter-spacing: 0.06em;
  cursor: pointer;
  transition: all var(--transition-fast);
  box-shadow: 0 4px 0 0 rgba(0, 0, 0, 0.15);
}

.notice__back:active {
  transform: translateY(2px);
  box-shadow: 0 2px 0 0 rgba(0, 0, 0, 0.15);
}
</style>
