<template>
  <header class="admin-header">
    <div class="header-left">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="header-right">
      <!-- 主题切换 -->
      <button class="theme-toggle" :title="isDark ? '切换亮色模式' : '切换暗色模式'" @click="toggleTheme">
        <el-icon :size="18">
          <Sunny v-if="isDark" />
          <Moon v-else />
        </el-icon>
      </button>

      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="32" :icon="UserFilled" />
          <span class="user-name">{{ adminStore.userInfo?.nickname || adminStore.userInfo?.username || '管理员' }}</span>
          <el-icon class="arrow"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup>
import { computed, ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import { UserFilled, ArrowDown, SwitchButton, Sunny, Moon } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()

const currentTitle = computed(() => route.meta?.title)
const isDark = ref(false)

function applyTheme(dark) {
  isDark.value = dark
  document.documentElement.setAttribute('data-theme', dark ? 'dark' : 'light')
  localStorage.setItem('admin_theme', dark ? 'dark' : 'light')
}

function toggleTheme() {
  applyTheme(!isDark.value)
}

// 启动时从 localStorage 读取，默认暗色
onMounted(() => {
  const saved = localStorage.getItem('admin_theme')
  applyTheme(saved === 'light' ? false : true) // 默认暗色
})

function handleCommand(cmd) {
  if (cmd === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning',
    }).then(() => {
      adminStore.logout()
      router.push('/login')
    }).catch(() => {})
  }
}
</script>

<style scoped>
.admin-header {
  height: 56px;
  background: var(--bg-header);
  border-bottom: 2px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
  transition: background 0.3s ease, border-color 0.3s ease;
}
.header-left { display: flex; align-items: center; }
.header-right { display: flex; align-items: center; gap: 8px; }

/* 主题切换按钮 */
.theme-toggle {
  width: 34px; height: 34px;
  display: flex; align-items: center; justify-content: center;
  border: 2px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-card);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
}
.theme-toggle:hover {
  border-color: var(--accent-500);
  color: var(--accent-500);
  background: rgba(7, 193, 96, 0.06);
}

.user-info {
  display: flex; align-items: center; gap: 8px;
  cursor: pointer; padding: 4px 8px;
  border-radius: var(--radius-md);
  transition: background 0.15s;
}
.user-info:hover { background: var(--hover-row); }
.user-name { font-size: 0.875rem; font-weight: 500; color: var(--text-primary); }
.arrow { font-size: 0.75rem; color: var(--text-muted); }
</style>
