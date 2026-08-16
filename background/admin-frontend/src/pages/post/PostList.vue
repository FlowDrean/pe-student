<template>
  <div class="post-page">
    <h2 class="page-title">帖子管理</h2>

    <div class="admin-card search-bar">
      <el-input v-model="search.keyword" placeholder="搜索帖子标题" clearable style="width: 240px" :prefix-icon="Search"
        @clear="fetchData" @compositionstart="composing = true" @compositionend="composing = false"
        @keyup.enter="composing || fetchData()" />
      <el-select v-model="search.category" placeholder="分类筛选" clearable style="width: 140px" @change="fetchData">
        <el-option label="求助" value="HELP" /><el-option label="美食" value="FOOD" />
        <el-option label="学习" value="STUDY" /><el-option label="生活" value="LIFE" /><el-option label="其他" value="OTHER" />
      </el-select>
      <el-select v-model="search.status" placeholder="状态筛选" clearable style="width: 140px" @change="fetchData">
        <el-option label="正常" :value="1" /><el-option label="审核中" :value="2" /><el-option label="已删除" :value="0" />
      </el-select>
      <el-button type="primary" @click="fetchData"><el-icon><Search /></el-icon> 搜索</el-button>
    </div>

    <div class="admin-card" style="padding: 0; overflow: hidden;">
      <el-table :data="list" v-loading="loading" stripe style="width: 100%">
        <el-table-column label="标题" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">{{ row.title || '(无标题)' }}</template>
        </el-table-column>
        <el-table-column label="作者" width="140">
          <template #default="{ row }">
            <div class="user-cell" v-if="row.author">
              <el-avatar :size="28" :src="row.author.avatar"><el-icon><UserFilled /></el-icon></el-avatar>
              <span>{{ row.author.nickname || row.author.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="80">
          <template #default="{ row }"><el-tag size="small" effect="light" type="info">{{ categoryMap[row.category] || row.category }}</el-tag></template>
        </el-table-column>
        <el-table-column label="数据" width="160">
          <template #default="{ row }">
            <span class="data-item"><el-icon><Star /></el-icon> {{ row.likes }}</span>
            <span class="data-item"><el-icon><ChatDotSquare /></el-icon> {{ row.comments }}</span>
            <span class="data-item"><el-icon><View /></el-icon> {{ row.views }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }"><el-tag :type="statusType(row.status)" size="small" effect="light">{{ statusMap[row.status] || '未知' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="发布时间" width="170"><template #default="{ row }">{{ formatDate(row.createdAt) }}</template></el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="showDetail(row)">详情</el-button>
            <el-button size="small" link @click="showEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 2" size="small" type="success" link @click="updateStatus(row, 1)">通过</el-button>
            <el-button v-if="row.status === 1" size="small" type="warning" link @click="updateStatus(row, 0)">下架</el-button>
            <el-button v-if="row.status === 0" size="small" type="success" link @click="updateStatus(row, 1)">恢复</el-button>
            <el-button size="small" type="danger" link @click="deletePost(row)">删除</el-button>
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
    <el-dialog v-model="detailVisible" title="帖子详情" width="640px">
      <template v-if="currentPost">
        <h3 style="margin-bottom:12px;">{{ currentPost.title || '(无标题)' }}</h3>
        <div style="color: var(--text-secondary); margin-bottom: 16px; white-space: pre-wrap;">{{ currentPost.content }}</div>
        <div v-if="currentPost.images?.length" style="display:flex;gap:8px;flex-wrap:wrap;margin-bottom:16px;">
          <img v-for="(img, i) in currentPost.images" :key="i" :src="getImageUrl(img)" style="width:120px;height:120px;object-fit:cover;border-radius:6px;" @error="onImgError" />
        </div>
        <el-descriptions :column="2" size="small" border>
          <el-descriptions-item label="作者">{{ currentPost.author?.nickname || currentPost.author?.username }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ categoryMap[currentPost.category] || currentPost.category }}</el-descriptions-item>
          <el-descriptions-item label="点赞">{{ currentPost.likes }}</el-descriptions-item>
          <el-descriptions-item label="评论">{{ currentPost.comments }}</el-descriptions-item>
          <el-descriptions-item label="浏览">{{ currentPost.views }}</el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag :type="statusType(currentPost.status)" size="small">{{ statusMap[currentPost.status] }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatDate(currentPost.createdAt) }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑帖子" width="640px" @close="editFormRef?.resetFields()">
      <el-form v-if="editForm" ref="editFormRef" :model="editForm" label-width="80px">
        <el-form-item label="标题"><el-input v-model="editForm.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="editForm.content" type="textarea" :rows="6" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="分类">
              <el-select v-model="editForm.category" style="width:100%">
                <el-option v-for="(v, k) in categoryMap" :key="k" :label="v" :value="k" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="置顶">
              <el-switch v-model="editForm.isTop" :active-value="1" :inactive-value="0" />
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
        <el-form-item label="标签">
          <el-input v-model="tagInput" placeholder="多个标签用逗号分隔，如：编程,书籍" />
        </el-form-item>
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
import { postApi } from '@/api/post'
import { formatDate } from '@/utils/format'
import { Search, UserFilled, Star, ChatDotSquare, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false), composing = ref(false), saving = ref(false)
const list = ref([]), detailVisible = ref(false), currentPost = ref(null)
const editVisible = ref(false), editForm = ref(null), editFormRef = ref(null)
const tagInput = ref(''), editingPostId = ref(null)

const search = reactive({ keyword: '', category: '', status: null })
const pagination = reactive({ page: 1, size: 10, total: 0 })

const categoryMap = { HELP: '求助', FOOD: '美食', STUDY: '学习', LIFE: '生活', OTHER: '其他' }
const statusMap = { 0: '已删除', 1: '正常', 2: '审核中' }
const statusType = (s) => ({ 0: 'danger', 1: 'success', 2: 'warning' }[s] || 'info')

// 图片走相对路径（开发环境由 vite 代理 /uploads，生产同源部署直接生效）；跨域部署时通过 VITE_BACKEND_URL 覆盖
const BACKEND = import.meta.env.VITE_BACKEND_URL || ''
function getImageUrl(url) { if (!url) return ''; if (url.startsWith('http')) return url; return BACKEND + (url.startsWith('/') ? url : '/' + url) }
function onImgError(e) { e.target.style.display = 'none' }

async function fetchData() {
  loading.value = true
  try {
    const res = await postApi.getList({ page: pagination.page, size: pagination.size, keyword: search.keyword || undefined, category: search.category || undefined, status: search.status })
    list.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (e) { /* handled */ } finally { loading.value = false }
}

async function showDetail(row) {
  const res = await postApi.getDetail(row.id)
  currentPost.value = res.data
  detailVisible.value = true
}

async function showEdit(row) {
  const res = await postApi.getDetail(row.id)
  const p = res.data
  editingPostId.value = p.id
  editForm.value = {
    title: p.title || '',
    content: p.content || '',
    category: p.category || '',
    isTop: p.isTop ?? 0,
    status: p.status ?? 1,
  }
  tagInput.value = (p.tags || []).join(',')
  editVisible.value = true
}

async function saveEdit() {
  saving.value = true
  try {
    const data = { ...editForm.value }
    data.tags = tagInput.value ? tagInput.value.split(',').map(s => s.trim()).filter(Boolean) : []
    await postApi.update(editingPostId.value, data)
    ElMessage.success('保存成功')
    editVisible.value = false
    fetchData()
  } catch (e) { /* handled */ } finally { saving.value = false }
}

async function updateStatus(row, status) {
  const labels = { 0: '下架', 1: '通过/恢复', 2: '设为审核中' }
  try {
    await ElMessageBox.confirm(`确定要${labels[status]}该帖子吗？`, '确认操作', { type: 'warning' })
    await postApi.updateStatus(row.id, status)
    ElMessage.success('操作成功')
    fetchData()
  } catch (e) { /* cancelled */ }
}

async function deletePost(row) {
  try {
    await ElMessageBox.confirm('确定要删除该帖子吗？', '确认删除', { type: 'warning', confirmButtonText: '确定删除' })
    await postApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) { /* cancelled */ }
}

onMounted(fetchData)
</script>

<style scoped>
.search-bar { padding: 16px 20px; margin-bottom: 16px; display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.user-cell { display: flex; align-items: center; gap: 8px; font-size: 0.875rem; }
.data-item { display: inline-flex; align-items: center; gap: 2px; margin-right: 12px; font-size: 0.8125rem; color: var(--text-muted); }
.table-footer { padding: 16px 20px; display: flex; justify-content: flex-end; }
</style>
