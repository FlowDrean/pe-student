<script setup lang="ts">
import AuthLayout from '@layouts/auth.vue'

function goToLogin() {
  uni.navigateBack()
}
</script>

<template>
  <AuthLayout title="找回密码" subtitle="通过手机号重置密码">
    <!-- 短信验证码能力尚未接入，暂时无法通过自助方式重置密码 -->
    <view class="notice">
      <view class="notice__icon-wrap">
        <wd-icon name="warning" size="42px" color="rgba(255,255,255,0.55)" />
      </view>
      <text class="notice__title">功能暂未开放</text>
      <text class="notice__text">
        短信验证码服务尚未接入，暂时无法在线重置密码。\n如忘记密码，请联系平台管理员，并提供注册手机号进行人工申诉。
      </text>
      <button class="notice__back" @tap="goToLogin">
        <text class="notice__back-text">返回登录</text>
      </button>
    </view>
  </AuthLayout>
</template>

<style scoped>
.notice {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 36px 24px;
  background: rgba(255, 255, 255, 0.05);
  border: 2px solid var(--ink-700);
  border-radius: var(--radius-lg);
  text-align: center;
}

.notice__icon-wrap {
  margin-bottom: 4px;
}

.notice__title {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
}

.notice__text {
  font-size: 13px;
  color: var(--ink-400);
  line-height: 1.8;
}

.notice__back {
  margin-top: 12px;
  padding: 0 36px;
  height: 44px;
  background: #fff;
  border: none;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.notice__back::after {
  border: none;
}

.notice__back-text {
  font-size: 15px;
  font-weight: 700;
  color: #0f1119;
  letter-spacing: 1px;
}
</style>
