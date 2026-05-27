<template>
  <div class="main-shell">
    <header class="topbar">
      <RouterLink class="brand" to="/">
        <CampusHubLogo size="sm" />
        <strong>CampusHub</strong>
        <span>校园互助平台</span>
      </RouterLink>

      <label class="search-box">
        <el-icon><Search /></el-icon>
        <input
          v-model="topKeyword"
          placeholder="搜索快递代取、学习辅导、二手交易、活动组队..."
          @keyup.enter="handleTopSearch"
        />
      </label>

      <nav class="top-actions" aria-label="快捷入口">
        <RouterLink class="publish-button" to="/tasks/publish">
          <el-icon><Plus /></el-icon>
          发布需求
        </RouterLink>
        <RouterLink class="nav-link" to="/orders">
          <el-icon><Document /></el-icon>
          我的订单
        </RouterLink>
        <RouterLink class="notice-button" to="/messages">
          <el-icon><Bell /></el-icon>
          消息通知
          <span v-if="unreadMessageCount" class="message-badge">{{ unreadMessageCount }}</span>
        </RouterLink>
        <el-dropdown
          v-if="authStore.isAuthenticated"
          trigger="click"
          class="user-menu"
          @command="handleUserCommand"
        >
          <button type="button" class="user-entry user-menu-trigger">
            <el-avatar :size="38" :src="headerAvatar || undefined" class="avatar">
              {{ headerAvatarFallback }}
            </el-avatar>
            <span>个人中心</span>
            <el-icon class="chevron"><ArrowDown /></el-icon>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile-edit">个人主页</el-dropdown-item>
              <el-dropdown-item command="account-delete">账号注销</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <RouterLink v-else class="auth-link primary" to="/login">登录 / 注册</RouterLink>
      </nav>
    </header>

    <div class="workspace" :class="{ 'profile-workspace': isProfileArea }">
      <aside v-if="!isProfileArea" class="sidebar">
        <section class="sidebar-section">
          <div class="section-title">
            <h2>需求分类</h2>
            <button type="button" @click="appStore.toggleSidebar">☰</button>
          </div>
          <button
            v-for="item in categories"
            :key="item.value"
            type="button"
            class="sidebar-item"
            :class="{ active: appStore.activeTaskCategory === item.value }"
            @click="selectTaskCategory(item.value)"
          >
            <span :class="['category-icon', item.tone]">
              <el-icon><component :is="item.icon" /></el-icon>
            </span>
            <strong>{{ item.label }}</strong>
          </button>
        </section>

        <section class="sidebar-section filters">
          <h2>筛选条件</h2>
          <div
            v-for="group in filterGroups"
            :key="group.key"
            class="filter-row"
            :class="{ 'sort-filter-row': group.key === 'sort', 'status-filter-row': group.key === 'status' }"
          >
            <b>{{ group.label }}</b>
            <button
              v-for="option in group.options"
              :key="option.value"
              type="button"
              class="pill"
              :class="{ active: isTaskFilterActive(group.key, option.value) }"
              @click="selectTaskFilter(group.key, option.value)"
            >
              {{ option.label }}
            </button>
          </div>
        </section>

        <section class="sidebar-section stats">
          <div>
            <span class="stat-dot blue"><el-icon><TrendCharts /></el-icon></span>
            <b>{{ feedStore.stats.todayCreated }}</b>
            <small>今日新增</small>
          </div>
          <div>
            <span class="stat-dot green"><el-icon><Clock /></el-icon></span>
            <b>{{ feedStore.stats.inProgress }}</b>
            <small>进行中</small>
          </div>
          <div>
            <span class="stat-dot orange"><el-icon><CircleCheck /></el-icon></span>
            <b>{{ feedStore.stats.completed }}</b>
            <small>已完成</small>
          </div>
        </section>
      </aside>

      <main class="content-area">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ArrowDown,
  Bell,
  Box,
  CircleCheck,
  Clock,
  Document,
  Grid,
  MoreFilled,
  PriceTag,
  Plus,
  Reading,
  Search,
  TrendCharts,
  UserFilled,
} from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'

import { getUnreadMessageCount } from '@/api/message'
import CampusHubLogo from '@/components/CampusHubLogo.vue'
import { useAppStore, useAuthStore, useFeedStore, useUserStore } from '@/stores'
import { resolveAssetUrl } from '@/utils/asset'
import type { LocationTypeFilter, RewardTypeFilter, SortType, TaskCategory, TaskStatusFilter } from '@/types'

