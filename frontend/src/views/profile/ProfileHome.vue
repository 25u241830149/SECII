<template>
  <section class="profile-home">
    <section class="hero-card">
      <div class="identity">
        <el-avatar :size="92" :src="userAvatar">
          {{ avatarFallback }}
        </el-avatar>
        <div>
          <div class="name-line">
            <h1>{{ displayName }}</h1>
            <span>{{ creditLevel }}</span>
          </div>
          <p>学号：{{ authStore.user?.studentId || '-' }} <b></b> {{ authStore.user?.role || 'USER' }}</p>
          <p class="bio">{{ authStore.user?.department || userStore.home?.nickname || '校园互助用户' }}</p>
          <div class="meta-line">
            <span>{{ authStore.user?.department || '学院未填写' }}</span>
            <span>CampusHub</span>
          </div>
        </div>
      </div>

      <div class="credit-panel">
        <div>
          <span>信用分</span>
          <strong>{{ creditScore }}</strong>
        </div>
        <div class="medal">★</div>
        <div>
          <span>信用等级</span>
          <strong>{{ creditLevel }}</strong>
        </div>
      </div>
    </section>

    <section class="metric-strip">
      <article v-for="item in topStats" :key="item.label">
        <span :class="['metric-icon', item.tone]">
          <el-icon><component :is="item.icon" /></el-icon>
        </span>
        <div>
          <small>{{ item.label }}</small>
          <strong>{{ item.value }}</strong>
        </div>
      </article>
    </section>

    <section class="profile-card favorites-card">
      <header>
        <h2><el-icon><Star /></el-icon> 我的收藏</h2>
        <RouterLink to="/profile/favorites">更多</RouterLink>
      </header>
      <el-empty v-if="!favoritePreview.length" description="暂无收藏任务" />
      <article v-for="task in favoritePreview" v-else :key="task.taskId" class="favorite-row">
        <div class="task-thumb">{{ task.publisherName.slice(0, 1) }}</div>
        <div>
          <h3>{{ task.title }}</h3>
          <p>
            <span>{{ profileTaskCategoryLabels[task.category] }}</span>
            {{ task.location || '校内待定地点' }}
          </p>
        </div>
        <time>{{ formatDate(task.createdAt) }}</time>
        <button type="button" aria-label="查看任务" @click="router.push(`/tasks/${task.taskId}`)">→</button>
      </article>
    </section>

    <div class="profile-grid">
      <section class="profile-card compact-card">
        <header>
          <h2><el-icon><Document /></el-icon> 我的发单</h2>
          <RouterLink to="/profile/published">更多</RouterLink>
        </header>
        <ul>
          <li><span class="dot orange"></span>进行中<strong>{{ publishedStats.inProgress }}</strong></li>
          <li><span class="dot green"></span>已完成<strong>{{ publishedStats.completed }}</strong></li>
          <li><span class="dot red"></span>已下线/取消<strong>{{ publishedStats.offline }}</strong></li>
        </ul>
      </section>

      <section class="profile-card compact-card">
        <header>
          <h2><el-icon><Memo /></el-icon> 我的接单</h2>
          <RouterLink to="/profile/orders">更多</RouterLink>
        </header>
        <ul>
          <li><span class="dot orange"></span>进行中<strong>{{ helperOrderStats.inProgress }}</strong></li>
          <li><span class="dot green"></span>已完成<strong>{{ helperOrderStats.completed }}</strong></li>
          <li><span class="dot red"></span>已取消<strong>{{ helperOrderStats.cancelled }}</strong></li>
        </ul>
      </section>

      <section class="profile-card credit-card">
        <header>
          <h2><el-icon><Medal /></el-icon> 信用分与等级</h2>
          <RouterLink to="/profile/credit">更多</RouterLink>
        </header>
        <div class="credit-mini">
          <strong>{{ creditScore }}</strong>
          <div class="medal small">★</div>
          <div>
            <p>完成率<span>{{ completionRate }}%</span></p>
            <el-progress :percentage="completionRate" :show-text="false" color="#35b779" />
            <p>取消率<span>{{ cancelRate }}%</span></p>
            <el-progress :percentage="cancelRate" :show-text="false" color="#f05252" />
          </div>
        </div>
      </section>

      <section class="profile-card compact-card">
        <header>
          <h2><el-icon><Warning /></el-icon> 我的订单</h2>
          <RouterLink to="/orders">更多</RouterLink>
        </header>
        <ul>
          <li><span class="dot orange"></span>进行中<strong>{{ allOrderStats.inProgress }}</strong></li>
          <li><span class="dot green"></span>已完成<strong>{{ allOrderStats.completed }}</strong></li>
          <li><span class="dot red"></span>已取消<strong>{{ allOrderStats.cancelled }}</strong></li>
        </ul>
      </section>

    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { Check, Document, Medal, Memo, Position, Star, Warning } from '@element-plus/icons-vue'

