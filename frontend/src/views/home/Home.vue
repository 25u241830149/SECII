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
                      :disabled="!isJoinableTask(task)"
                      @click="handleGrab(task)"
                    >
                      {{ task.category === 'TEAM_UP' && task.status === 'IN_PROGRESS' ? '申请加入' : task.status === 'OPEN' ? '立即接单' : taskStatusLabels[task.status] }}
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
            <strong v-if="averageRating !== null"><el-icon><StarFilled /></el-icon> {{ averageRating.toFixed(1) }}</strong>
            <strong v-else class="empty-rating">暂无</strong>
          </div>
        </div>
      </section>

      <section class="right-card order-status-card">
        <header class="right-head">
          <h2>我的订单状态</h2>
          <RouterLink to="/orders">查看全部 <el-icon><ArrowRight /></el-icon></RouterLink>
        </header>
        <div class="order-grid">
          <button type="button" class="order-box cyan" @click="goOrders('OPEN')">
            <el-icon><Box /></el-icon>
            <span>待接单</span>
            <strong>{{ orderStats.waitingAcceptance }}</strong>
          </button>
          <button type="button" class="order-box blue" @click="goOrders('PENDING')">
            <el-icon><Document /></el-icon>
            <span>待确认</span>
            <strong>{{ orderStats.pending }}</strong>
          </button>
          <button type="button" class="order-box green" @click="goOrders('CONFIRMED')">
            <el-icon><Promotion /></el-icon>
            <span>进行中</span>
            <strong>{{ orderStats.inProgress }}</strong>
          </button>
          <button type="button" class="order-box orange" @click="goOrders('WAITING_REVIEW')">
            <el-icon><ChatDotRound /></el-icon>
            <span>待评价</span>
            <strong>{{ orderStats.waitingReview }}</strong>
          </button>
          <button type="button" class="order-box purple" @click="goOrders('COMPLETED')">
            <el-icon><CircleCheck /></el-icon>
            <span>已完成</span>
            <strong>{{ orderStats.completed }}</strong>
          </button>
        </div>
      </section>

      <section class="right-card recommendation-card">
        <header class="right-head">
          <h2>智能推荐</h2>
          <button type="button" @click="rotateRecommendations">换一换 <el-icon><Refresh /></el-icon></button>
        </header>
        <p class="hint">{{ recommendationHint }}</p>
        <div class="recommend-list">
          <template v-for="slot in recommendationSlots" :key="slot.key">
            <button
              v-if="slot.task"
              type="button"
              class="recommend-item"
              @click="goDetail(slot.task)"
            >
              <span :class="['small-icon', categoryMeta[slot.task.category].tone]">
                <el-icon><component :is="categoryMeta[slot.task.category].icon" /></el-icon>
              </span>
              <span>
                <strong>{{ slot.task.title }}</strong>
                <small>{{ slot.task.location || '校内待定地点' }}</small>
              </span>
              <b>{{ formatReward(slot.task) }}</b>
            </button>
            <p v-else class="recommend-item recommend-placeholder">
              <span class="small-icon placeholder"><el-icon><MoreFilled /></el-icon></span>
              <span>
                <strong>暂无更多推荐</strong>
                <small>继续浏览后会更懂你</small>
              </span>
              <b></b>
            </p>
          </template>
        </div>
      </section>

      <section class="right-card notice-card">
        <header class="right-head">
          <h2>最新通知</h2>
          <RouterLink to="/messages">查看全部 <el-icon><ArrowRight /></el-icon></RouterLink>
        </header>
        <div class="notice-list">
          <template v-if="notices.length">
            <p v-for="slot in noticeSlots" :key="slot.key" class="notice-item">
              <template v-if="slot.notice">
                <span :class="['notice-dot', { unread: !slot.notice.read }]"></span>
                <span>{{ slot.notice.title || slot.notice.content || '无标题消息' }}</span>
                <small>{{ formatRelativeTime(slot.notice.createdAt) }}</small>
              </template>
              <template v-else>
                <span class="notice-dot placeholder"></span>
                <span class="notice-placeholder-text">暂无更多消息</span>
                <small></small>
              </template>
            </p>
          </template>
          <div v-else class="notice-empty-state">
            <div class="empty-box-illustration" aria-hidden="true">
              <span class="box-lid"></span>
              <span class="box-body"></span>
              <span class="box-flap left"></span>
              <span class="box-flap right"></span>
              <span class="box-shadow"></span>
            </div>
            <p>暂无消息</p>
          </div>
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
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
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

