<template>
  <section class="home-page">
    <main class="home-main">
      <section class="hero-card">
        <div class="hero-content">
          <h1>让校园互助更高效、更可信、更简单</h1>
          <p>发布需求、发现服务、完成协作，CampusHub 帮你把零散的校园互助连接起来。</p>

          <div class="quick-actions">
            <RouterLink class="quick-card" to="/tasks/publish">
              <span class="quick-icon blue"><el-icon><Promotion /></el-icon></span>
              <span>
                <strong>发布互助需求</strong>
                <small>填写需求信息<br />快速发布</small>
              </span>
            </RouterLink>
            <button class="quick-card" type="button" @click="scrollToTasks">
              <span class="quick-icon purple"><el-icon><Search /></el-icon></span>
              <span>
                <strong>浏览可接需求</strong>
                <small>发现合适的需求<br />提供帮助</small>
              </span>
            </button>
            <RouterLink class="quick-card" to="/orders">
              <span class="quick-icon green"><el-icon><Memo /></el-icon></span>
              <span>
                <strong>查看我的订单</strong>
                <small>管理订单进度<br />查看历史记录</small>
              </span>
            </RouterLink>
          </div>
        </div>
      </section>

      <section ref="taskPanelRef" class="task-panel">
        <header class="task-tabs">
          <button
            v-for="tab in tabs"
            :key="tab.value"
            type="button"
            :class="{ active: sort === tab.value }"
            @click="switchTab(tab.value)"
          >
            {{ tab.label }}
          </button>
        </header>

        <div class="search-row">
          <el-input
            v-model="keyword"
            placeholder="搜索任务标题或描述"
            clearable
            @keyup.enter="applyFilters"
            @clear="applyFilters"
          />
          <el-button type="primary" @click="applyFilters">搜索</el-button>
        </div>

        <div ref="taskScrollRef" class="task-scroll">
          <el-skeleton :loading="feedStore.loading" animated :rows="6">
            <template #default>
              <el-empty v-if="!feedStore.tasks.length" description="暂无匹配需求" />
              <div v-else class="task-list">
                <article
                  v-for="task in feedStore.tasks"
                  :key="task.taskId"
                  class="task-row"
                  :class="categoryMeta[task.category].tone"
                >
                  <div class="task-thumb">
                    <el-icon><component :is="categoryMeta[task.category].icon" /></el-icon>
                  </div>

                  <div class="task-body">
                    <h2>
                      <span class="category-tag">{{ taskCategoryLabels[task.category] }}</span>
                      {{ formatTaskTitle(task) }}
                    </h2>
                    <div class="meta-line">
                      <span class="publisher">
                        <el-avatar :size="24" :src="resolveAssetUrl(task.publisherAvatarUrl || '')">
                          {{ task.publisherName.slice(0, 1) }}
                        </el-avatar>
                        {{ task.publisherName }}
                      </span>
                      <span><el-icon><Medal /></el-icon> 信用分 {{ task.publisherCreditScore ?? '-' }}</span>
                      <span><el-icon><Location /></el-icon> {{ task.location || '校内待定地点' }}</span>
                      <span><el-icon><Clock /></el-icon> {{ formatTime(task.createdAt) }}</span>
                    </div>
                    <p>{{ task.description || '发布者没有补充更多描述。' }}</p>
                  </div>

                  <div class="task-reward">
                    <strong>{{ formatReward(task) }}</strong>
                    <span :class="['status-pill', task.status.toLowerCase()]">{{ taskStatusLabels[task.status] }}</span>
                  </div>

                  <div class="task-actions">
                    <el-button plain @click="goDetail(task)">查看详情</el-button>
                    <el-button
                      type="primary"
                      :disabled="task.status !== 'OPEN'"
                      @click="handleGrab(task)"
                    >
                      {{ task.status === 'OPEN' ? '立即接单' : taskStatusLabels[task.status] }}
                    </el-button>
                  </div>
                </article>
              </div>
            </template>
          </el-skeleton>
        </div>

        <footer class="pager">
          <span>共 {{ feedStore.total }} 条</span>
          <el-pagination
            v-model:current-page="feedStore.page"
            layout="prev, pager, next"
            :page-size="feedStore.size"
            :total="feedStore.total"
            @current-change="handlePageChange"
          />
        </footer>
      </section>
    </main>

    <aside class="right-sidebar">
      <section class="right-card profile-card">
        <div class="profile-top">
          <el-avatar :size="66" :src="currentAvatar || undefined" class="profile-avatar">
            {{ avatarFallback }}
          </el-avatar>
          <div>
            <h2>{{ displayName }} <el-icon><CircleCheckFilled /></el-icon></h2>
            <div class="role-line">
              <span>需求方</span>
              <span>服务方</span>
            </div>
          </div>
          <div class="credit-score">
            <small>信用分</small>
            <strong>{{ creditScore }}</strong>
          </div>
        </div>
        <p class="level-line">等级：<b>{{ creditLevel }}</b></p>
        <div class="profile-stats">
          <div>
            <small>已完成订单</small>
            <strong>{{ completedOrderCount }}</strong>
          </div>
          <div>
            <small>平均评分</small>
            <strong><el-icon><StarFilled /></el-icon> 4.9</strong>
          </div>
        </div>
      </section>

      <section class="right-card">
        <header class="right-head">
          <h2>我的订单状态</h2>
          <RouterLink to="/orders">查看全部 <el-icon><ArrowRight /></el-icon></RouterLink>
        </header>
        <div class="order-grid">
          <div class="order-box blue">
            <el-icon><Document /></el-icon>
            <span>待确认</span>
            <strong>{{ orderStats.pending }}</strong>
          </div>
          <div class="order-box green">
            <el-icon><Promotion /></el-icon>
            <span>进行中</span>
            <strong>{{ orderStats.running }}</strong>
          </div>
          <div class="order-box orange">
            <el-icon><ChatDotRound /></el-icon>
            <span>待评价</span>
            <strong>{{ orderStats.reviewing }}</strong>
          </div>
          <div class="order-box purple">
            <el-icon><CircleCheck /></el-icon>
            <span>已完成</span>
            <strong>{{ orderStats.completed }}</strong>
          </div>
        </div>
      </section>

      <section class="right-card">
        <header class="right-head">
          <h2>智能推荐</h2>
          <button type="button" @click="rotateRecommendations">换一换 <el-icon><Refresh /></el-icon></button>
        </header>
        <p class="hint">你经常接取快递代取任务，以下需求可能适合你</p>
        <div class="recommend-list">
          <button
            v-for="task in recommendedTasks"
            :key="task.taskId"
            type="button"
            class="recommend-item"
            @click="goDetail(task)"
          >
            <span :class="['small-icon', categoryMeta[task.category].tone]">
              <el-icon><component :is="categoryMeta[task.category].icon" /></el-icon>
            </span>
            <span>
              <strong>{{ task.title }}</strong>
              <small>{{ task.location || '校内待定地点' }}</small>
            </span>
            <b>{{ formatReward(task) }}</b>
          </button>
          <p v-if="!recommendedTasks.length" class="empty-hint">暂无可推荐需求</p>
        </div>
      </section>

      <section class="right-card">
        <header class="right-head">
          <h2>最新通知</h2>
          <RouterLink to="/messages">查看全部 <el-icon><ArrowRight /></el-icon></RouterLink>
        </header>
        <div class="notice-list">
          <p v-for="notice in notices" :key="notice.text" class="notice-item">
            <span :class="['notice-dot', notice.tone]"></span>
            <span>{{ notice.text }}</span>
            <small>{{ notice.time }}</small>
          </p>
        </div>
      </section>

      <section v-if="authStore.isAdmin" class="right-card admin-card">
        <div>
          <h2>管理员入口</h2>
          <p>数据管理与平台运营</p>
        </div>
        <RouterLink to="/admin">
          <el-icon><DataAnalysis /></el-icon>
          数据看板
          <el-icon><ArrowRight /></el-icon>
        </RouterLink>
      </section>
    </aside>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ArrowRight,
  Box,
  ChatDotRound,
  CircleCheck,
  CircleCheckFilled,
  Clock,
  DataAnalysis,
  Document,
  Location,
  Medal,
  Memo,
  MoreFilled,
  PriceTag,
  Promotion,
  Reading,
  Refresh,
  Search,
  StarFilled,
  UserFilled,
} from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'

