<template>
  <section class="profile-page">
    <header class="page-head">
      <div>
        <h1>我的发单</h1>
        <p>当前账号发布过的任务</p>
      </div>
    </header>

    <article class="panel">
      <el-skeleton :loading="loading" animated :rows="6">
        <template #default>
          <el-empty v-if="!tasks.length" description="还没有发布任何任务" />
          <div v-else class="task-list">
            <article
              v-for="item in tasks"
              :key="item.task.taskId"
              class="order-row"
            >
              <div class="main">
                <h2>{{ item.task.title }}</h2>
                <p>{{ taskCategoryLabels[item.task.category] }} · {{ item.task.location || '校内待定地点' }}</p>
                <small>{{ item.meta }}</small>
              </div>
              <div class="price">{{ formatReward(item.task) }}</div>
              <OrderStatusBadge :status="item.badgeStatus" :label="taskStatusLabels[item.task.status]" :tone-override="item.tone" />
              <el-button @click="router.push(item.detailPath)">查看详情</el-button>
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
import { useRouter } from 'vue-router'

import { getOrders } from '@/api/order'
import { getPublishedTasks } from '@/api/task'
import OrderStatusBadge from '@/components/OrderStatusBadge.vue'
import { useAuthStore } from '@/stores'
import { taskCategoryLabels, taskStatusLabels } from '@/types'
import type { OrderListDTO, OrderStatus, TaskListDTO, TaskStatus } from '@/types'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const tasks = ref<PublishedItem[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 6

interface PublishedItem {
  task: TaskListDTO
  meta: string
  detailPath: string
  badgeStatus: OrderStatus
  tone: 'orange' | 'green' | 'blue' | 'red' | 'purple'
}

const formatReward = (task: TaskListDTO) => {
  if (task.category === 'TEAM_UP') return '组队'
  const reward = Number(task.reward)
  if (Number.isNaN(reward)) return String(task.reward || '无偿')
  if (reward === 0) return '无偿'
  return `¥${reward.toFixed(2)}`
}

const badgeByTaskStatus = (status: TaskStatus): Pick<PublishedItem, 'badgeStatus' | 'tone'> => {
  switch (status) {
    case 'COMPLETED':
      return { badgeStatus: 'COMPLETED', tone: 'blue' }
    case 'CANCELLED':
    case 'OFFLINE':
      return { badgeStatus: 'CANCELLED', tone: 'red' }
    case 'IN_PROGRESS':
      return { badgeStatus: 'CONFIRMED', tone: 'green' }
    default:
      return { badgeStatus: 'PENDING', tone: 'orange' }
  }
}

const resolveOrderByTask = (orders: OrderListDTO[]) => {
  const map = new Map<number, OrderListDTO>()
  orders.forEach((order) => {
    if (!map.has(order.taskId)) {
      map.set(order.taskId, order)
    }
  })
  return map
}

const toPublishedItem = (task: TaskListDTO, order?: OrderListDTO): PublishedItem => {
  const badge = badgeByTaskStatus(task.status)
  const teamMeta = task.category === 'TEAM_UP'
    ? `组队进度：${task.teamCurrentMembers ?? 0}/${task.teamTotalMembers ?? 0}人`
    : '发布需求'
  return {
    task,
    detailPath: order ? `/orders/${order.orderId}` : `/tasks/${task.taskId}`,
    meta: order?.helperName ? `帮手：${order.helperName}` : teamMeta,
    ...badge,
  }
}

const loadTasks = async () => {
  if (!authStore.user) return
  loading.value = true
  try {
    const [taskResult, orderResult] = await Promise.all([
      getPublishedTasks({
        userId: authStore.user.userId,
        page: page.value,
        size: pageSize,
      }),
      getOrders({
        userId: authStore.user.userId,
        role: 'poster',
        page: 1,
        size: 50,
      }),
    ])
    const orderByTask = resolveOrderByTask(orderResult.records)
    tasks.value = taskResult.records.map((task) => toPublishedItem(task, orderByTask.get(task.taskId)))
    total.value = taskResult.total
  } finally {
    loading.value = false
  }
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
  grid-template-columns: minmax(0, 1fr) 120px 110px 100px;
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