import { getOrders } from '@/api/order'
import { getFavoriteTasks, getPublishedTasks } from '@/api/task'
import { useAuthStore, useUserStore } from '@/stores'
import { resolveAssetUrl } from '@/utils/asset'
import type { EntityId, OrderStatus, TaskListDTO, TaskStatusFilter } from '@/types'
import { profileTaskCategoryLabels } from './profileLabels'

const router = useRouter()
const authStore = useAuthStore()
const userStore = useUserStore()

const favoritePreview = ref<TaskListDTO[]>([])
const publishedTotal = ref(0)
const helperOrderTotal = ref(0)
const allOrderTotal = ref(0)
const publishedStats = ref({
  inProgress: 0,
  completed: 0,
  offline: 0,
})
const helperOrderStats = ref({
  inProgress: 0,
  completed: 0,
  cancelled: 0,
})

const displayName = computed(() => userStore.home?.nickname || authStore.user?.nickname || '用户')
const userAvatar = computed(() => resolveAssetUrl(userStore.home?.avatarUrl || authStore.user?.avatarUrl || ''))
const avatarFallback = computed(() => displayName.value.slice(0, 1).toUpperCase())
const creditScore = computed(() => userStore.home?.creditScore ?? authStore.user?.creditScore ?? 100)
const creditLevel = computed(() => userStore.home?.creditLevel || 'Lv.3')

const allOrderStats = computed(() => ({
  inProgress: publishedStats.value.inProgress + helperOrderStats.value.inProgress,
  completed: publishedStats.value.completed + helperOrderStats.value.completed,
  cancelled: publishedStats.value.offline + helperOrderStats.value.cancelled,
}))

const completionRate = computed(() => {
  const total = allOrderStats.value.inProgress + allOrderStats.value.completed + allOrderStats.value.cancelled
  return total ? Math.round((allOrderStats.value.completed / total) * 100) : 0
})

const cancelRate = computed(() => {
  const total = allOrderStats.value.inProgress + allOrderStats.value.completed + allOrderStats.value.cancelled
  return total ? Math.round((allOrderStats.value.cancelled / total) * 100) : 0
})

const topStats = computed(() => [
  { label: '发布任务数', value: publishedTotal.value, icon: Position, tone: 'blue' },
  { label: '完成接单数', value: helperOrderStats.value.completed, icon: Check, tone: 'green' },
  { label: '我的订单数', value: allOrderTotal.value, icon: Memo, tone: 'orange' },
])

const formatDate = (value: string) => {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 16)
}

const getPublishedTotal = async (userId: EntityId, status?: Exclude<TaskStatusFilter, 'ALL'>) => {
  const result = await getPublishedTasks({
    userId,
    ...(status ? { status } : {}),
    page: 1,
    size: 1,
  })
  return result.total
}