import { getOrders, grabOrder } from '@/api/order'
import { getTasks } from '@/api/task'
import { useAppStore, useAuthStore, useFeedStore } from '@/stores'
import { resolveAssetUrl } from '@/utils/asset'
import {
  taskCategoryLabels,
  taskStatusLabels,
  type OrderListDTO,
  type SortType,
  type TaskCategory,
  type TaskListDTO,
} from '@/types'

const router = useRouter()
const route = useRoute()
const feedStore = useFeedStore()
const appStore = useAppStore()
const authStore = useAuthStore()

const keyword = ref(feedStore.keyword)
const sort = ref<SortType>(feedStore.sort)
const activeCategory = ref<TaskCategory | 'ALL'>(feedStore.category)
const taskPanelRef = ref<HTMLElement>()
const taskScrollRef = ref<HTMLElement>()
const orders = ref<OrderListDTO[]>([])
const recommendationTasks = ref<TaskListDTO[]>([])
const recommendationOffset = ref(0)

const tabs: Array<{ label: string; value: SortType }> = [
  { label: '推荐需求', value: 'hot' },
  { label: '最新发布', value: 'time' },
]

const validCategories = new Set<TaskCategory | 'ALL'>([
  'ALL',
  'EXPRESS',
  'STUDY',
  'SECOND_HAND',
  'TEAM_UP',
  'OTHER',
])