const appStore = useAppStore()
const authStore = useAuthStore()
const feedStore = useFeedStore()
const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const unreadMessageCount = ref(0)
const topKeyword = ref('')
const messageUnreadUpdatedEvent = 'campushub:message-unread-updated'

const isProfileArea = computed(() => route.path.startsWith('/profile'))
const headerAvatar = computed(() => resolveAssetUrl(userStore.profile?.avatarUrl || authStore.user?.avatarUrl || ''))
const headerAvatarFallback = computed(() => (authStore.user?.nickname || '个').slice(0, 1).toUpperCase())

const categories = [
  { value: 'ALL' as const, label: '全部需求', icon: Grid, tone: 'category-blue' },
  { value: 'EXPRESS' as const, label: '快递代取', icon: Box, tone: 'category-orange' },
  { value: 'STUDY' as const, label: '学习辅导', icon: Reading, tone: 'category-blue' },
  { value: 'SECOND_HAND' as const, label: '二手交易', icon: PriceTag, tone: 'category-orange' },
  { value: 'TEAM_UP' as const, label: '活动组队', icon: UserFilled, tone: 'category-blue' },
  { value: 'OTHER' as const, label: '其他', icon: MoreFilled, tone: 'category-gray' },
]

type CategoryFilter = TaskCategory | 'ALL'
type TaskFilterKey = 'sort' | 'status' | 'rewardType' | 'locationType'
type TaskFilterValue = SortType | TaskStatusFilter | RewardTypeFilter | LocationTypeFilter

const validCategoryValues = new Set<CategoryFilter>(categories.map((item) => item.value))
const validStatusFilters = new Set<TaskStatusFilter>(['ALL', 'OPEN', 'PENDING_CONFIRM', 'IN_PROGRESS'])
const validRewardFilters = new Set<RewardTypeFilter>(['ALL', 'PAID', 'FREE'])
const validLocationFilters = new Set<LocationTypeFilter>(['ALL', 'WITH_LOCATION', 'UNSPECIFIED'])

const filterGroups: Array<{
  key: TaskFilterKey
  label: string
  options: Array<{ label: string; value: TaskFilterValue }>
}> = [
  {
    key: 'sort',
    label: '排序',
    options: [
      { label: '最新发布', value: 'time' },
      { label: '热门推荐', value: 'hot' },
    ],
  },
  {
    key: 'status',
    label: '状态',
    options: [
      { label: '全部', value: 'ALL' },
      { label: '可接单', value: 'OPEN' },
      { label: '待确认', value: 'PENDING_CONFIRM' },
      { label: '进行中', value: 'IN_PROGRESS' },
    ],
  },
  {
    key: 'rewardType',
    label: '报酬',
    options: [
      { label: '全部', value: 'ALL' },
      { label: '有偿', value: 'PAID' },
      { label: '无偿', value: 'FREE' },
    ],
  },
  {
    key: 'locationType',
    label: '地点',
    options: [
      { label: '全部', value: 'ALL' },
      { label: '已填写', value: 'WITH_LOCATION' },
      { label: '待确定', value: 'UNSPECIFIED' },
    ],
  },
]

