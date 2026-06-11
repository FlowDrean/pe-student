<template>
  <div class="edit-trade page">
    <h2 class="edit-trade__heading">编辑商品</h2>
    <input v-model="title" class="input" placeholder="商品标题" maxlength="50" style="margin-bottom:12px" />
    <textarea v-model="desc" class="edit-trade__textarea" placeholder="描述商品成色、使用情况等..." maxlength="1000"></textarea>
    <div style="display:flex;gap:12px;margin-bottom:12px">
      <input v-model.number="price" class="input" type="number" placeholder="售价 (¥)" style="flex:1" />
      <input v-model.number="originalPrice" class="input" type="number" placeholder="原价 (¥)" style="flex:1" />
    </div>
    <div class="category-row">
      <select v-model="category" class="input"><option value="">选择分类</option><option v-for="c in TRADE_CATEGORIES" :key="c.key" :value="c.key">{{ c.label }}</option></select>
      <select v-model.number="condition" class="input"><option :value="0">选择成色</option><option v-for="c in CONDITIONS" :key="c.key" :value="c.key">{{ c.label }}</option></select>
    </div>
    <div v-if="loadError" class="edit-trade__error">加载商品信息失败，请返回重试</div>
    <div style="display:flex;gap:12px;margin-top:20px">
      <button class="btn btn-outline btn-block btn-lg" @click="handleDelete" :disabled="deleting">{{ deleting ? '删除中...' : '删除' }}</button>
      <button class="btn btn-primary btn-block btn-lg" @click="handleSubmit" :disabled="submitting">{{ submitting ? '保存中...' : '保存修改' }}</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { TRADE_CATEGORIES, CONDITIONS } from '@utils/constants'
import { getTradeDetail, updateTrade, deleteTrade } from '@api'

const router = useRouter()
const route = useRoute()
const tradeId = Number(route.params.id)
const title = ref(''); const desc = ref(''); const price = ref(0); const originalPrice = ref(0); const category = ref(''); const condition = ref(0)
const submitting = ref(false); const deleting = ref(false); const loadError = ref(false)
const canSubmit = computed(() => title.value.trim() && price.value > 0 && category.value && !submitting.value)

onMounted(async () => {
  if (!tradeId) { showToast('商品ID无效'); router.back(); return }
  try { const item = await getTradeDetail(tradeId); title.value = item.title || ''; desc.value = item.description || ''; price.value = item.price || 0; originalPrice.value = item.originalPrice || 0; category.value = item.category || ''; condition.value = item.condition || 0 }
  catch { loadError.value = true }
})

async function handleSubmit() {
  if (!canSubmit.value) { if (!title.value.trim()) return showToast('请输入商品标题'); if (!price.value) return showToast('请输入售价'); if (!category.value) return showToast('请选择分类'); return }
  submitting.value = true
  try { await updateTrade(tradeId, { title: title.value, description: desc.value, price: price.value, originalPrice: originalPrice.value || null, category: category.value, condition: condition.value || null }); showToast('保存成功'); router.back() }
  catch { showToast('保存失败，请重试') }
  finally { submitting.value = false }
}

async function handleDelete() {
  try { await showConfirmDialog({ title: '确认删除', message: '删除后无法恢复，确定要删除这个商品吗？', confirmButtonColor: '#dc2626' }) } catch { return }
  deleting.value = true
  try { await deleteTrade(tradeId); showToast('已删除'); router.replace('/trade') }
  catch { showToast('删除失败，请重试') }
  finally { deleting.value = false }
}
</script>

<style scoped>
.edit-trade { padding: var(--space-4) var(--content-padding); background: var(--bg-secondary); min-height: 100vh; }
.edit-trade__heading { font-family: var(--font-display); font-size: var(--text-xl); font-weight: var(--font-black); color: var(--ink-900); margin-bottom: var(--space-5); letter-spacing: -0.01em; }
.edit-trade__textarea { width: 100%; min-height: 120px; padding: 12px 16px; background: var(--bg-primary); border: 2px solid var(--ink-200); border-radius: var(--radius-lg); font-size: var(--text-base); line-height: 1.8; resize: vertical; font-family: var(--font-body); margin-bottom: 12px; transition: all var(--transition-fast); }
.edit-trade__textarea:focus { border-color: var(--accent-500); box-shadow: var(--shadow-glow-blue); outline: none; }
.category-row { display: flex; gap: 12px; }
.category-row select { flex: 1; }
.edit-trade__error { text-align: center; padding: var(--space-8); color: var(--ink-400); font-size: var(--text-sm); }
</style>