const categoryMeta: Record<TaskCategory, {
  icon: unknown
  tone: string
}> = {
  EXPRESS: { icon: Box, tone: 'express' },
  STUDY: { icon: Reading, tone: 'study' },
  SECOND_HAND: { icon: PriceTag, tone: 'trade' },
  TEAM_UP: { icon: UserFilled, tone: 'team' },
  OTHER: { icon: MoreFilled, tone: 'other' },
}

const notices = [
  { text: '王同学接取了你的需求', time: '2 分钟前', tone: 'blue' },
  { text: '你的订单已进入进行中状态', time: '15 分钟前', tone: 'green' },
  { text: '李同学给你留下了评价', time: '1 小时前', tone: 'purple' },
]

const displayName = computed(() => authStore.user?.nickname || '同学')
const currentAvatar = computed(() => resolveAssetUrl(authStore.user?.avatarUrl || ''))
const avatarFallback = computed(() => displayName.value.slice(0, 1).toUpperCase())
const creditScore = computed(() => authStore.user?.creditScore ?? 96)
const creditLevel = computed(() => {
  const score = creditScore.value
  if (score >= 95) return '优秀'
  if (score >= 85) return '良好'
  return '成长中'
})

const orderStats = computed(() => ({
  pending: orders.value.filter((order) => order.status === 'PENDING').length,
  running: orders.value.filter((order) => order.status === 'CONFIRMED').length,
  reviewing: orders.value.filter((order) => order.status === 'COMPLETED').length,
  completed: orders.value.filter((order) => order.status === 'COMPLETED').length,
}))

const completedOrderCount = computed(() => orderStats.value.completed)

const recommendedTasks = computed(() => {
  const openTasks = recommendationTasks.value.filter((task) => task.status === 'OPEN')
  if (openTasks.length <= 3) return openTasks

  return Array.from({ length: 3 }, (_, index) => {
    return openTasks[(recommendationOffset.value + index) % openTasks.length]
  })
})

const normalizeCategory = (value: unknown): TaskCategory | 'ALL' => {
  return typeof value === 'string' && validCategories.has(value as TaskCategory | 'ALL')
    ? (value as TaskCategory | 'ALL')
    : 'ALL'
}

const normalizeSort = (value: unknown): SortType => {
  return value === 'hot' ? 'hot' : 'time'
}

const applyFilters = async () => {
  feedStore.setKeyword(keyword.value)
  feedStore.setSort(sort.value)
  feedStore.setCategory(activeCategory.value)
  appStore.setActiveTaskCategory(activeCategory.value)

  await router.push({
    name: 'task-list',
    query: {
      ...(activeCategory.value === 'ALL' ? {} : { category: activeCategory.value }),
      ...(keyword.value.trim() ? { keyword: keyword.value.trim() } : {}),
      ...(sort.value === 'time' ? {} : { sort: sort.value }),
    },
  })

  await feedStore.fetchTasks()
  scrollTaskListToTop()
}