import { getOrders, getOrderStats, grabOrder } from '@/api/order'
import { getMessages } from '@/api/message'
import { getTasks } from '@/api/task'
import { getUserHome } from '@/api/user'
import { useAppStore, useAuthStore, useFeedStore } from '@/stores'
import {
  getRecommendationHint,
  rankSmartTaskRecommendations,
  readTaskViewProfile,
  rememberTaskView,
  type TaskViewProfileEntry,
} from '@/utils/taskRecommendation'
import { resolveAssetUrl } from '@/utils/asset'
import {
  taskCategoryLabels,
  taskStatusLabels,
  type LocationTypeFilter,
  type MessageDTO,
  type OrderListDTO,
  type OrderStatsDTO,
  type RewardTypeFilter,
  type SortType,
  type TaskCategory,
  type TaskListDTO,
  type TaskStatusFilter,
  type UserHomeDTO,
} from '@/types'

const router = useRouter()
const goOrders = (status?: string) => {
  router.push({ path: '/orders', query: status ? { status } : {} })
}
const route = useRoute()
const feedStore = useFeedStore()
const appStore = useAppStore()
const authStore = useAuthStore()

const keyword = ref(feedStore.keyword)
const sort = ref<SortType>(feedStore.sort)
const statusFilter = ref<TaskStatusFilter>(feedStore.status)
const rewardTypeFilter = ref<RewardTypeFilter>(feedStore.rewardType)
const locationTypeFilter = ref<LocationTypeFilter>(feedStore.locationType)
const activeCategory = ref<TaskCategory | 'ALL'>(feedStore.category)
const taskPanelRef = ref<HTMLElement>()
const taskScrollRef = ref<HTMLElement>()
const orders = ref<OrderListDTO[]>([])
const orderStats = ref<OrderStatsDTO>({
  waitingAcceptance: 0,
  pending: 0,
  inProgress: 0,
  waitingReview: 0,
  completed: 0,
})
const recommendationCandidates = ref<TaskListDTO[]>([])
const recommendationTasks = ref<TaskListDTO[]>([])
const recommendationProfile = ref<TaskViewProfileEntry[]>([])
const recommendationOffset = ref(0)
const notices = ref<MessageDTO[]>([])
const userSummary = ref<UserHomeDTO | null>(null)

const tabs: Array<{ label: string; value: SortType }> = [
  { label: '最新发布', value: 'time' },
  { label: '热门推荐', value: 'hot' },
]

const validCategories = new Set<TaskCategory | 'ALL'>([
  'ALL',
  'EXPRESS',
  'STUDY',
  'SECOND_HAND',
  'TEAM_UP',
  'OTHER',
])
const validStatusFilters = new Set<TaskStatusFilter>(['ALL', 'OPEN', 'PENDING_CONFIRM', 'IN_PROGRESS'])
const validRewardFilters = new Set<RewardTypeFilter>(['ALL', 'PAID', 'FREE'])
const validLocationFilters = new Set<LocationTypeFilter>(['ALL', 'WITH_LOCATION', 'UNSPECIFIED'])

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

const displayName = computed(() => authStore.user?.nickname || '同学')
const currentAvatar = computed(() => resolveAssetUrl(authStore.user?.avatarUrl || ''))
const avatarFallback = computed(() => displayName.value.slice(0, 1).toUpperCase())
const creditScore = computed(() => userSummary.value?.creditScore ?? authStore.user?.creditScore ?? 90)
const creditLevel = computed(() => userSummary.value?.creditLevel || '普通用户')

const completedOrderCount = computed(() => userSummary.value?.completedOrderCount ?? orderStats.value.completed)
const averageRating = computed(() => userSummary.value?.averageRating ?? null)
const recommendationHint = computed(() => getRecommendationHint(recommendationProfile.value))

