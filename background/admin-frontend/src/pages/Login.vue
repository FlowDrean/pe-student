<template>
  <div class="login-page">
    <!-- 装饰背景 -->
    <div class="login-bg">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
      <div class="dot-grid"></div>
    </div>

    <div class="login-card">
      <div class="card-header">
        <div class="brand-icon">P</div>
        <h1>PE-Student</h1>
        <p>后台管理系统</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入管理员账号（用户名或手机号）"
            :prefix-icon="User"
            size="large"
            @compositionstart="composing = true"
            @compositionend="composing = false"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
            @compositionstart="composing = true"
            @compositionend="composing = false"
            @keyup.enter="handleEnter"
          />
        </el-form-item>

        <el-button
          type="primary"
          size="large"
          class="login-btn"
          :loading="loading"
          @click="handleLogin"
        >
          {{ loading ? '登录中...' : '登 录' }}
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const adminStore = useAdminStore()
const formRef = ref(null)
const loading = ref(false)
const composing = ref(false)

const form = reactive({
  username: 'admin',
  password: 'admin123',
})

const rules = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

function handleEnter() {
  if (!composing.value) handleLogin()
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await adminStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (err) {
    ElMessage.error(err?.message || err?.response?.data?.message || '登录失败，请检查账号和密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--ink-800);
  position: relative;
  overflow: hidden;
}

/* 装饰元素 — 与现有 AuthLayout 风格一致 */
.login-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}
.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.12;
}
.orb-1 {
  width: 400px;
  height: 400px;
  background: var(--accent-500);
  top: -100px;
  right: -100px;
}
.orb-2 {
  width: 300px;
  height: 300px;
  background: var(--coral-500);
  bottom: -80px;
  left: -80px;
}
.orb-3 {
  width: 200px;
  height: 200px;
  background: var(--amber-500);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
.dot-grid {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(255,255,255,0.04) 1px, transparent 1px);
  background-size: 24px 24px;
}

.login-card {
  position: relative;
  z-index: 1;
  width: 400px;
  background: #fff;
  border-radius: 14px;
  padding: 48px 40px 40px;
  box-shadow: 0 24px 80px rgba(0,0,0,0.3);
}
.card-header {
  text-align: center;
  margin-bottom: 36px;
}
.brand-icon {
  width: 52px;
  height: 52px;
  background: var(--accent-500);
  color: #fff;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-size: 1.5rem;
  font-weight: 700;
  box-shadow: 0 3px 0 0 rgba(6, 173, 86, 0.3);
  margin-bottom: 16px;
}
.card-header h1 {
  font-family: var(--font-display);
  font-size: 1.5rem;
  color: var(--ink-800);
  margin: 0 0 4px;
}
.card-header p {
  font-size: 0.875rem;
  color: var(--ink-400);
  margin: 0;
}

.login-form :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--ink-100) inset;
  border-radius: 8px;
}
.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--ink-200) inset;
}
.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--accent-500) inset;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 8px;
  margin-top: 8px;
  letter-spacing: 2px;
  box-shadow: var(--shadow-button);
}
.login-btn:active {
  box-shadow: 0 1px 0 0 rgba(26, 28, 36, 0.2);
  transform: translateY(2px);
}
</style>