const switchTab = async (value: SortType) => {
  sort.value = value
  await applyFilters()
}

const goDetail = (task: TaskListDTO) => {
  router.push(`/tasks/${task.taskId}`)
}

const handlePageChange = async (page: number) => {
  feedStore.setPage(page)
  await feedStore.fetchTasks()
  scrollTaskListToTop()
}

const ensureAuthenticated = () => {
  if (authStore.isAuthenticated) return true
  ElMessage.warning('请先登录后再执行该操作')
  router.push('/login')
  return false
}

const handleGrab = async (task: TaskListDTO) => {
  if (!ensureAuthenticated()) return
  await grabOrder({ taskId: task.taskId })
  ElMessage.success('抢单成功')
  await feedStore.fetchTasks()
  await loadRecommendations()
  await loadOrders()
  router.push('/orders')
}

const scrollToTasks = () => {
  taskPanelRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const scrollTaskListToTop = () => {
  taskScrollRef.value?.scrollTo({ top: 0, behavior: 'smooth' })
}

const rotateRecommendations = () => {
  const openCount = recommendationTasks.value.filter((task) => task.status === 'OPEN').length
  if (!openCount) return
  recommendationOffset.value = (recommendationOffset.value + 1) % openCount
}

const formatTaskTitle = (task: TaskListDTO) => {
  const label = taskCategoryLabels[task.category]
  return task.title.includes(`【${label}】`) ? task.title : `【${label}】 ${task.title}`
}

const formatReward = (task: TaskListDTO) => {
  if (task.category === 'TEAM_UP' && (!task.reward || Number(task.reward) === 0)) {
    return '互助组队'
  }

  const rewardNumber = Number(task.reward)
  if (!Number.isNaN(rewardNumber)) {
    if (rewardNumber === 0) return '无偿'
    return `${Number.isInteger(rewardNumber) ? rewardNumber : rewardNumber.toFixed(2)} 元`
  }

  return String(task.reward || '面议')
}

const formatTime = (value: string) => {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value || '时间待定'

  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const loadOrders = async () => {
  const userId = authStore.user?.userId
  if (!authStore.isAuthenticated || !userId) {
    orders.value = []
    return
  }

  try {
    const page = await getOrders({ userId, page: 1, size: 50 })
    orders.value = page.records
  } catch {
    orders.value = []
  }
}

const loadRecommendations = async () => {
  try {
    const page = await getTasks({
      page: 1,
      size: 10,
      sort: 'hot',
      excludeCompleted: true,
    })
    recommendationTasks.value = page.records
    recommendationOffset.value = 0
  } catch {
    recommendationTasks.value = []
  }
}

watch(
  () => [route.query.category, route.query.keyword, route.query.sort],
  async ([category, routeKeyword, routeSort]) => {
    const normalizedCategory = normalizeCategory(category)
    activeCategory.value = normalizedCategory
    keyword.value = typeof routeKeyword === 'string' ? routeKeyword : ''
    sort.value = normalizeSort(routeSort)

    feedStore.setCategory(normalizedCategory)
    feedStore.setKeyword(keyword.value)
    feedStore.setSort(sort.value)
    appStore.setActiveTaskCategory(normalizedCategory)
    await feedStore.fetchTasks()
    scrollTaskListToTop()
  },
  { immediate: true },
)

watch(
  () => authStore.user?.userId,
  () => {
    loadOrders()
  },
)

onMounted(() => {
  loadOrders()
  loadRecommendations()
})
</script>

<style scoped>
.home-page {
  display: grid;
  height: calc(100vh - 122px);
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 22px;
  min-height: 0;
  overflow: hidden;
}

.home-main,
.right-sidebar {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 14px;
  min-height: 0;
  min-width: 0;
}

.right-sidebar {
  justify-content: space-between;
  overflow-y: auto;
  padding-right: 4px;
}

.hero-card,
.task-panel,
.right-card {
  border: 1px solid #e7edf7;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.055);
}

.hero-card {
  position: relative;
  min-height: 178px;
  overflow: hidden;
  padding: 24px 30px 22px;
  background:
    radial-gradient(circle at 82% 65%, rgba(255, 255, 255, 0.42), transparent 15%),
    radial-gradient(circle at 89% 35%, rgba(255, 255, 255, 0.34), transparent 10%),
    linear-gradient(120deg, #0878ff 0%, #376fff 45%, #7657ff 100%);
  color: #fff;
}

.hero-card::after {
  position: absolute;
  top: 0;
  right: 0;
  width: 36%;
  height: 100%;
  background:
    linear-gradient(to top, rgba(255, 255, 255, 0.18), transparent 70%),
    url("data:image/svg+xml,%3Csvg width='500' height='260' viewBox='0 0 500 260' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' stroke='rgba(255,255,255,.5)' stroke-width='3'%3E%3Cpath d='M80 175h260M105 175v-80h70v80M205 175v-115h95v115M330 175V65h60v110'/%3E%3Cpath d='M335 65l25-38 25 38M228 60l25-32 25 32'/%3E%3Cpath d='M42 70c40-38 78-31 114 0' stroke-dasharray='8 8'/%3E%3C/g%3E%3Cpath d='M35 48l65-20-22 64-15-28z' fill='rgba(255,255,255,.78)'/%3E%3Ccircle cx='415' cy='160' r='30' fill='rgba(255,255,255,.25)'/%3E%3Ccircle cx='450' cy='145' r='22' fill='rgba(255,255,255,.18)'/%3E%3C/svg%3E") center / cover no-repeat;
  content: "";
  opacity: 0.9;
}

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-content h1 {
  margin: 0 0 10px;
  font-size: 30px;
  letter-spacing: 0;
  line-height: 1.25;
}

.hero-content p {
  margin: 0;
  opacity: 0.92;
  font-size: 15px;
}

.quick-actions {
  display: grid;
  max-width: 690px;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.quick-card {
  display: flex;
  min-height: 78px;
  align-items: center;
  gap: 14px;
  padding: 14px;
  border: 0;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.96);
  color: #1f2937;
  cursor: pointer;
  font: inherit;
  text-align: left;
  text-decoration: none;
  box-shadow: 0 14px 26px rgba(8, 60, 160, 0.16);
}

.quick-icon {
  display: grid;
  width: 48px;
  height: 48px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  font-size: 24px;
}

.quick-icon.blue {
  background: #2f83ff;
}

.quick-icon.purple {
  background: #7c5cff;
}

.quick-icon.green {
  background: #21b485;
}

.quick-card strong,
.quick-card small {
  display: block;
}

.quick-card strong {
  color: #111827;
  font-size: 17px;
}

.quick-card small {
  margin-top: 5px;
  color: #64748b;
  line-height: 1.35;
}

.task-panel {
  display: flex;
  min-height: 0;
  flex: 1;
  flex-direction: column;
  padding: 16px 16px 10px;
}

.task-tabs {
  display: flex;
  gap: 46px;
  border-bottom: 1px solid #e5ebf4;
}

.task-tabs button {
  position: relative;
  padding: 0 8px 12px;
  border: 0;
  background: transparent;
  color: #6b7280;
  font: inherit;
  font-size: 17px;
  font-weight: 800;
  cursor: pointer;
}

.task-tabs button.active {
  color: #1268ed;
}

.task-tabs button.active::after {
  position: absolute;
  right: 0;
  bottom: -1px;
  left: 0;
  height: 3px;
  border-radius: 99px;
  background: #1268ed;
  content: "";
}

.search-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 92px;
  gap: 12px;
  margin: 12px 0 8px;
}

.task-scroll {
  min-height: 0;
  flex: 1;
  overflow-y: auto;
  padding-right: 8px;
  scrollbar-gutter: stable;
}

.task-scroll::-webkit-scrollbar,
.right-sidebar::-webkit-scrollbar {
  width: 8px;
}

.task-scroll::-webkit-scrollbar-thumb,
.right-sidebar::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: #cbd5e1;
}

.task-scroll::-webkit-scrollbar-track,
.right-sidebar::-webkit-scrollbar-track {
  background: transparent;
}

.task-list {
  display: flex;
  flex-direction: column;
}

.task-row {
  display: grid;
  grid-template-columns: 74px minmax(0, 1fr) 110px 190px;
  gap: 14px;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #eef2f7;
}

.task-row:last-child {
  border-bottom: 0;
}

.task-thumb {
  display: grid;
  width: 64px;
  height: 64px;
  place-items: center;
  border-radius: 12px;
  background: #eef5ff;
  color: #1677ff;
  font-size: 34px;
}

.task-row.study .task-thumb {
  background: #f1ecff;
  color: #7c3aed;
}

.task-row.trade .task-thumb {
  background: #eafaf2;
  color: #12a673;
}

.task-row.team .task-thumb {
  background: #fff5df;
  color: #f97316;
}

.task-row.other .task-thumb {
  background: #f3f4f6;
  color: #6b7280;
}

.task-body h2 {
  margin: 0 0 8px;
  color: #111827;
  font-size: 17px;
  line-height: 1.35;
}

.category-tag {
  display: inline-flex;
  align-items: center;
  margin-right: 8px;
  padding: 4px 8px;
  border-radius: 6px;
  background: #eaf3ff;
  color: #1268ed;
  font-size: 13px;
  font-weight: 800;
}

.task-row.study .category-tag {
  background: #f2ecff;
  color: #7c3aed;
}

.task-row.trade .category-tag {
  background: #e9f9ef;
  color: #12a673;
}

.task-row.team .category-tag {
  background: #fff0d9;
  color: #f97316;
}

.meta-line {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  margin-bottom: 8px;
  color: #6b7280;
  font-size: 13px;
}

.meta-line span,
.publisher {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.task-body p {
  margin: 0;
  color: #4b5563;
  font-size: 14px;
  line-height: 1.45;
}

.task-reward {
  text-align: center;
}

.task-reward strong {
  display: block;
  color: #f97316;
  font-size: 20px;
  font-weight: 900;
}

.status-pill {
  display: inline-flex;
  margin-top: 10px;
  padding: 6px 10px;
  border-radius: 999px;
  background: #e8f8ee;
  color: #119468;
  font-size: 13px;
  font-weight: 800;
}

.status-pill.in_progress {
  background: #eef5ff;
  color: #1268ed;
}

.status-pill.completed {
  background: #f3efff;
  color: #7c3aed;
}

.status-pill.cancelled,
.status-pill.offline {
  background: #f3f4f6;
  color: #6b7280;
}

.task-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.pager {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 10px;
  color: #64748b;
}

.right-card {
  padding: 14px;
}

.profile-top {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr) 84px;
  gap: 12px;
  align-items: center;
}

.profile-avatar {
  border: 4px solid #edf4ff;
}

.profile-top h2 {
  display: flex;
  align-items: center;
  gap: 5px;
  margin: 0 0 8px;
  color: #111827;
  font-size: 18px;
}

.profile-top h2 :deep(.el-icon) {
  color: #1677ff;
  font-size: 15px;
}

.role-line {
  display: flex;
  gap: 6px;
}

.role-line span {
  padding: 5px 9px;
  border-radius: 6px;
  background: #eef5ff;
  color: #1268ed;
  font-size: 13px;
  font-weight: 800;
}

.role-line span:nth-child(2) {
  background: #f2ecff;
  color: #7c3aed;
}

.credit-score {
  text-align: center;
}

.credit-score small {
  display: block;
  margin-bottom: 2px;
  color: #8b95a5;
  font-size: 12px;
}

.credit-score strong {
  color: #1268ed;
  font-size: 32px;
  line-height: 1;
}

.level-line {
  margin: 12px 0 16px 84px;
  color: #4b5563;
  font-size: 14px;
}

.level-line b {
  color: #1268ed;
}

.profile-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  padding-top: 16px;
  border-top: 1px solid #e8edf5;
  text-align: center;
}

