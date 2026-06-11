<template>
  <div class="create-page page">
    <textarea v-model="content" class="create-page__textarea" placeholder="编辑内容..." rows="6" maxlength="2000"></textarea>
    <div class="create-page__actions">
      <button class="btn btn-primary btn-block btn-lg" @click="handleSave">保存修改</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
const router = useRouter()
const content = ref('原帖子内容...')
function handleSave() { showToast('保存成功'); router.back() }
</script>

<style scoped>
.create-page { padding: var(--space-4) var(--content-padding); background: var(--bg-secondary); min-height: 100vh; }
.create-page__textarea { width: 100%; min-height: 160px; padding: 14px 16px; background: var(--bg-primary); border: 2px solid var(--ink-200); border-radius: var(--radius-lg); font-size: var(--text-base); line-height: 1.8; resize: vertical; color: var(--ink-800); font-family: var(--font-body); transition: all var(--transition-fast); }
.create-page__textarea:focus { border-color: var(--accent-500); box-shadow: var(--shadow-glow-blue); outline: none; }
.create-page__actions { margin-top: var(--space-5); }
</style>