const recommendedTasks = computed(() => {
  const openTasks = recommendationTasks.value.filter(isJoinableTask)
  if (openTasks.length <= 3) return openTasks

  return Array.from({ length: 3 }, (_, index) => {
    return openTasks[(recommendationOffset.value + index) % openTasks.length]
  })
})

const recommendationSlots = computed(() => {
  return Array.from({ length: 3 }, (_, index) => {
    const task = recommendedTasks.value[index] ?? null
    return {
      key: task ? `task-${task.taskId}` : `recommend-empty-${index}`,
      task,
    }
  })
})

const noticeSlots = computed(() => {
  return Array.from({ length: 3 }, (_, index) => {
    const notice = notices.value[index] ?? null
    return {
      key: notice ? `notice-${notice.messageId}` : `notice-empty-${index}`,
      notice,
    }
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

const normalizeStatusFilter = (value: unknown): TaskStatusFilter => {
  return typeof value === 'string' && validStatusFilters.has(value as TaskStatusFilter)
    ? (value as TaskStatusFilter)
    : 'ALL'
}

const normalizeRewardFilter = (value: unknown): RewardTypeFilter => {
  return typeof value === 'string' && validRewardFilters.has(value as RewardTypeFilter)
    ? (value as RewardTypeFilter)
    : 'ALL'
}

const normalizeLocationFilter = (value: unknown): LocationTypeFilter => {
  return typeof value === 'string' && validLocationFilters.has(value as LocationTypeFilter)
    ? (value as LocationTypeFilter)
    : 'ALL'
}

const applyFilters = async () => {
  feedStore.setKeyword(keyword.value)
  feedStore.setSort(sort.value)
  feedStore.setStatus(statusFilter.value)
  feedStore.setRewardType(rewardTypeFilter.value)
  feedStore.setLocationType(locationTypeFilter.value)
  feedStore.setCategory(activeCategory.value)
  appStore.setActiveTaskCategory(activeCategory.value)

  await router.push({
    name: 'task-list',
    query: {
      ...(activeCategory.value === 'ALL' ? {} : { category: activeCategory.value }),
      ...(keyword.value.trim() ? { keyword: keyword.value.trim() } : {}),
      ...(sort.value === 'time' ? {} : { sort: sort.value }),
      ...(statusFilter.value === 'ALL' ? {} : { status: statusFilter.value }),
      ...(rewardTypeFilter.value === 'ALL' ? {} : { rewardType: rewardTypeFilter.value }),
      ...(locationTypeFilter.value === 'ALL' ? {} : { locationType: locationTypeFilter.value }),
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
  recommendationProfile.value = rememberTaskView(task, authStore.user?.userId)
  refreshSmartRecommendations()
  router.push(`/tasks/${task.taskId}`)
}

const isJoinableTask = (task: TaskListDTO) =>
  task.publisherId !== authStore.user?.userId &&
  (task.status === 'OPEN' || (task.category === 'TEAM_UP' && task.status === 'IN_PROGRESS'))

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
  ElMessage.success(task.category === 'TEAM_UP' ? '加入申请已提交' : '抢单成功')
  await feedStore.fetchTasks()
  await feedStore.fetchStats()
  await loadRecommendations()
  await loadOrders()
}

const scrollToTasks = async () => {
  activeCategory.value = 'ALL'
  keyword.value = ''
  sort.value = 'time'
  statusFilter.value = 'OPEN'
  rewardTypeFilter.value = 'ALL'
  locationTypeFilter.value = 'ALL'

  await applyFilters()
  taskPanelRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const scrollTaskListToTop = () => {
  taskScrollRef.value?.scrollTo({ top: 0, behavior: 'smooth' })
}

const rotateRecommendations = () => {
  const openCount = recommendationTasks.value.filter(isJoinableTask).length
  if (!openCount) return
  recommendationOffset.value = (recommendationOffset.value + 3) % openCount
}

const formatTaskTitle = (task: TaskListDTO) => {
  const label = taskCategoryLabels[task.category]
  return task.title.includes(`【${label}】`) ? task.title : `【${label}】 ${task.title}`
}

const formatReward = (task: TaskListDTO) => {
  if (task.category === 'TEAM_UP') {
    return `${task.teamCurrentMembers ?? 0}/${task.teamTotalMembers ?? 0}人`
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

const formatRelativeTime = (value: string) => {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '刚刚'

  const diffSeconds = Math.max(0, Math.floor((Date.now() - date.getTime()) / 1000))
  if (diffSeconds < 60) return '刚刚'
  const diffMinutes = Math.floor(diffSeconds / 60)
  if (diffMinutes < 60) return `${diffMinutes} 分钟前`
  const diffHours = Math.floor(diffMinutes / 60)
  if (diffHours < 24) return `${diffHours} 小时前`
  const diffDays = Math.floor(diffHours / 24)
  if (diffDays < 7) return `${diffDays} 天前`

  return date.toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
  })
}

const loadOrders = async () => {
  const userId = authStore.user?.userId
  if (!authStore.isAuthenticated || !userId) {
    orders.value = []
    orderStats.value = {
      waitingAcceptance: 0,
      pending: 0,
      inProgress: 0,
      waitingReview: 0,
      completed: 0,
    }
    return
  }

  try {
    const page = await getOrders({ userId, page: 1, size: 50 })
    orders.value = page.records
    orderStats.value = await getOrderStats(userId)
  } catch {
    orders.value = []
    orderStats.value = {
      waitingAcceptance: 0,
      pending: 0,
      inProgress: 0,
      waitingReview: 0,
      completed: 0,
    }
  }
}

const loadUserSummary = async () => {
  const userId = authStore.user?.userId
  if (!authStore.isAuthenticated || !userId) {
    userSummary.value = null
    return
  }

  try {
    userSummary.value = await getUserHome(userId)
  } catch {
    userSummary.value = null
  }
}

const refreshSmartRecommendations = (resetOffset = true) => {
  recommendationProfile.value = readTaskViewProfile(authStore.user?.userId)
  recommendationTasks.value = rankSmartTaskRecommendations(
    recommendationCandidates.value,
    recommendationProfile.value,
  )
  if (resetOffset) {
    recommendationOffset.value = 0
  }
}

const loadRecommendations = async () => {
  try {
    const page = await getTasks({
      page: 1,
      size: 50,
      sort: 'hot',
      excludeCompleted: true,
    })
    recommendationCandidates.value = page.records
    refreshSmartRecommendations()
  } catch {
    recommendationCandidates.value = []
    recommendationTasks.value = []
  }
}

const loadLatestMessages = async () => {
  if (!authStore.isAuthenticated) {
    notices.value = []
    return
  }

  try {
    const page = await getMessages({ page: 1, size: 3 })
    notices.value = page.records
  } catch {
    notices.value = []
  }
}

const handleMessageUnreadUpdated = () => {
  loadLatestMessages()
}

watch(
  () => [
    route.query.category,
    route.query.keyword,
    route.query.sort,
    route.query.status,
    route.query.rewardType,
    route.query.locationType,
  ],
  async ([category, routeKeyword, routeSort, routeStatus, routeRewardType, routeLocationType]) => {
    const normalizedCategory = normalizeCategory(category)
    activeCategory.value = normalizedCategory
    keyword.value = typeof routeKeyword === 'string' ? routeKeyword : ''
    sort.value = normalizeSort(routeSort)
    statusFilter.value = normalizeStatusFilter(routeStatus)
    rewardTypeFilter.value = normalizeRewardFilter(routeRewardType)
    locationTypeFilter.value = normalizeLocationFilter(routeLocationType)

    feedStore.setCategory(normalizedCategory)
    feedStore.setKeyword(keyword.value)
    feedStore.setSort(sort.value)
    feedStore.setStatus(statusFilter.value)
    feedStore.setRewardType(rewardTypeFilter.value)
    feedStore.setLocationType(locationTypeFilter.value)
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
    loadUserSummary()
    loadRecommendations()
    loadLatestMessages()
  },
)

onMounted(() => {
  window.addEventListener('campushub:message-unread-updated', handleMessageUnreadUpdated)
  loadOrders()
  loadUserSummary()
  loadRecommendations()
  loadLatestMessages()
  feedStore.fetchStats()
})

onBeforeUnmount(() => {
  window.removeEventListener('campushub:message-unread-updated', handleMessageUnreadUpdated)
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
  justify-content: flex-start;
  gap: 10px;
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

.status-pill.pending_confirm {
  background: #fff5e7;
  color: #f59e0b;
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
  box-sizing: border-box;
  flex: 0 0 auto;
  overflow: hidden;
  padding: 12px;
}

.profile-card {
  height: 206px;
}

.order-status-card {
  height: 160px;
}

.recommendation-card {
  height: 260px;
}

.notice-card {
  height: 154px;
}

.profile-top {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr) 84px;
  gap: 12px;
  align-items: center;
}

.profile-top > div {
  min-width: 0;
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
  flex-wrap: nowrap;
  gap: 6px;
}

.role-line span {
  display: inline-flex;
  min-width: 52px;
  align-items: center;
  justify-content: center;
  padding: 5px 9px;
  border-radius: 6px;
  background: #eef5ff;
  color: #1268ed;
  font-size: 13px;
  font-weight: 800;
  white-space: nowrap;
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
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}

.order-box {
  appearance: none;
  display: grid;
  grid-template-rows: auto auto auto;
  min-height: 88px;
  align-content: center;
  justify-items: center;
  padding: 11px 6px 10px;
  border: 0;
  border-radius: 12px;
  cursor: pointer;
  font: inherit;
  text-align: center;
}

.order-box.blue {
  background: #f1f7ff;
  color: #1268ed;
}

.order-box.cyan {
  background: #eff9ff;
  color: #0284c7;
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
  margin-bottom: 5px;
  font-size: 26px;
}

.order-box span {
  color: #4b5563;
  font-size: 12px;
  line-height: 1.1;
}

.order-box strong {
  margin-top: 8px;
  color: #1f2937;
  font-size: 24px;
  line-height: 1;
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

.recommend-list {
  min-height: 146px;
}

.notice-list {
  height: 92px;
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

.recommend-placeholder {
  margin: 0;
  cursor: default;
}

.small-icon {
  display: grid;
  width: 32px;
  height: 32px;
  place-items: center;
  border-radius: 8px;
  border: 0;
  cursor: pointer;
  font: inherit;
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

.small-icon.placeholder {
  background: #f3f6fb;
  color: #a8b3c3;
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

.notice-item {
  display: grid;
  flex: 1 1 0;
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
  background: #cbd5e1;
}

.notice-dot.unread {
  background: #1677ff;
}

.notice-dot.placeholder {
  background: #e2e8f0;
}

.notice-item small {
  color: #8b95a5;
  text-align: right;
}

.notice-placeholder-text {
  color: #94a3b8;
}

.notice-empty-state {
  display: grid;
  height: 100%;
  align-content: center;
  justify-items: center;
  margin: 0;
  color: #94a3b8;
  font-size: 13px;
  text-align: center;
}

.notice-empty-state p {
  margin: 4px 0 0;
}

.empty-box-illustration {
  position: relative;
  width: 94px;
  height: 56px;
}

.empty-box-illustration span {
  position: absolute;
  display: block;
}

.box-body {
  right: 15px;
  bottom: 5px;
  left: 18px;
  height: 32px;
  background: #eef1f6;
}

.box-lid {
  top: 5px;
  left: 22px;
  width: 50px;
  height: 28px;
  transform: rotate(18deg);
  background: #f3f5f9;
  clip-path: polygon(0 16%, 70% 0, 100% 55%, 26% 100%);
}

.box-flap.left {
  bottom: 22px;
  left: 4px;
  width: 34px;
  height: 25px;
  background: #f1f4f8;
  clip-path: polygon(100% 0, 100% 100%, 0 100%);
}

.box-flap.right {
  right: 2px;
  bottom: 22px;
  width: 34px;
  height: 25px;
  background: #f5f7fb;
  clip-path: polygon(0 0, 100% 100%, 0 100%);
}

.box-shadow {
  right: 8px;
  bottom: 0;
  left: 8px;
  height: 9px;
  border-radius: 50%;
  background: #f3f5fa;
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

  .profile-card,
  .order-status-card,
  .recommendation-card,
  .notice-card {
    height: auto;
  }
}
</style>