.profile-stats div:first-child {
  border-right: 1px solid #e8edf5;
}

.profile-stats small,
.profile-stats strong {
  display: block;
}

.profile-stats small {
  margin-bottom: 6px;
  color: #6b7280;
  font-size: 13px;
}

.profile-stats strong {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #111827;
  font-size: 22px;
}

.profile-stats strong :deep(.el-icon) {
  color: #f6b800;
}

.right-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.right-head h2 {
  margin: 0;
  color: #111827;
  font-size: 18px;
}

.right-head a,
.right-head button {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: 0;
  background: transparent;
  color: #7b8495;
  font: inherit;
  font-size: 13px;
  text-decoration: none;
  cursor: pointer;
}

.order-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.order-box {
  display: grid;
  min-height: 88px;
  place-items: center;
  padding: 12px 6px;
  border-radius: 12px;
  text-align: center;
}

.order-box.blue {
  background: #f1f7ff;
  color: #1268ed;
}

.order-box.green {
  background: #ecfbf3;
  color: #12a673;
}

.order-box.orange {
  background: #fff5e7;
  color: #f59e0b;
}

.order-box.purple {
  background: #f3efff;
  color: #7c3aed;
}

.order-box :deep(.el-icon) {
  font-size: 22px;
}

.order-box span {
  color: #4b5563;
  font-size: 12px;
}

