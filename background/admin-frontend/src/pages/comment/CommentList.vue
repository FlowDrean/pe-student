<template>
  <div class="comment-page">
    <h2 class="page-title">评论管理</h2>

    <div class="admin-card search-bar">
      <el-input v-model="search.keyword" placeholder="搜索评论内容" clearable style="width: 280px" :prefix-icon="Search"
        @clear="fetchData" @compositionstart="composing = true" @compositionend="composing = false"
        @keyup.enter="composing || fetchData()" />
      <el-input v-model="search.postId" placeholder="帖子ID（可选）" clearable style="width: 160px" @clear="fetchData"
        @compositionstart="composing = true" @compositionend="composing = false"
        @keyup.enter="composing || fetchData()" />
      <el-button type="primary" @click="fetchData"><el-icon><Search /></el-icon> 搜索</el-button>
    </div>

    <div class="admin-card" style="padding: 0; overflow: hidden;">
      <el-table :data="list" v-loading="loading" stripe style="width: 100%">
        <el-table-column label="评论内容" min-width="280" show-overflow-tooltip>
          <template #default="{ row }">{{ row.content }}</template>
        </el-table-column>
        <el-table-column label="评论者" width="140">
          <template #default="{ row }">
            <div class="user-cell" v-if="row.user">
              <el-avatar :size="24" :src="row.user.avatar"><el-icon><UserFilled /></el-icon></el-avatar>
              <span>{{ row.user.nickname || row.user.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="所属帖子" width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.postTitle || `帖子#${row.postId}` }}</template>
        </el-table-column>
        <el-table-column label="点赞" width="70" prop="likes" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small" effect="light">
              {{ row.status === 1 ? '正常' : '已删除' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="170"><template #default="{ row }">{{ formatDate(row.createdAt) }}</template></el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="showEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 1" size="small" type="danger" link @click="deleteComment(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-footer">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
          :total="pagination.total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
          @current-change="fetchData" @size-change="fetchData" />
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑评论" width="520px" @close="editFormRef?.resetFields()">
      <el-form v-if="editForm" ref="editFormRef" :model="editForm" label-width="80px">
        <el-form-item label="内容"><el-input v-model="editForm.content" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">已删除</el-radio>
          </el-radio-group>
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
import { commentApi } from '@/api/comment'
import { formatDate } from '@/utils/format'
import { Search, UserFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false), composing = ref(false), saving = ref(false)
const list = ref([]), editVisible = ref(false)
const editForm = ref(null), editFormRef = ref(null), editingCommentId = ref(null)

const search = reactive({ keyword: '', postId: null })
const pagination = reactive({ page: 1, size: 10, total: 0 })

async function fetchData() {
  loading.value = true
  try {
    const res = await commentApi.getList({ page: pagination.page, size: pagination.size, keyword: search.keyword || undefined, postId: search.postId || undefined })
    list.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (e) { /* handled */ } finally { loading.value = false }
}

function showEdit(row) {
  editingCommentId.value = row.id
  editForm.value = { content: row.content || '', status: row.status ?? 1 }
  editVisible.value = true
}

async function saveEdit() {
  saving.value = true
  try {
    await commentApi.update(editingCommentId.value, editForm.value)
    ElMessage.success('保存成功')
    editVisible.value = false
    fetchData()
  } catch (e) { /* handled */ } finally { saving.value = false }
}

async function deleteComment(row) {
  try {
    await ElMessageBox.confirm('确定要删除该评论吗？', '确认删除', { type: 'warning', confirmButtonText: '确定删除' })
    await commentApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) { /* cancelled */ }
}

onMounted(fetchData)
</script>

<style scoped>
.search-bar { padding: 16px 20px; margin-bottom: 16px; display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.user-cell { display: flex; align-items: center; gap: 8px; font-size: 0.875rem; }
.table-footer { padding: 16px 20px; display: flex; justify-content: flex-end; }
</style>