const getOrderTotal = async (userId: EntityId, role: 'helper', status?: OrderStatus) => {
  const result = await getOrders({
    userId,
    role,
    ...(status ? { status } : {}),
    page: 1,
    size: 1,
  })
  return result.total
}

const loadProfileDashboard = async (userId: EntityId) => {
  const [
    favorites,
    publishedAll,
    publishedOpen,
    publishedPending,
    publishedInProgress,
    publishedCompleted,
    publishedCancelled,
    helperAll,
    helperPending,
    helperConfirmed,
    helperCompleted,
    helperCancelled,
  ] = await Promise.all([
    getFavoriteTasks({ userId, page: 1, size: 3 }),
    getPublishedTotal(userId),
    getPublishedTotal(userId, 'OPEN'),
    getPublishedTotal(userId, 'PENDING_CONFIRM'),
    getPublishedTotal(userId, 'IN_PROGRESS'),
    getPublishedTotal(userId, 'COMPLETED'),
    getPublishedTotal(userId, 'CANCELLED'),
    getOrderTotal(userId, 'helper'),
    getOrderTotal(userId, 'helper', 'PENDING'),
    getOrderTotal(userId, 'helper', 'CONFIRMED'),
    getOrderTotal(userId, 'helper', 'COMPLETED'),
    getOrderTotal(userId, 'helper', 'CANCELLED'),
  ])

  favoritePreview.value = favorites.records
  publishedTotal.value = publishedAll
  helperOrderTotal.value = helperAll
  allOrderTotal.value = publishedAll + helperAll
  publishedStats.value = {
    inProgress: publishedOpen + publishedPending + publishedInProgress,
    completed: publishedCompleted,
    offline: publishedCancelled,
  }
  helperOrderStats.value = {
    inProgress: helperPending + helperConfirmed,
    completed: helperCompleted,
    cancelled: helperCancelled,
  }
}

onMounted(async () => {
  const userId = authStore.user?.userId
  if (!userId) return

  await Promise.all([
    userStore.fetchHome(userId),
    loadProfileDashboard(userId),
  ])
})
</script>

<style scoped>
.profile-home {
  display: grid;
  gap: 18px;
}

.hero-card,
.metric-strip,
.profile-card {
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.06);
}

.hero-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: 28px;
  align-items: center;
  padding: 36px;
  background:
    linear-gradient(120deg, rgba(239, 246, 255, 0.96), rgba(244, 240, 255, 0.92)),
    #fff;
}

.identity {
  display: flex;
  gap: 24px;
  align-items: center;
}

.name-line {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.name-line h1 {
  margin: 0;
  color: #111827;
  font-size: 30px;
}

.name-line span,
.favorite-row p span {
  padding: 4px 8px;
  border-radius: 6px;
  background: #dbeafe;
  color: #2563eb;
  font-size: 12px;
  font-weight: 800;
  white-space: nowrap;
}

.identity p {
  margin: 10px 0 0;
  color: #667085;
}

.identity p b {
  display: inline-block;
  width: 1px;
  height: 12px;
  margin: 0 10px;
  background: #cbd5e1;
}

.bio {
  color: #475569 !important;
}

.meta-line {
  display: flex;
  gap: 24px;
  margin-top: 22px;
  color: #64748b;
}

.credit-panel {
  display: grid;
  grid-template-columns: minmax(76px, 0.9fr) 104px minmax(140px, 1.25fr);
  align-items: center;
  padding: 28px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.54);
  box-shadow: inset 0 0 0 1px rgba(226, 232, 240, 0.6);
  text-align: center;
}

.credit-panel > div:last-child {
  border-left: 1px solid #dbe3f0;
}

.credit-panel span {
  color: #334155;
  font-weight: 700;
}

.credit-panel strong {
  display: block;
  margin-top: 8px;
  color: #4169e1;
  font-size: 42px;
  line-height: 1;
}

