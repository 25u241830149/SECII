<template>
  <section class="admin-page">
    <article class="toolbar-card">
      <div>
        <h2>任务管理</h2>
        <p>可按 ID 下架违规任务，也可浏览当前任务列表进行快速处理。</p>
      </div>
      <form class="inline-form" @submit.prevent="offlineById">
        <el-input-number v-model="targetTaskId" :min="1" controls-position="right" />
        <el-button type="danger" :loading="submitting" native-type="submit">下架任务</el-button>
      </form>
    </article>

    <article class="table-card">
      <div class="table-tools">
        <el-input v-model="keyword" placeholder="搜索任务标题或描述" clearable @keyup.enter="reload" @clear="reload" />
        <el-button @click="reload">搜索</el-button>
      </div>

      <el-table v-loading="loading" :data="tasks" stripe>
        <el-table-column prop="taskId" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column label="分类" width="120">
          <template #default="{ row }">{{ categoryLabel(row) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">{{ statusLabel(row) }}</template>
        </el-table-column>
        <el-table-column label="发布者" width="150">
          <template #default="{ row }">{{ row.publisherName }} (#{{ row.publisherId }})</template>
        </el-table-column>
        <el-table-column prop="reward" label="报酬" width="100" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="danger"
              plain
              :disabled="row.status === 'OFFLINE'"
              @click="offline(row.taskId)"
            >
              下架
            </el-button>
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
          @current-change="loadTasks"
        />
      </div>
    </article>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { offlineTask } from '@/api/admin'
import { getTasks } from '@/api/task'
import { taskCategoryLabels, taskStatusLabels } from '@/types'
import type { TaskListDTO } from '@/types'

const loading = ref(false)
const submitting = ref(false)
const tasks = ref<TaskListDTO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 10
const keyword = ref('')
const targetTaskId = ref(1)
const categoryLabel = (task: TaskListDTO) => taskCategoryLabels[task.category]
const statusLabel = (task: TaskListDTO) => taskStatusLabels[task.status]

const loadTasks = async () => {
  loading.value = true
  try {
    const result = await getTasks({
      page: page.value,
      size: pageSize,
      keyword: keyword.value.trim() || undefined,
    })
    tasks.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

const reload = () => {
  page.value = 1
  loadTasks()
}

const offline = async (taskId: number) => {
  await ElMessageBox.confirm(`确认下架任务 #${taskId} 吗？`, '下架任务', {
    type: 'warning',
    confirmButtonText: '下架',
    cancelButtonText: '取消',
  })
  await offlineTask(taskId)
  ElMessage.success('任务已下架')
  await loadTasks()
}

const offlineById = async () => {
  submitting.value = true
  try {
    await offline(targetTaskId.value)
  } finally {
    submitting.value = false
  }
}

onMounted(loadTasks)
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

.inline-form,
.table-tools,
.pager {
  display: flex;
  align-items: center;
  gap: 10px;
}

.table-tools {
  margin-bottom: 14px;
}

.table-tools :deep(.el-input) {
  max-width: 360px;
}

.pager {
  justify-content: space-between;
  margin-top: 18px;
  color: #64748b;
}

@media (max-width: 760px) {
  .toolbar-card,
  .inline-form,
  .table-tools,
  .pager {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
