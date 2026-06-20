<template>
  <div class="user-page">
    <h2 class="page-title">用户管理</h2>

    <div class="admin-card search-bar">
      <el-input v-model="search.keyword" placeholder="搜索用户名/昵称" clearable style="width: 240px" :prefix-icon="Search"
        @clear="fetchData" @compositionstart="composing = true" @compositionend="composing = false"
        @keyup.enter="composing || fetchData()" />
      <el-select v-model="search.status" placeholder="状态筛选" clearable style="width: 140px" @change="fetchData">
        <el-option label="正常" :value="1" />
        <el-option label="已禁用" :value="0" />
      </el-select>
      <el-select v-model="search.role" placeholder="角色筛选" clearable style="width: 140px" @change="fetchData">
        <el-option label="管理员" value="admin" />
        <el-option label="普通用户" value="user" />
      </el-select>
      <el-button type="primary" @click="fetchData"><el-icon><Search /></el-icon> 搜索</el-button>
    </div>

    <div class="admin-card" style="padding: 0; overflow: hidden;">
      <el-table :data="list" v-loading="loading" stripe style="width: 100%">
        <el-table-column label="用户" min-width="200">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="36" :src="row.avatar"><el-icon><UserFilled /></el-icon></el-avatar>
              <div>
                <div class="user-name">{{ row.nickname || row.username }}</div>
                <div class="user-username">@{{ row.username }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="school" label="学校" width="130" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'admin' ? 'danger' : 'info'" size="small" effect="light">
              {{ row.role === 'admin' ? '管理员' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small" effect="light">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="showDetail(row)">详情</el-button>
            <el-button size="small" link @click="showEdit(row)">编辑</el-button>
            <el-button v-if="row.role !== 'admin'" size="small"
              :type="row.status === 1 ? 'warning' : 'success'" link @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" :type="row.role === 'admin' ? 'warning' : 'success'" link @click="toggleRole(row)">
              {{ row.role === 'admin' ? '取消管理员' : '设为管理员' }}
            </el-button>
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
    <el-dialog v-model="detailVisible" title="用户详情" width="520px">
      <el-descriptions v-if="currentUser" :column="2" border>
        <el-descriptions-item label="ID">{{ currentUser.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ currentUser.nickname }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentUser.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentUser.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学校">{{ currentUser.school || '-' }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ currentUser.major || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentUser.studentId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="简介">{{ currentUser.bio || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentUser.status === 1 ? 'success' : 'danger'" size="small">{{ currentUser.status === 1 ? '正常' : '禁用' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="currentUser.role === 'admin' ? 'danger' : 'info'" size="small">{{ currentUser.role === 'admin' ? '管理员' : '用户' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ formatDate(currentUser.createdAt) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑用户" width="560px" @close="editFormRef?.resetFields()">
      <el-form v-if="editForm" ref="editFormRef" :model="editForm" label-width="80px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="昵称"><el-input v-model="editForm.nickname" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱"><el-input v-model="editForm.email" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号"><el-input v-model="editForm.phone" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-select v-model="editForm.gender" style="width:100%">
                <el-option label="未知" :value="0" />
                <el-option label="男" :value="1" />
                <el-option label="女" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学校"><el-input v-model="editForm.school" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="专业"><el-input v-model="editForm.major" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学号"><el-input v-model="editForm.studentId" /></el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="简介"><el-input v-model="editForm.bio" type="textarea" :rows="2" /></el-form-item>
          </el-col>
        </el-row>
        <el-divider />
        <el-form-item label="重置密码">
          <el-input v-model="newPassword" type="password" placeholder="留空则不修改" show-password style="width:200px" />
          <el-button type="warning" plain size="small" style="margin-left:12px" @click="handleResetPwd" :disabled="!newPassword">
            确认重置
          </el-button>
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
import { userApi } from '@/api/user'
import { formatDate } from '@/utils/format'
import { Search, UserFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const composing = ref(false)
const saving = ref(false)
const list = ref([])
const detailVisible = ref(false)
const currentUser = ref(null)
const editVisible = ref(false)
const editForm = ref(null)
const editFormRef = ref(null)
const newPassword = ref('')
const editingUserId = ref(null)

const search = reactive({ keyword: '', status: null, role: null })
const pagination = reactive({ page: 1, size: 10, total: 0 })

async function fetchData() {
  loading.value = true
  try {
    const res = await userApi.getList({ page: pagination.page, size: pagination.size, keyword: search.keyword || undefined, status: search.status, role: search.role })
    list.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (e) { /* handled */ } finally { loading.value = false }
}

async function showDetail(row) {
  const res = await userApi.getDetail(row.id)
  currentUser.value = res.data
  detailVisible.value = true
}

async function showEdit(row) {
  const res = await userApi.getDetail(row.id)
  const u = res.data
  editingUserId.value = u.id
  editForm.value = {
    nickname: u.nickname || '',
    email: u.email || '',
    phone: u.phone || '',
    gender: u.gender ?? 0,
    school: u.school || '',
    major: u.major || '',
    studentId: u.studentId || '',
    bio: u.bio || '',
  }
  newPassword.value = ''
  editVisible.value = true
}

async function saveEdit() {
  saving.value = true
  try {
    await userApi.update(editingUserId.value, editForm.value)
    ElMessage.success('保存成功')
    editVisible.value = false
    fetchData()
  } catch (e) { /* handled */ } finally { saving.value = false }
}

async function handleResetPwd() {
  if (!newPassword.value) return
  try {
    await ElMessageBox.confirm('确定要重置该用户的密码吗？', '确认操作', { type: 'warning' })
    await userApi.resetPassword(editingUserId.value, newPassword.value)
    ElMessage.success('密码已重置')
    newPassword.value = ''
  } catch (e) { /* cancelled */ }
}

async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await ElMessageBox.confirm(`确定要${newStatus === 0 ? '禁用' : '启用'}该用户吗？`, '确认操作', { type: 'warning' })
    await userApi.updateStatus(row.id, newStatus)
    ElMessage.success('操作成功')
    fetchData()
  } catch (e) { /* cancelled */ }
}

async function toggleRole(row) {
  const newRole = row.role === 'admin' ? 'user' : 'admin'
  try {
    await ElMessageBox.confirm(`确定要${newRole === 'admin' ? '设为' : '取消'}管理员吗？`, '确认操作', { type: 'warning' })
    await userApi.updateRole(row.id, newRole)
    ElMessage.success('操作成功')
    fetchData()
  } catch (e) { /* cancelled */ }
}

onMounted(fetchData)
</script>

<style scoped>
.search-bar { padding: 16px 20px; margin-bottom: 16px; display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.user-cell { display: flex; align-items: center; gap: 10px; }
.user-name { font-weight: 600; color: var(--text-primary); font-size: 0.875rem; }
.user-username { font-size: 0.75rem; color: var(--text-muted); }
.table-footer { padding: 16px 20px; display: flex; justify-content: flex-end; }
</style>