.order-box strong {
  color: #1f2937;
  font-size: 20px;
}

.hint {
  margin: -6px 0 12px;
  color: #7b8495;
  font-size: 13px;
}

.recommend-list,
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recommend-item {
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr) 64px;
  gap: 10px;
  align-items: center;
  padding: 0 0 10px;
  border: 0;
  border-bottom: 1px solid #eef2f7;
  background: transparent;
  color: inherit;
  font: inherit;
  text-align: left;
  cursor: pointer;
}

.recommend-item:last-child {
  border-bottom: 0;
  padding-bottom: 0;
}

.small-icon {
  display: grid;
  width: 32px;
  height: 32px;
  place-items: center;
  border-radius: 8px;
  background: #eef5ff;
  color: #1677ff;
  font-size: 18px;
}

.small-icon.study {
  background: #f1ecff;
  color: #7c3aed;
}

.small-icon.trade {
  background: #eafaf2;
  color: #12a673;
}

.small-icon.team {
  background: #fff5df;
  color: #f97316;
}

.recommend-item strong,
.recommend-item small {
  display: block;
}

.recommend-item strong {
  overflow: hidden;
  color: #1f2937;
  font-size: 14px;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommend-item small {
  margin-top: 4px;
  color: #7b8495;
  font-size: 12px;
}

.recommend-item b {
  color: #f97316;
  font-size: 14px;
  text-align: right;
}

.empty-hint {
  margin: 0;
  color: #8b95a5;
  font-size: 13px;
}

.notice-item {
  display: grid;
  grid-template-columns: 10px minmax(0, 1fr) 68px;
  gap: 10px;
  align-items: center;
  margin: 0;
  color: #4b5563;
  font-size: 13px;
}

.notice-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #1677ff;
}

