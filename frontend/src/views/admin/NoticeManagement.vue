<template>
  <section class="admin-page">
    <article class="toolbar-card">
      <div>
        <h2>公告管理</h2>
        <p>公告会出现在消息中心右侧和首页相关入口中。</p>
      </div>
      <el-button type="primary" @click="openCreate">发布公告</el-button>
    </article>

    <article class="table-card">
      <el-table v-loading="loading" :data="notices" stripe>
        <el-table-column prop="noticeId" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="content" label="内容" min-width="280" show-overflow-tooltip />
        <el-table-column label="发布者" width="150">
          <template #default="{ row }">{{ row.publisherName }} (#{{ row.publisherId }})</template>
        </el-table-column>
        <el-table-column label="更新时间" width="190">
          <template #default="{ row }">{{ formatTime(row.updatedAt || row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="removeNotice(row.noticeId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <span>共 {{ total }} 条</span>
        <el-pagination
          v-model:current-page="page"
          layout="prev, pager, next"
          :page-size="pageSize"
          :total="total"
          @current-change="loadNotices"
        />
      </div>
    </article>

    <el-dialog v-model="dialogVisible" :title="editingNotice ? '编辑公告' : '发布公告'" width="560px">
      <el-form label-position="top">
        <el-form-item label="标题">
          <el-input v-model="form.title" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="6" maxlength="1000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="saveNotice">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { createNotice, deleteNotice, getAdminNotices, updateNotice } from '@/api/admin'
import type { NoticeDTO, NoticeCreateRequest } from '@/types'

const loading = ref(false)
const submitting = ref(false)
const notices = ref<NoticeDTO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 10
const dialogVisible = ref(false)
const editingNotice = ref<NoticeDTO | null>(null)
const form = reactive<NoticeCreateRequest>({
  title: '',
  content: '',
})

const formatTime = (value: string) => new Date(value).toLocaleString('zh-CN', { hour12: false })

const loadNotices = async () => {
  loading.value = true
  try {
    const result = await getAdminNotices({ page: page.value, size: pageSize })
    notices.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.title = ''
  form.content = ''
}

const openCreate = () => {
  editingNotice.value = null
  resetForm()
  dialogVisible.value = true
}

const openEdit = (notice: NoticeDTO) => {
  editingNotice.value = notice
  form.title = notice.title
  form.content = notice.content
  dialogVisible.value = true
}

const saveNotice = async () => {
  if (!form.title.trim() || !form.content.trim()) {
    ElMessage.warning('请填写标题和内容')
    return
  }

  submitting.value = true
  try {
    const payload = { title: form.title.trim(), content: form.content.trim() }
    if (editingNotice.value) {
      await updateNotice(editingNotice.value.noticeId, payload)
      ElMessage.success('公告已更新')
    } else {
      await createNotice(payload)
      ElMessage.success('公告已发布')
    }
    dialogVisible.value = false
    await loadNotices()
  } finally {
    submitting.value = false
  }
}

const removeNotice = async (noticeId: number) => {
  await ElMessageBox.confirm('确认删除这条公告吗？', '删除公告', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
  })
  await deleteNotice(noticeId)
  ElMessage.success('公告已删除')
  await loadNotices()
}

onMounted(loadNotices)
</script>

<style scoped>
.admin-page {
  display: grid;
  gap: 18px;
}

.toolbar-card,
.table-card {
  padding: 20px;
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 26px rgba(15, 23, 42, 0.06);
}

.toolbar-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.toolbar-card h2,
.toolbar-card p {
  margin: 0;
}

.toolbar-card h2 {
  color: #111827;
  font-size: 20px;
}

.toolbar-card p {
  margin-top: 6px;
  color: #64748b;
}

.pager {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 18px;
  color: #64748b;
}

@media (max-width: 760px) {
  .toolbar-card,
  .pager {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