const normalizeCategory = (value: unknown): CategoryFilter => {
  return typeof value === 'string' && validCategoryValues.has(value as CategoryFilter)
    ? (value as CategoryFilter)
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

const activeTaskFilters = computed(() => ({
  sort: normalizeSort(route.query.sort),
  status: normalizeStatusFilter(route.query.status),
  rewardType: normalizeRewardFilter(route.query.rewardType),
  locationType: normalizeLocationFilter(route.query.locationType),
}))

const buildTaskQuery = (filters: {
  category?: CategoryFilter
  keyword?: string
  sort?: SortType
  status?: TaskStatusFilter
  rewardType?: RewardTypeFilter
  locationType?: LocationTypeFilter
} = {}) => {
  const category = filters.category ?? normalizeCategory(route.query.category)
  const keyword = filters.keyword ?? (typeof route.query.keyword === 'string' ? route.query.keyword : '')
  const sort = filters.sort ?? activeTaskFilters.value.sort
  const status = filters.status ?? activeTaskFilters.value.status
  const rewardType = filters.rewardType ?? activeTaskFilters.value.rewardType
  const locationType = filters.locationType ?? activeTaskFilters.value.locationType

  return {
    ...(category === 'ALL' ? {} : { category }),
    ...(keyword.trim() ? { keyword: keyword.trim() } : {}),
    ...(sort === 'time' ? {} : { sort }),
    ...(status === 'ALL' ? {} : { status }),
    ...(rewardType === 'ALL' ? {} : { rewardType }),
    ...(locationType === 'ALL' ? {} : { locationType }),
  }
}

const isTaskFilterActive = (key: TaskFilterKey, value: TaskFilterValue) => {
  return activeTaskFilters.value[key] === value
}

const selectTaskFilter = (key: TaskFilterKey, value: TaskFilterValue) => {
  router.push({
    name: 'task-list',
    query: buildTaskQuery({ [key]: value }),
  })
}

const selectTaskCategory = (category: CategoryFilter) => {
  appStore.setActiveTaskCategory(category)
  router.push({
    name: 'task-list',
    query: buildTaskQuery({ category }),
  })
}

const handleTopSearch = () => {
  const keyword = topKeyword.value.trim()
  router.push({
    name: 'task-list',
    query: buildTaskQuery({
      category: appStore.activeTaskCategory,
      keyword,
    }),
  })
}

const handleUserCommand = (command: string | number | object) => {
  if (command === 'profile-edit') {
    router.push('/profile')
    return
  }

  if (command === 'account-delete') {
    router.push('/profile/delete')
    return
  }

  if (command === 'logout') {
    userStore.setProfile(null)
    authStore.logout()
    ElMessage.success('已退出登录')
    router.replace('/login')
  }
}

const refreshUnreadCount = async () => {
  if (!authStore.isAuthenticated) {
    unreadMessageCount.value = 0
    return
  }

  try {
    unreadMessageCount.value = (await getUnreadMessageCount()).count
  } catch {
    unreadMessageCount.value = 0
  }
}

const handleMessageUnreadUpdated = () => {
  refreshUnreadCount()
}

const refreshTaskStats = () => {
  feedStore.fetchStats()
}

watch(
  () => [authStore.isAuthenticated, route.fullPath],
  () => {
    refreshUnreadCount()
    refreshTaskStats()
  },
)

onMounted(() => {
  topKeyword.value = typeof route.query.keyword === 'string' ? route.query.keyword : ''
  window.addEventListener(messageUnreadUpdatedEvent, handleMessageUnreadUpdated)
  refreshUnreadCount()
  refreshTaskStats()
})

onBeforeUnmount(() => {
  window.removeEventListener(messageUnreadUpdatedEvent, handleMessageUnreadUpdated)
})
</script>

<style scoped>
.main-shell {
  min-height: 100vh;
  background: #f5f7fb;
}

.topbar {
  position: sticky;
  z-index: 10;
  top: 0;
  display: grid;
  min-height: 72px;
  grid-template-columns: minmax(300px, auto) minmax(320px, 520px) auto;
  gap: 24px;
  align-items: center;
  padding: 0 28px;
  border-bottom: 1px solid #e8edf5;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.04);
}

.brand,
.top-actions,
.user-entry {
  display: flex;
  align-items: center;
}

.brand {
  gap: 12px;
  color: #111827;
  text-decoration: none;
}

.brand strong {
  color: #1168e8;
  font-size: 22px;
}

.brand span:last-child {
  color: #374151;
  font-size: 17px;
  font-weight: 700;
}

.search-box {
  display: flex;
  height: 46px;
  align-items: center;
  gap: 12px;
  padding: 0 16px;
  border: 1px solid #dfe6f2;
  border-radius: 14px;
  background: #fff;
  color: #9ca3af;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.search-box input {
  width: 100%;
  border: 0;
  outline: 0;
  background: transparent;
  color: #475569;
  font: inherit;
}

.top-actions {
  justify-content: flex-end;
  gap: 22px;
  color: #374151;
}

.top-actions a,
.notice-button,
.user-menu-trigger {
  border: 0;
  background: transparent;
  color: inherit;
  font: inherit;
  text-decoration: none;
  white-space: nowrap;
}

.nav-link,
.notice-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.publish-button {
  display: inline-flex;
  height: 40px;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 0 26px;
  border-radius: 10px;
  background: linear-gradient(135deg, #1478ff, #6b48ff) !important;
  color: #fff !important;
  font-weight: 700;
  line-height: 1;
  box-shadow: 0 10px 20px rgba(79, 124, 255, 0.26);
}

.publish-button :deep(.el-icon) {
  font-size: 18px;
}

.notice-button {
  position: relative;
  cursor: pointer;
}

.message-badge {
  position: absolute;
  top: -14px;
  right: -16px;
  display: grid;
  width: 20px;
  height: 20px;
  place-items: center;
  border-radius: 50%;
  background: #ff3b4e;
  color: #fff;
  font-size: 12px;
  font-weight: 800;
}

.user-entry {
  gap: 9px;
}

.user-menu-trigger {
  cursor: pointer;
}

.chevron {
  color: #8b95a5;
  line-height: 1;
}

.auth-link {
  display: inline-flex;
  height: 40px;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  border-radius: 8px;
  line-height: 1;
}

.auth-link.primary {
  background: #eef5ff;
  color: #1268ed !important;
  font-weight: 700;
}

.avatar {
  border: 3px solid #edf4ff;
  background: radial-gradient(circle at 35% 30%, #e8f1ff, #b8cffd 42%, #354f87 43%, #172033 100%);
  color: #fff;
  font-weight: 700;
  box-shadow: 0 6px 14px rgba(15, 23, 42, 0.12);
}

.workspace {
  display: grid;
  grid-template-columns: 270px minmax(0, 1fr);
  gap: 22px;
  padding: 22px 28px 28px;
}

.workspace.profile-workspace {
  grid-template-columns: minmax(0, 1fr);
}

.sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sidebar-section {
  padding: 18px;
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.055);
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-title button {
  display: none;
}

.sidebar-section h2 {
  margin: 0 0 14px;
  color: #111827;
  font-size: 18px;
}

.sidebar-item {
  display: flex;
  width: 100%;
  height: 44px;
  align-items: center;
  gap: 12px;
  margin-top: 9px;
  padding: 0 14px;
  border: 0;
  border-radius: 8px;
  background: #f8fafc;
  color: #4b5563;
  font: inherit;
  text-align: left;
  cursor: pointer;
}

.sidebar-item.active,
.sidebar-item:hover {
  background: #eef5ff;
  color: #1268ed;
}

.category-icon {
  display: grid;
  width: 28px;
  height: 28px;
  place-items: center;
  font-size: 24px;
  line-height: 1;
}

.category-blue {
  color: #1677ff;
}

.category-orange {
  color: #ff8a1f;
}

.category-gray {
  width: 26px;
  height: 26px;
  border: 2px solid #7d8795;
  border-radius: 50%;
  color: #4b5563;
  font-size: 16px;
}

.filter-row {
  display: grid;
  grid-template-columns: 42px repeat(3, minmax(0, 1fr));
  align-items: center;
  gap: 8px;
  margin: 13px 0;
}

.sort-filter-row {
  grid-template-columns: 42px repeat(2, minmax(76px, 1fr));
}

.status-filter-row {
  grid-template-columns: 42px repeat(4, minmax(0, 1fr));
  gap: 5px;
}

.status-filter-row .pill {
  padding-right: 4px;
  padding-left: 4px;
}

.filter-row b {
  color: #374151;
  font-weight: 600;
}

.pill {
  border: 1px solid #e1e8f2;
  min-width: 0;
  padding: 7px 6px;
  border-radius: 8px;
  background: #fff;
  color: #5b6472;
  cursor: pointer;
  font: inherit;
  font-size: 13px;
  line-height: 1.2;
  text-align: center;
  white-space: nowrap;
}

.pill.active {
  border-color: #d7e8ff;
  background: #edf5ff;
  color: #176fed;
  font-weight: 700;
}

.stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0;
  padding: 20px 8px;
  text-align: center;
}

.stats div + div {
  border-left: 1px solid #e5eaf2;
}

.stat-dot {
  display: grid;
  width: 34px;
  height: 34px;
  place-items: center;
  margin: 0 auto 8px;
  border-radius: 50%;
  color: #fff;
  font-size: 20px;
  font-weight: 800;
}

.stat-dot :deep(.el-icon) {
  font-size: 20px;
}

.blue {
  background: #1677ff;
}

.green {
  background: #21b485;
}

.orange {
  background: #ff8a1f;
}

.stats b {
  display: block;
  font-size: 26px;
}

.stats small {
  display: block;
  color: #6b7280;
}

.content-area {
  min-width: 0;
}

@media (max-width: 1180px) {
  .topbar {
    grid-template-columns: 1fr;
    padding: 16px 20px;
  }

  .top-actions {
    justify-content: flex-start;
    overflow-x: auto;
    padding-bottom: 4px;
  }

  .workspace {
    grid-template-columns: 1fr;
    padding: 18px;
  }

  .sidebar {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .stats {
    grid-column: 1 / -1;
  }
}

@media (max-width: 720px) {
  .sidebar {
    grid-template-columns: 1fr;
  }

  .brand span:last-child {
    display: none;
  }
}
</style>
