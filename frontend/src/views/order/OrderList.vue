<template>
  <section class="orders-page">
    <article class="toolbar-card">
      <div>
        <h1>订单列表</h1>
        <p>集中查看我发布和我接取的订单，待接单、进行中、已完成状态统一管理。</p>
      </div>
      <el-button :loading="loading" @click="loadItems">刷新</el-button>
    </article>

    <article class="list-card">
      <el-tabs v-model="activeTab" @tab-change="reload">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="我发布的" name="published" />
        <el-tab-pane label="我接取的" name="accepted" />
      </el-tabs>

      <el-skeleton :loading="loading" animated :rows="6">
        <template #default>
          <el-empty v-if="!visibleItems.length" description="暂无记录" />
          <div v-else class="order-list">
            <article v-for="item in visibleItems" :key="item.key" class="order-row">
              <div class="main">
                <h2>{{ item.title }}</h2>
                <p>{{ taskCategoryLabels[item.category] }} · {{ item.location || '校内待定地点' }}</p>
                <small>{{ item.meta }}</small>
              </div>
              <div class="price">¥{{ Number(item.reward).toFixed(2) }}</div>
              <span :class="['status-badge', item.tone]">{{ item.statusText }}</span>
              <el-button @click="router.push(item.detailPath)">{{ item.actionText }}</el-button>
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
          @current-change="handlePageChange"
        />
      </div>
    </article>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { getOrders } from '@/api/order'
import { getPublishedTasks } from '@/api/task'
import { useAuthStore } from '@/stores'
import { orderStatusLabels, taskCategoryLabels, taskStatusLabels } from '@/types'
import type { EntityId, OrderListDTO, TaskCategory, TaskListDTO } from '@/types'

type TabName = 'all' | 'published' | 'accepted'
type BadgeTone = 'orange' | 'green' | 'blue' | 'red' | 'gray'

interface ListItem {
  key: string
  title: string
  category: TaskCategory
  location?: string | null
  reward: number | string
  statusText: string
  tone: BadgeTone
  meta: string
  createdAt: string
  detailPath: string
  actionText: string
}

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const activeTab = ref<TabName>('all')
const page = ref(1)
const pageSize = 8
const total = ref(0)
const publishedTasks = ref<TaskListDTO[]>([])
const acceptedOrders = ref<OrderListDTO[]>([])
const posterOrders = ref<OrderListDTO[]>([])

const posterOrderByTaskId = computed(() => {
  const map = new Map<EntityId, OrderListDTO>()
  posterOrders.value.forEach((order) => map.set(order.taskId, order))
  return map
})

const visibleItems = computed(() => {
  if (activeTab.value === 'accepted') {
    return acceptedOrders.value.map(orderToItem)
  }

  const taskItems = publishedTasks.value.map(taskToItem)
  if (activeTab.value === 'published') {
    return taskItems
  }

  return [...taskItems, ...acceptedOrders.value.map(orderToItem)]
    .sort((left, right) => new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime())
    .slice((page.value - 1) * pageSize, page.value * pageSize)
})

const taskTone = (status: TaskListDTO['status']): BadgeTone => {
  if (status === 'OPEN') return 'orange'
  if (status === 'IN_PROGRESS') return 'green'
  if (status === 'COMPLETED') return 'blue'
  if (status === 'CANCELLED' || status === 'OFFLINE') return 'red'
  return 'gray'
}

const orderTone = (status: OrderListDTO['status']): BadgeTone => {
  if (status === 'PENDING') return 'orange'
  if (status === 'CONFIRMED') return 'green'
  if (status === 'COMPLETED') return 'blue'
  if (status === 'CANCELLED') return 'red'
  return 'gray'
}

const taskToItem = (task: TaskListDTO): ListItem => {
  const relatedOrder = posterOrderByTaskId.value.get(task.taskId)
  return {
    key: `published-${task.taskId}`,
    title: task.title,
    category: task.category,
    location: task.location,
    reward: task.reward,
    statusText: task.status === 'OPEN' ? '待接单' : taskStatusLabels[task.status],
    tone: taskTone(task.status),
    meta: relatedOrder
      ? `订单号 #${relatedOrder.orderId} · 帮手：${relatedOrder.helperName}`
      : `发布者：${task.publisherName} · 暂未接单`,
    createdAt: task.createdAt,
    detailPath: relatedOrder ? `/orders/${relatedOrder.orderId}` : `/tasks/${task.taskId}`,
    actionText: '查看详情',
  }
}

