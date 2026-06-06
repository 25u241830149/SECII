<template>
  <section class="orders-page">
    <article class="toolbar-card">
      <div>
        <h1>我的订单</h1>
        <p>按状态查看已发布和已接取的需求，处理确认、履约、评价与取消记录。</p>
      </div>
      <el-button :loading="loading" @click="loadItems">刷新</el-button>
    </article>

    <article class="list-card">
      <div class="role-tabs">
        <button
          v-for="tab in roleTabs"
          :key="tab.value"
          type="button"
          :class="{ active: activeRole === tab.value }"
          @click="switchRole(tab.value)"
        >
          {{ tab.label }}
        </button>
      </div>

      <div class="status-tabs">
        <button
          v-for="tab in statusTabs"
          :key="tab.value"
          type="button"
          :class="{ active: activeStatus === tab.value }"
          @click="switchStatus(tab.value)"
        >
          {{ tab.label }}
        </button>
      </div>

      <div class="orders-content">
        <div v-if="!items.length" class="order-empty">
          <el-empty description="暂无记录" />
        </div>
        <div v-else class="order-list">
          <article v-for="item in items" :key="item.key" class="order-row">
            <div class="main">
              <h2>{{ item.title }}</h2>
              <p>{{ taskCategoryLabels[item.category] }} · {{ item.location || '校内待定地点' }}</p>
              <small>{{ item.meta }}</small>
            </div>
            <div class="price">{{ formatReward(item) }}</div>
            <span :class="['status-badge', item.tone]">{{ item.statusText }}</span>
            <el-button @click="goDetail(item)">{{ item.actionText }}</el-button>
          </article>
        </div>
      </div>

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
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { getOrders } from '@/api/order'
import { getPublishedTasks } from '@/api/task'
import { useAuthStore } from '@/stores'
import { orderStatusLabels, taskCategoryLabels, taskStatusLabels } from '@/types'
import type { OrderListDTO, OrderStatus, TaskCategory, TaskListDTO, TaskStatus } from '@/types'

type RoleFilter = 'published' | 'accepted'
type StatusFilter = 'ALL' | 'OPEN' | 'PENDING' | 'CONFIRMED' | 'WAITING_REVIEW' | 'COMPLETED' | 'CANCELLED'
type TaskQueryStatus = Exclude<TaskStatus, 'OFFLINE'>
type BadgeTone = 'orange' | 'green' | 'blue' | 'red' | 'gray' | 'purple'

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
const route = useRoute()
const authStore = useAuthStore()

const loading = ref(false)
const activeRole = ref<RoleFilter>('published')
const activeStatus = ref<StatusFilter>('ALL')
const page = ref(1)
const pageSize = 10
const total = ref(0)
const items = ref<ListItem[]>([])

const roleTabs: Array<{ label: string; value: RoleFilter }> = [
  { label: '我的发布', value: 'published' },
  { label: '我的接取', value: 'accepted' },
]