.notice-dot.green {
  background: #21b485;
}

.notice-dot.purple {
  background: #8358ff;
}

.notice-item small {
  color: #8b95a5;
  text-align: right;
}

.admin-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.admin-card h2,
.admin-card p {
  margin: 0;
}

.admin-card h2 {
  color: #111827;
  font-size: 18px;
}

.admin-card p {
  margin-top: 6px;
  color: #6b7280;
  font-size: 13px;
}

.admin-card a {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  border: 1px solid #bfd5ff;
  border-radius: 10px;
  background: #f7fbff;
  color: #1268ed;
  font-weight: 800;
  text-decoration: none;
  white-space: nowrap;
}

@media (max-width: 1320px) {
  .home-page {
    height: auto;
    grid-template-columns: minmax(0, 1fr);
    overflow: visible;
  }

  .home-main,
  .right-sidebar {
    height: auto;
  }

  .right-sidebar {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    justify-content: initial;
    overflow: visible;
    padding-right: 0;
  }
}

@media (max-width: 980px) {
  .quick-actions,
  .right-sidebar {
    grid-template-columns: 1fr;
  }

  .task-row {
    grid-template-columns: 64px minmax(0, 1fr);
  }

  .task-reward,
  .task-actions {
    grid-column: 2;
    justify-content: flex-start;
    text-align: left;
  }
}

@media (max-width: 720px) {
  .hero-card {
    padding: 24px;
  }

  .hero-card::after {
    opacity: 0.36;
  }

  .search-row,
  .profile-top,
  .order-grid {
    grid-template-columns: 1fr;
  }

  .level-line {
    margin-left: 0;
  }

  .pager {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