const orderToItem = (order: OrderListDTO): ListItem => ({
  key: `accepted-${order.orderId}`,
  title: order.taskTitle,
  category: order.taskCategory,
  location: order.taskLocation,
  reward: order.reward,
  statusText: orderStatusLabels[order.status],
  tone: orderTone(order.status),
  meta: `发布者：${order.posterName} · 帮手：${order.helperName}`,
  createdAt: order.createdAt,
  detailPath: `/orders/${order.orderId}`,
  actionText: '查看详情',
})

const loadPublishedTasks = async (paged: boolean) => {
  if (!authStore.user) return { records: [], total: 0 }
  return getPublishedTasks({
    userId: authStore.user.userId,
    page: paged ? page.value : 1,
    size: paged ? pageSize : 50,
  })
}

const loadAcceptedOrders = async (paged: boolean) => {
  if (!authStore.user) return { records: [], total: 0 }
  return getOrders({
    userId: authStore.user.userId,
    role: 'helper',
    page: paged ? page.value : 1,
    size: paged ? pageSize : 50,
  })
}

const loadPosterOrders = async () => {
  if (!authStore.user) return
  const result = await getOrders({
    userId: authStore.user.userId,
    role: 'poster',
    page: 1,
    size: 50,
  })
  posterOrders.value = result.records
}

const loadItems = async () => {
  if (!authStore.user) return
  loading.value = true
  try {
    await loadPosterOrders()

    if (activeTab.value === 'published') {
      const result = await loadPublishedTasks(true)
      publishedTasks.value = result.records
      acceptedOrders.value = []
      total.value = result.total
      return
    }

    if (activeTab.value === 'accepted') {
      const result = await loadAcceptedOrders(true)
      acceptedOrders.value = result.records
      publishedTasks.value = []
      total.value = result.total
      return
    }

    const [tasksResult, ordersResult] = await Promise.all([
      loadPublishedTasks(false),
      loadAcceptedOrders(false),
    ])
    publishedTasks.value = tasksResult.records
    acceptedOrders.value = ordersResult.records
    total.value = tasksResult.records.length + ordersResult.records.length
  } finally {
    loading.value = false
  }
}

const reload = () => {
  page.value = 1
  loadItems()
}

const handlePageChange = () => {
  if (activeTab.value === 'all') {
    return
  }
  loadItems()
}

onMounted(loadItems)
</script>

<style scoped>
.orders-page {
  display: grid;
  gap: 18px;
}

.toolbar-card,
.list-card {
  padding: 22px;
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.07);
}

.toolbar-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.toolbar-card h1,
.toolbar-card p {
  margin: 0;
}

.toolbar-card h1 {
  color: #111827;
  font-size: 26px;
}

.toolbar-card p {
  margin-top: 8px;
  color: #64748b;
}

.order-list {
  display: grid;
  gap: 14px;
}

.order-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 120px 110px 100px;
  gap: 16px;
  align-items: center;
  padding: 18px;
  border: 1px solid #edf2f7;
  border-radius: 8px;
}

.main h2,
.main p,
.main small {
  margin: 0;
}

.main h2 {
  color: #111827;
  font-size: 18px;
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

.status-badge {
  display: inline-flex;
  min-height: 30px;
  align-items: center;
  justify-content: center;
  padding: 0 12px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 700;
}

.status-badge.orange {
  background: #fff4e5;
  color: #b45309;
}

.status-badge.green {
  background: #eaf8ef;
  color: #15803d;
}

.status-badge.blue {
  background: #eaf1ff;
  color: #1d4ed8;
}

.status-badge.red {
  background: #fff1f2;
  color: #dc2626;
}

.status-badge.gray {
  background: #f1f5f9;
  color: #475569;
}

.pager {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 18px;
  color: #64748b;
}

@media (max-width: 900px) {
  .toolbar-card,
  .order-row,
  .pager {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