const allStatusTabs: Array<{ label: string; value: StatusFilter }> = [
  { label: '全部', value: 'ALL' },
  { label: '待接单', value: 'OPEN' },
  { label: '待确认', value: 'PENDING' },
  { label: '进行中', value: 'CONFIRMED' },
  { label: '待评价', value: 'WAITING_REVIEW' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' },
]

const statusTabs = computed(() => (
  activeRole.value === 'published'
    ? allStatusTabs
    : allStatusTabs.filter((tab) => tab.value !== 'OPEN')
))

const taskStatusByFilter: Partial<Record<StatusFilter, TaskQueryStatus>> = {
  OPEN: 'OPEN',
  PENDING: 'PENDING_CONFIRM',
  CONFIRMED: 'IN_PROGRESS',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED',
}

const orderStatusByFilter: Partial<Record<StatusFilter, OrderStatus>> = {
  PENDING: 'PENDING',
  CONFIRMED: 'CONFIRMED',
  WAITING_REVIEW: 'WAITING_REVIEW',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED',
}

const taskTone = (status: TaskStatus): BadgeTone => {
  if (status === 'OPEN') return 'orange'
  if (status === 'PENDING_CONFIRM') return 'purple'
  if (status === 'IN_PROGRESS') return 'green'
  if (status === 'COMPLETED') return 'blue'
  if (status === 'CANCELLED' || status === 'OFFLINE') return 'red'
  return 'gray'
}

const orderTone = (status: OrderStatus, waitingReview = false): BadgeTone => {
  if (waitingReview) return 'purple'
  if (status === 'PENDING') return 'orange'
  if (status === 'CONFIRMED') return 'green'
  if (status === 'COMPLETED') return 'blue'
  if (status === 'CANCELLED') return 'red'
  return 'gray'
}

const taskToItem = (task: TaskListDTO, orderId?: number): ListItem => ({
  key: `task-${task.taskId}`,
  title: task.title,
  category: task.category,
  location: task.location,
  reward: task.reward,
  statusText: taskStatusLabels[task.status],
  tone: taskTone(task.status),
  meta: task.category === 'TEAM_UP'
    ? `组队进度：${task.teamCurrentMembers ?? 0}/${task.teamTotalMembers ?? 0}人`
    : `发布者：${task.publisherName}`,
  createdAt: task.createdAt,
  detailPath: orderId ? `/orders/${orderId}` : `/tasks/${task.taskId}`,
  actionText: '查看详情',
})

const orderToItem = (order: OrderListDTO, waitingReview = false): ListItem => ({
  key: `order-${order.orderId}`,
  title: order.taskTitle,
  category: order.taskCategory,
  location: order.taskLocation,
  reward: order.reward,
  statusText: waitingReview ? '待评价' : orderStatusLabels[order.status],
  tone: orderTone(order.status, waitingReview),
  meta: order.taskCategory === 'TEAM_UP'
    ? `发起人：${order.posterName} · 组队进度：${order.teamCurrentMembers ?? 0}/${order.teamTotalMembers ?? 0}人`
    : `发布者：${order.posterName} · 帮手：${order.helperName}`,
  createdAt: order.createdAt,
  detailPath: `/orders/${order.orderId}`,
  actionText: waitingReview ? '去评价' : '查看详情',
})

const sortItems = (records: ListItem[]) =>
  records.sort((left, right) => new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime())

const formatReward = (item: ListItem) => {
  if (item.category === 'TEAM_UP') return '组队'
  const rewardNumber = Number(item.reward)
  if (Number.isNaN(rewardNumber)) return String(item.reward || '无偿')
  if (rewardNumber === 0) return '无偿'
  return `¥${rewardNumber.toFixed(2)}`
}

const goDetail = (item: ListItem) => {
  router.push({
    path: item.detailPath,
    query: {
      fromRole: activeRole.value,
      fromStatus: activeStatus.value,
      fromPage: String(page.value),
    },
  })
}

const loadTaskItems = async (status?: TaskQueryStatus) => {
  if (!authStore.user) return { records: [] as ListItem[], total: 0 }
  const result = await getPublishedTasks({
    userId: authStore.user.userId,
    ...(status ? { status } : {}),
    page: page.value,
    size: pageSize,
  })
  const shouldLinkOrders = status !== 'OPEN'
  const orderResult = shouldLinkOrders
    ? await getOrders({
        userId: authStore.user.userId,
        role: 'poster',
        page: 1,
        size: 50,
      })
    : null
  const matchingOrderByTask = new Map<number, number>()
  orderResult?.records.forEach((order) => {
    const matchesTaskStatus =
      (order.status === 'PENDING' && result.records.some((task) => task.taskId === order.taskId && task.status === 'PENDING_CONFIRM')) ||
      (order.status === 'CONFIRMED' && result.records.some((task) => task.taskId === order.taskId && task.status === 'IN_PROGRESS')) ||
      (order.status === 'COMPLETED' && result.records.some((task) => task.taskId === order.taskId && task.status === 'COMPLETED')) ||
      (order.status === 'CANCELLED' && result.records.some((task) => task.taskId === order.taskId && task.status === 'CANCELLED'))
    if (matchesTaskStatus && !matchingOrderByTask.has(order.taskId)) {
      matchingOrderByTask.set(order.taskId, order.orderId)
    }
  })
  return {
    records: result.records.map((task) => taskToItem(task, matchingOrderByTask.get(task.taskId))),
    total: result.total,
  }
}

const loadOrderItems = async (role: 'poster' | 'helper', status?: OrderStatus) => {
  if (!authStore.user) return { records: [] as ListItem[], total: 0 }
  const result = await getOrders({
    userId: authStore.user.userId,
    role,
    ...(status ? { status } : {}),
    page: page.value,
    size: pageSize,
  })
  return {
    records: result.records.map((order) => orderToItem(order, status === 'WAITING_REVIEW')),
    total: result.total,
  }
}

const loadPublishedItems = async () => {
  if (activeStatus.value === 'CONFIRMED') {
    return loadTaskItems('IN_PROGRESS')
  }

  if (
    activeStatus.value === 'PENDING' ||
    activeStatus.value === 'WAITING_REVIEW'
  ) {
    return loadOrderItems('poster', orderStatusByFilter[activeStatus.value])
  }

  return loadTaskItems(taskStatusByFilter[activeStatus.value])
}

const loadAcceptedItems = async () => {
  if (activeStatus.value === 'OPEN') {
    return loadOrderItems('helper')
  }

  return loadOrderItems('helper', orderStatusByFilter[activeStatus.value])
}

const loadItems = async () => {
  if (!authStore.user) return
  loading.value = true
  try {
    const result = activeRole.value === 'published'
      ? await loadPublishedItems()
      : await loadAcceptedItems()

    items.value = sortItems(result.records)
    total.value = result.total
  } finally {
    loading.value = false
  }
}

const updateListRoute = (role: RoleFilter, status: StatusFilter, currentPage: number) => {
  router.push({
    path: '/orders',
    query: {
      role,
      status,
      page: String(currentPage),
    },
  })
}

const switchRole = (role: RoleFilter) => {
  updateListRoute(role, 'ALL', 1)
}

const switchStatus = (status: StatusFilter) => {
  updateListRoute(activeRole.value, status, 1)
}

const handlePageChange = (currentPage: number) => {
  updateListRoute(activeRole.value, activeStatus.value, currentPage)
}

watch(
  () => [route.query.role, route.query.status, route.query.page],
  () => {
    const requestedRole = String(route.query.role || '')
    activeRole.value = requestedRole === 'accepted' ? 'accepted' : 'published'

    const requestedStatus = String(route.query.status || '')
    const allowedStatus = allStatusTabs.some((tab) => tab.value === requestedStatus)
      && !(activeRole.value === 'accepted' && requestedStatus === 'OPEN')
    if (allowedStatus) {
      activeStatus.value = requestedStatus as StatusFilter
    } else {
      activeStatus.value = 'ALL'
    }

    const requestedPage = Number(route.query.page)
    if (Number.isInteger(requestedPage) && requestedPage > 0) {
      page.value = requestedPage
    } else {
      page.value = 1
    }

    loadItems()
  },
  { immediate: true },
)
</script>

<style scoped>
.orders-page {
  display: grid;
  height: calc(100vh - 122px);
  grid-template-rows: auto minmax(0, 1fr);
  gap: 18px;
  min-height: 0;
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

.list-card {
  display: flex;
  min-height: 0;
  flex-direction: column;
}

.orders-content {
  display: flex;
  min-height: 0;
  flex: 1;
}

.order-empty {
  display: flex;
  min-height: 0;
  flex: 1;
  align-items: center;
  justify-content: center;
}

.role-tabs,
.status-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.role-tabs {
  margin-bottom: 12px;
  padding: 4px;
  border-radius: 8px;
  background: #f5f8fc;
}

.status-tabs {
  margin-bottom: 16px;
}

.role-tabs button,
.status-tabs button {
  min-height: 34px;
  padding: 0 14px;
  border: 1px solid #dfe6f2;
  border-radius: 8px;
  background: #fff;
  color: #4b5563;
  cursor: pointer;
  font: inherit;
}

.role-tabs button {
  min-width: 120px;
}

.role-tabs button.active,
.status-tabs button.active {
  border-color: #b9d8ff;
  background: #edf5ff;
  color: #1268ed;
  font-weight: 700;
}

.order-list {
  display: grid;
  gap: 14px;
  min-height: 0;
  flex: 1;
  align-content: start;
  overflow-y: auto;
  padding-right: 8px;
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

.status-badge.purple {
  background: #f3efff;
  color: #7c3aed;
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
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 18px;
  color: #64748b;
}

@media (max-width: 900px) {
  .orders-page {
    height: auto;
  }

  .toolbar-card,
  .order-row,
  .pager {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
