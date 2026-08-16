<template>
  <div class="trade-page">
    <h2 class="page-title">商品管理</h2>

    <div class="admin-card search-bar">
      <el-input v-model="search.keyword" placeholder="搜索商品标题" clearable style="width: 240px" :prefix-icon="Search"
        @clear="fetchData" @compositionstart="composing = true" @compositionend="composing = false"
        @keyup.enter="composing || fetchData()" />
      <el-select v-model="search.category" placeholder="分类筛选" clearable style="width: 140px" @change="fetchData">
        <el-option label="数码" value="数码" /><el-option label="书籍" value="书籍" />
        <el-option label="生活" value="生活" /><el-option label="服饰" value="服饰" /><el-option label="其他" value="其他" />
      </el-select>
      <el-select v-model="search.status" placeholder="状态筛选" clearable style="width: 140px" @change="fetchData">
        <el-option label="在售" :value="1" /><el-option label="交易中" :value="3" />
        <el-option label="已售" :value="2" /><el-option label="已下架" :value="0" />
      </el-select>
      <el-button type="primary" @click="fetchData"><el-icon><Search /></el-icon> 搜索</el-button>
    </div>

    <div class="admin-card" style="padding: 0; overflow: hidden;">
      <el-table :data="list" v-loading="loading" stripe style="width: 100%">
        <el-table-column label="商品信息" min-width="240">
          <template #default="{ row }">
            <div class="trade-cell">
              <img v-if="row.images?.length" :src="getImageUrl(row.images[0])" class="trade-thumb" @error="onImgError" />
              <div v-else class="trade-thumb-placeholder"><el-icon><ShoppingBag /></el-icon></div>
              <div>
                <div class="trade-title">{{ row.title }}</div>
                <div class="trade-price">
                  <span class="price">¥{{ row.price }}</span>
                  <span v-if="row.originalPrice" class="original-price">¥{{ row.originalPrice }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="卖家" width="130">
          <template #default="{ row }">
            <div class="user-cell" v-if="row.seller">
              <el-avatar :size="24" :src="row.seller.avatar"><el-icon><UserFilled /></el-icon></el-avatar>
              <span>{{ row.seller.nickname || row.seller.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="80"><template #default="{ row }"><el-tag size="small" effect="light" type="info">{{ row.category || '-' }}</el-tag></template></el-table-column>
        <el-table-column label="状态" width="90"><template #default="{ row }"><el-tag :type="statusType(row.status)" size="small" effect="light">{{ statusMap[row.status] || '未知' }}</el-tag></template></el-table-column>
        <el-table-column label="浏览" width="70" prop="views" />
        <el-table-column label="发布时间" width="170"><template #default="{ row }">{{ formatDate(row.createdAt) }}</template></el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="showDetail(row)">详情</el-button>
            <el-button size="small" link @click="showEdit(row)">编辑</el-button>
            <el-button v-if="row.status !== 1" size="small" type="success" link @click="updateStatus(row, 1)">上架</el-button>
            <el-button v-if="row.status === 1" size="small" type="warning" link @click="updateStatus(row, 0)">下架</el-button>
            <el-button size="small" type="danger" link @click="deleteTrade(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-footer">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
          :total="pagination.total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
          @current-change="fetchData" @size-change="fetchData" />
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="商品详情" width="640px">
      <template v-if="currentTrade">
        <div v-if="currentTrade.images?.length" style="display:flex;gap:8px;flex-wrap:wrap;margin-bottom:16px;">
          <img v-for="(img, i) in currentTrade.images" :key="i" :src="getImageUrl(img)" style="width:150px;height:150px;object-fit:cover;border-radius:8px;" @error="onImgError" />
        </div>
        <h3 style="margin-bottom:8px;">{{ currentTrade.title }}</h3>
        <div style="color: var(--accent-500); font-size:1.5rem; font-weight:700; margin-bottom:12px;">¥{{ currentTrade.price }}</div>
        <div style="color: var(--text-secondary); margin-bottom: 16px; white-space: pre-wrap;">{{ currentTrade.description }}</div>
        <el-descriptions :column="2" size="small" border>
          <el-descriptions-item label="卖家">{{ currentTrade.seller?.nickname || currentTrade.seller?.username }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ currentTrade.category || '-' }}</el-descriptions-item>
          <el-descriptions-item label="成色">{{ conditionMap[currentTrade.condition] || '-' }}</el-descriptions-item>
          <el-descriptions-item label="交易地点">{{ currentTrade.location || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ currentTrade.contact || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag :type="statusType(currentTrade.status)" size="small">{{ statusMap[currentTrade.status] }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatDate(currentTrade.createdAt) }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑商品" width="640px" @close="editFormRef?.resetFields()">
      <el-form v-if="editForm" ref="editFormRef" :model="editForm" label-width="80px">
        <el-form-item label="标题"><el-input v-model="editForm.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="editForm.description" type="textarea" :rows="4" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="售价"><el-input v-model="editForm.price" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="原价"><el-input v-model="editForm.originalPrice" placeholder="选填" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="分类"><el-input v-model="editForm.category" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="成色">
              <el-select v-model="editForm.condition" style="width:100%">
                <el-option v-for="(v, k) in conditionMap" :key="k" :label="v" :value="Number(k)" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-select v-model="editForm.status" style="width:100%">
                <el-option v-for="(v, k) in statusMap" :key="k" :label="v" :value="Number(k)" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="交易地点"><el-input v-model="editForm.location" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系方式"><el-input v-model="editForm.contact" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { tradeApi } from '@/api/trade'
import { formatDate } from '@/utils/format'
import { Search, UserFilled, ShoppingBag } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false), composing = ref(false), saving = ref(false)
const list = ref([]), detailVisible = ref(false), currentTrade = ref(null)
const editVisible = ref(false), editForm = ref(null), editFormRef = ref(null)
const editingTradeId = ref(null)

const search = reactive({ keyword: '', category: '', status: null })
const pagination = reactive({ page: 1, size: 10, total: 0 })

const statusMap = { 0: '已下架', 1: '在售', 2: '已售', 3: '交易中' }
const statusType = (s) => ({ 0: 'info', 1: 'success', 2: 'warning', 3: 'primary' }[s] || 'info')
const conditionMap = { 1: '全新', 2: '九成新', 3: '八成新', 4: '七成新及以下' }

// 图片走相对路径（开发环境由 vite 代理 /uploads，生产同源部署直接生效）；跨域部署时通过 VITE_BACKEND_URL 覆盖
const BACKEND = import.meta.env.VITE_BACKEND_URL || ''
function getImageUrl(url) { if (!url) return ''; if (url.startsWith('http')) return url; return BACKEND + (url.startsWith('/') ? url : '/' + url) }
function onImgError(e) { e.target.style.display = 'none' }

async function fetchData() {
  loading.value = true
  try {
    const res = await tradeApi.getList({ page: pagination.page, size: pagination.size, keyword: search.keyword || undefined, category: search.category || undefined, status: search.status })
    list.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (e) { /* handled */ } finally { loading.value = false }
}

async function showDetail(row) { const res = await tradeApi.getDetail(row.id); currentTrade.value = res.data; detailVisible.value = true }

async function showEdit(row) {
  const res = await tradeApi.getDetail(row.id)
  const t = res.data
  editingTradeId.value = t.id
  editForm.value = {
    title: t.title || '', description: t.description || '',
    price: t.price ?? '', originalPrice: t.originalPrice ?? '',
    category: t.category || '', condition: t.condition ?? 1,
    location: t.location || '', contact: t.contact || '',
    status: t.status ?? 1,
  }
  editVisible.value = true
}

async function saveEdit() {
  saving.value = true
  try {
    await tradeApi.update(editingTradeId.value, editForm.value)
    ElMessage.success('保存成功')
    editVisible.value = false
    fetchData()
  } catch (e) { /* handled */ } finally { saving.value = false }
}

async function updateStatus(row, status) {
  const labels = { 0: '下架', 1: '上架', 2: '标记为已售', 3: '标记为交易中' }
  try {
    await ElMessageBox.confirm(`确定要${labels[status]}该商品吗？`, '确认操作', { type: 'warning' })
    await tradeApi.updateStatus(row.id, status)
    ElMessage.success('操作成功')
    fetchData()
  } catch (e) { /* cancelled */ }
}

async function deleteTrade(row) {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '确认删除', { type: 'warning', confirmButtonText: '确定删除' })
    await tradeApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) { /* cancelled */ }
}

onMounted(fetchData)
</script>

<style scoped>
.search-bar { padding: 16px 20px; margin-bottom: 16px; display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.trade-cell { display: flex; align-items: center; gap: 12px; }
.trade-thumb { width: 56px; height: 56px; object-fit: cover; border-radius: 6px; border: 1px solid var(--border-color); flex-shrink: 0; }
.trade-thumb-placeholder { width: 56px; height: 56px; background: var(--bg-page); border-radius: 6px; display: flex; align-items: center; justify-content: center; color: var(--text-muted); font-size: 1.25rem; flex-shrink: 0; }
.trade-title { font-weight: 600; color: var(--text-primary); font-size: 0.875rem; margin-bottom: 4px; }
.trade-price .price { color: var(--coral-500); font-weight: 700; font-size: 0.9375rem; }
.trade-price .original-price { color: var(--text-muted); font-size: 0.75rem; text-decoration: line-through; margin-left: 8px; }
.user-cell { display: flex; align-items: center; gap: 8px; font-size: 0.875rem; }
.table-footer { padding: 16px 20px; display: flex; justify-content: flex-end; }
</style>
