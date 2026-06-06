<template>
  <section class="profile-page">
    <header class="page-head">
      <div>
        <h1>我的收藏</h1>
        <p>当前账号收藏过的任务</p>
      </div>
    </header>

    <article class="panel">
      <el-skeleton :loading="loading" animated :rows="6">
        <template #default>
          <el-empty v-if="!tasks.length" description="还没有收藏任何任务" />
          <div v-else class="task-list">
            <article
              v-for="task in tasks"
              :key="task.taskId"
              class="order-row"
            >
              <div class="main">
                <h2>{{ task.title }}</h2>
                <p>{{ taskCategoryLabels[task.category] }} · {{ task.location || '校内待定地点' }}</p>
                <small>发布者：{{ task.publisherName }}</small>
              </div>
              <div class="price">¥{{ Number(task.reward).toFixed(2) }}</div>
              <span :class="['task-status', statusTone(task.status)]">{{ taskStatusLabels[task.status] }}</span>
              <div class="actions">
                <el-button @click="handleViewDetail(task)">查看详情</el-button>
                <el-button @click="handleFavorite(task)">取消收藏</el-button>
              </div>
            </article>
          </div>
        </template>
      </el-skeleton>

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
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

import { getOrders } from '@/api/order'
import { getFavoriteTasks, unfavoriteTask } from '@/api/task'
import { useAuthStore } from '@/stores'
import { taskCategoryLabels, taskStatusLabels } from '@/types'
import type { TaskListDTO, TaskStatus } from '@/types'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const tasks = ref<TaskListDTO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 6

const loadTasks = async () => {
  if (!authStore.user) return
  loading.value = true
  try {
    const result = await getFavoriteTasks({
      userId: authStore.user.userId,
      page: page.value,
      size: pageSize,
    })
    tasks.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

const handleFavorite = async (task: TaskListDTO) => {
  await unfavoriteTask(task.taskId)
  ElMessage.success('已取消收藏')
  await loadTasks()
}

const handleViewDetail = async (task: TaskListDTO) => {
  if (!authStore.user) {
    router.push(`/tasks/${task.taskId}`)
    return
  }

  const [publishedOrders, acceptedOrders] = await Promise.all([
    getOrders({ userId: authStore.user.userId, role: 'poster', page: 1, size: 50 }),
    getOrders({ userId: authStore.user.userId, role: 'helper', page: 1, size: 50 }),
  ])
  const relatedOrder = [...publishedOrders.records, ...acceptedOrders.records]
    .find((order) => order.taskId === task.taskId)

  router.push(relatedOrder ? `/orders/${relatedOrder.orderId}` : `/tasks/${task.taskId}`)
}

const statusTone = (status: TaskStatus) => {
  if (status === 'OPEN') return 'blue'
  if (status === 'IN_PROGRESS') return 'green'
  if (status === 'COMPLETED') return 'purple'
  if (status === 'OFFLINE') return 'gray'
  return 'orange'
}

onMounted(loadTasks)
</script>

<style scoped>
.profile-page {
  display: grid;
  height: 100%;
  min-height: 0;
  grid-template-rows: auto minmax(0, 1fr);
  gap: 16px;
}

.page-head,
.panel {
  display: flex;
  min-height: 0;
  flex-direction: column;
  padding: 22px;
  border: 1px solid #e7edf7;
  border-radius: 24px;
  background: #fff;
  box-shadow: 0 18px 36px rgba(15, 23, 42, 0.08);
}

.panel :deep(.el-skeleton) {
  flex: 1 1 auto;
  min-height: 0;
  overflow: hidden;
}

.page-head h1,
.page-head p {
  margin: 0;
}

.page-head p {
  margin-top: 8px;
  color: #64748b;
}

.task-list {
  display: grid;
  gap: 14px;
  min-height: 0;
  overflow-y: auto;
  padding-right: 4px;
}

.task-list::-webkit-scrollbar {
  width: 8px;
}

.task-list::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: #cbd5e1;
}

.task-list::-webkit-scrollbar-track {
  background: transparent;
}

.order-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 120px 110px 180px;
  gap: 16px;
  align-items: center;
  padding: 14px 16px;
  border: 1px solid #edf2f7;
  border-radius: 18px;
}

.main h2,
.main p,
.main small {
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.main p,
.main small {
  margin-top: 6px;
  color: #64748b;
}

.price {
  color: #ea580c;
  font-size: 22px;
  font-weight: 700;
  text-align: center;
}

.task-status {
  justify-self: center;
  min-width: 82px;
  padding: 6px 12px;
  border-radius: 999px;
  font-weight: 800;
  text-align: center;
  white-space: nowrap;
}

.task-status.blue {
  background: #eaf1ff;
  color: #1268ed;
}

.task-status.green {
  background: #e9f8ef;
  color: #14935b;
}

.task-status.orange {
  background: #fff7ed;
  color: #f97316;
}

.task-status.purple {
  background: #f1ebff;
  color: #7c3aed;
}

.task-status.gray {
  background: #f1f5f9;
  color: #64748b;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.pager {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 18px;
  color: #64748b;
}
</style>
