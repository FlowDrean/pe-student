<template>
  <div class="message-page">
    <h2 class="page-title">消息管理</h2>

    <div class="admin-card search-bar">
      <el-input v-model="search.keyword" placeholder="搜索消息内容" clearable style="width: 240px" :prefix-icon="Search"
        @clear="fetchData" @compositionstart="composing = true" @compositionend="composing = false"
        @keyup.enter="composing || fetchData()" />
      <el-select v-model="search.type" placeholder="类型筛选" clearable style="width: 140px" @change="fetchData">
        <el-option label="文字" :value="1" />
        <el-option label="图片" :value="2" />
        <el-option label="系统消息" :value="3" />
      </el-select>
      <el-button type="primary" @click="fetchData"><el-icon><Search /></el-icon> 搜索</el-button>
    </div>

    <div class="admin-card" style="padding: 0; overflow: hidden;">
      <el-table :data="list" v-loading="loading" stripe style="width: 100%">
        <el-table-column label="ID" width="70" prop="id" />
        <el-table-column label="发送者" width="150">
          <template #default="{ row }">
            <div class="user-cell" v-if="row.sender">
              <el-avatar :size="28" :src="row.sender.avatar"><el-icon><UserFilled /></el-icon></el-avatar>
              <span>{{ row.sender.nickname || row.sender.username }}</span>
            </div>
            <span v-else class="text-muted">用户{{ row.senderId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="接收者" width="150">
          <template #default="{ row }">
            <div class="user-cell" v-if="row.receiver">
              <el-avatar :size="28" :src="row.receiver.avatar"><el-icon><UserFilled /></el-icon></el-avatar>
              <span>{{ row.receiver.nickname || row.receiver.username }}</span>
            </div>
            <span v-else class="text-muted">用户{{ row.receiverId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="内容" min-width="280" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.type === 2" class="text-muted">[图片]</span>
            <span v-else>{{ row.content }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag size="small" effect="light" :type="typeTag(row.type)">{{ typeMap[row.type] || '未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="已读" width="70">
          <template #default="{ row }">
            <el-tag size="small" effect="light" :type="row.isRead ? 'success' : 'info'">{{ row.isRead ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发送时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="showDetail(row)">详情</el-button>
            <el-button size="small" type="danger" link @click="deleteMsg(row)">删除</el-button>
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
    <el-dialog v-model="detailVisible" title="消息详情" width="560px">
      <template v-if="currentMsg">
        <el-descriptions :column="2" size="small" border>
          <el-descriptions-item label="消息ID">{{ currentMsg.id }}</el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag size="small" effect="light" :type="typeTag(currentMsg.type)">{{ typeMap[currentMsg.type] }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发送者">{{ currentMsg.sender?.nickname || currentMsg.sender?.username || '用户' + currentMsg.senderId }}</el-descriptions-item>
          <el-descriptions-item label="接收者">{{ currentMsg.receiver?.nickname || currentMsg.receiver?.username || '用户' + currentMsg.receiverId }}</el-descriptions-item>
          <el-descriptions-item label="内容" :span="2">
            <template v-if="currentMsg.type === 2">
              <img v-if="currentMsg.imageUrl" :src="getImageUrl(currentMsg.imageUrl)" style="max-width:300px;max-height:300px;border-radius:8px;" @error="onImgError" />
              <span v-else class="text-muted">[图片]（无图片URL）</span>
            </template>
            <span v-else style="white-space: pre-wrap;">{{ currentMsg.content }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="已读">{{ currentMsg.isRead ? '是' : '否' }}</el-descriptions-item>
          <el-descriptions-item label="发送时间">{{ formatDate(currentMsg.createdAt) }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { messageApi } from '@/api/message'
import { formatDate } from '@/utils/format'
import { Search, UserFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false), composing = ref(false)
const list = ref([]), detailVisible = ref(false), currentMsg = ref(null)

const search = reactive({ keyword: '', type: null })
const pagination = reactive({ page: 1, size: 10, total: 0 })

const typeMap = { 1: '文字', 2: '图片', 3: '系统消息' }
const typeTag = (t) => ({ 1: '', 2: 'warning', 3: 'info' }[t] || '')

const BACKEND = 'http://localhost:8080'
function getImageUrl(url) { if (!url) return ''; if (url.startsWith('http')) return url; return BACKEND + (url.startsWith('/') ? url : '/' + url) }
function onImgError(e) { e.target.style.display = 'none' }

async function fetchData() {
  loading.value = true
  try {
    const res = await messageApi.getList({
      page: pagination.page,
      size: pagination.size,
      keyword: search.keyword || undefined,
      type: search.type || undefined,
    })
    list.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (e) { /* handled */ } finally { loading.value = false }
}

async function showDetail(row) {
  try {
    const res = await messageApi.getDetail(row.id)
    currentMsg.value = res.data
    detailVisible.value = true
  } catch (e) { /* handled */ }
}

async function deleteMsg(row) {
  try {
    await ElMessageBox.confirm('确定要删除该消息吗？', '确认删除', { type: 'warning', confirmButtonText: '确定删除' })
    await messageApi.delete(row.id)
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
.text-muted { color: var(--text-muted); font-style: italic; }
</style>