.credit-panel > div:last-child strong {
  font-size: 30px;
  white-space: nowrap;
}

.credit-panel small {
  display: block;
  margin-top: 8px;
  color: #475569;
  font-weight: 700;
}

.medal {
  display: grid;
  width: 92px;
  height: 92px;
  box-sizing: border-box;
  place-items: center;
  margin: 0 auto;
  border: 9px solid #bcd3ff;
  border-radius: 50%;
  background: linear-gradient(135deg, #82a9ff, #4f77de);
  color: #fff;
  font-size: 40px;
  box-shadow: 0 12px 24px rgba(79, 119, 222, 0.28);
}

.metric-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  padding: 24px 30px;
}

.metric-strip article {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 22px;
}

.metric-strip article + article {
  border-left: 1px solid #e5ebf4;
}

.metric-icon {
  display: grid;
  width: 54px;
  height: 54px;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  font-size: 28px;
}

.metric-icon.blue {
  background: #4169e1;
}

.metric-icon.green {
  background: #3dbb78;
}

.metric-icon.orange {
  background: #f59e0b;
}

.metric-strip small,
.metric-strip strong {
  display: block;
}

.metric-strip small {
  color: #475569;
  font-weight: 700;
}

.metric-strip strong {
  margin-top: 8px;
  color: #111827;
  font-size: 26px;
}

.profile-card {
  padding: 24px 28px;
}

.profile-card header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.profile-card h2 {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0;
  color: #111827;
  font-size: 18px;
}

.profile-card header a {
  color: #64748b;
  font-weight: 700;
  text-decoration: none;
}

.favorite-row {
  display: grid;
  grid-template-columns: 76px minmax(0, 1fr) 190px 42px;
  gap: 18px;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #eef2f7;
}

.favorite-row:last-child {
  border-bottom: 0;
}

.task-thumb {
  display: grid;
  width: 68px;
  height: 68px;
  place-items: center;
  border-radius: 8px;
  background: #f1f5f9;
  color: #334155;
  font-size: 28px;
  font-weight: 800;
}

.favorite-row h3 {
  margin: 0 0 10px;
  color: #111827;
  font-size: 17px;
}

.favorite-row p {
  display: flex;
  gap: 14px;
  align-items: center;
  margin: 0;
  color: #64748b;
}

.favorite-row time {
  color: #94a3b8;
}

.favorite-row button {
  width: 38px;
  height: 38px;
  border: 1px solid #dbe3ef;
  border-radius: 8px;
  background: #fff;
  color: #64748b;
  cursor: pointer;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.compact-card ul {
  display: grid;
  gap: 16px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.compact-card li {
  display: grid;
  grid-template-columns: 12px 1fr 40px;
  gap: 10px;
  align-items: center;
  color: #475569;
}

.dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
}

.dot.orange {
  background: #f59e0b;
}

.dot.green {
  background: #35b779;
}

.dot.red {
  background: #f05252;
}

.credit-mini {
  display: grid;
  grid-template-columns: 78px 110px minmax(0, 1fr);
  gap: 22px;
  align-items: center;
}

.credit-mini > strong {
  color: #4169e1;
  font-size: 44px;
}

.medal.small {
  width: 72px;
  height: 72px;
  font-size: 30px;
}

.credit-mini p {
  display: flex;
  justify-content: space-between;
  margin: 8px 0 4px;
  color: #475569;
}

@media (max-width: 980px) {
  .hero-card,
  .profile-grid {
    grid-template-columns: 1fr;
  }

  .metric-strip {
    grid-template-columns: 1fr;
    gap: 18px;
  }

  .metric-strip article + article {
    border-left: 0;
    border-top: 1px solid #e5ebf4;
    padding-top: 18px;
  }

  .favorite-row {
    grid-template-columns: 64px minmax(0, 1fr);
  }

  .favorite-row time,
  .favorite-row button {
    grid-column: 2;
  }
}
</style>
