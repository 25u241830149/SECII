<template>
  <section class="home-page">
    <section class="hero">
      <div class="hero-copy">
        <p class="eyebrow">Sprint 2 / 任务与订单核心</p>
        <h1>校园任务流已经接入真实后端，发布、浏览、收藏、抢单走同一条链路。</h1>
        <p class="summary">
          按分类筛选，按关键词搜索，支持最新和热度排序。点进详情后可以直接收藏或抢单。
        </p>
        <div class="hero-actions">
          <RouterLink class="hero-button primary" to="/tasks/publish">发布任务</RouterLink>
          <RouterLink class="hero-button ghost" to="/orders">查看订单</RouterLink>
        </div>
      </div>
      <div class="hero-stats">
        <article>
          <strong>{{ feedStore.total }}</strong>
          <span>当前可见任务</span>
        </article>
        <article>
          <strong>{{ openTaskCount }}</strong>
          <span>可抢单任务</span>
        </article>
        <article>
          <strong>{{ favoriteableCount }}</strong>
          <span>已带收藏态</span>
        </article>
      </div>
    </section>

    <section class="toolbar">
      <CategoryNav
        :model-value="activeCategory"
        @update:model-value="handleCategoryChange"
      />
      <div class="filters">
        <el-input
          v-model="keyword"
          placeholder="搜索任务标题或描述"
          clearable
          @keyup.enter="applyFilters"
          @clear="applyFilters"
        />
        <el-select v-model="sort" @change="applyFilters">
          <el-option label="最新发布" value="time" />
          <el-option label="热度优先" value="hot" />
        </el-select>
        <el-button type="primary" @click="applyFilters">筛选</el-button>
      </div>
    </section>

    <section class="feed-section">
      <div class="section-head">
        <div>
          <h2>任务信息流</h2>
          <p>支持分页、分类、关键词和热度排序。</p>
        </div>
      </div>

      <el-skeleton :loading="feedStore.loading" animated :rows="6">
        <template #default>
          <el-empty v-if="!feedStore.tasks.length" description="暂无匹配任务" />
          <div v-else class="task-grid">
            <TaskCard
              v-for="task in feedStore.tasks"
              :key="task.taskId"
              :task="task"
              :show-favorite="true"
              :show-grab="true"
              @view="goDetail"
              @grab="handleGrab"
              @favorite="toggleFavorite"
            />
          </div>
        </template>
      </el-skeleton>

      <div class="pager">
        <span>共 {{ feedStore.total }} 条</span>
        <el-pagination
          v-model:current-page="feedStore.page"
          layout="prev, pager, next"
          :page-size="feedStore.size"
          :total="feedStore.total"
          @current-change="handlePageChange"
        />
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'

import { favoriteTask, unfavoriteTask } from '@/api/task'
import { grabOrder } from '@/api/order'
import CategoryNav from '@/components/CategoryNav.vue'
import TaskCard from '@/components/TaskCard.vue'
import { useAppStore, useAuthStore, useFeedStore } from '@/stores'
import type { SortType, TaskCategory, TaskListDTO } from '@/types'

const router = useRouter()
const route = useRoute()
const feedStore = useFeedStore()
const appStore = useAppStore()
const authStore = useAuthStore()

const keyword = ref(feedStore.keyword)
const sort = ref<SortType>(feedStore.sort)
const activeCategory = ref<TaskCategory | 'ALL'>(feedStore.category)
const validCategories = new Set<TaskCategory | 'ALL'>([
  'ALL',
  'EXPRESS',
  'STUDY',
  'SECOND_HAND',
  'TEAM_UP',
  'OTHER',
])

const openTaskCount = computed(() => feedStore.tasks.filter((task) => task.status === 'OPEN').length)
const favoriteableCount = computed(() => feedStore.tasks.filter((task) => task.favorited !== undefined).length)

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
}

const handleCategoryChange = async (category: TaskCategory | 'ALL') => {
  activeCategory.value = category
  await applyFilters()
}

const goDetail = (task: TaskListDTO) => {
  router.push(`/tasks/${task.taskId}`)
}

const handlePageChange = async (page: number) => {
  feedStore.setPage(page)
  await feedStore.fetchTasks()
}

const ensureAuthenticated = () => {
  if (authStore.isAuthenticated) return true
  ElMessage.warning('请先登录后再执行该操作')
  router.push('/login')
  return false
}

const toggleFavorite = async (task: TaskListDTO) => {
  if (!ensureAuthenticated()) return
  if (task.favorited) {
    await unfavoriteTask(task.taskId)
  } else {
    await favoriteTask(task.taskId)
  }
  await feedStore.fetchTasks()
}

const handleGrab = async (task: TaskListDTO) => {
  if (!ensureAuthenticated()) return
  await grabOrder({ taskId: task.taskId })
  ElMessage.success('抢单成功')
  await feedStore.fetchTasks()
  router.push('/orders')
}

const normalizeCategory = (value: unknown): TaskCategory | 'ALL' => {
  return typeof value === 'string' && validCategories.has(value as TaskCategory | 'ALL')
    ? (value as TaskCategory | 'ALL')
    : 'ALL'
}

watch(
  () => route.query.category,
  async (category) => {
    const normalizedCategory = normalizeCategory(category)
    activeCategory.value = normalizedCategory
    feedStore.setCategory(normalizedCategory)
    appStore.setActiveTaskCategory(normalizedCategory)
    await feedStore.fetchTasks()
  },
  { immediate: true },
)

onMounted(() => {
  keyword.value = feedStore.keyword
  sort.value = feedStore.sort
})
</script>

<style scoped>
.home-page {
  display: grid;
  gap: 18px;
}

.hero,
.toolbar,
.feed-section {
  border: 1px solid #e7edf7;
  border-radius: 24px;
  background: #fff;
  box-shadow: 0 20px 38px rgba(15, 23, 42, 0.08);
}

.hero {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) 320px;
  gap: 24px;
  padding: 30px;
  background:
    linear-gradient(130deg, rgba(14, 116, 144, 0.08), transparent 35%),
    linear-gradient(145deg, rgba(37, 99, 235, 0.12), rgba(251, 191, 36, 0.08)),
    #fff;
}

.eyebrow {
  margin: 0;
  color: #0f766e;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero h1 {
  margin: 10px 0 14px;
  color: #111827;
  font-size: 34px;
  line-height: 1.25;
}

.summary {
  max-width: 720px;
  margin: 0;
  color: #475569;
  font-size: 16px;
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.hero-button {
  display: inline-flex;
  min-height: 44px;
  align-items: center;
  justify-content: center;
  padding: 0 18px;
  border-radius: 999px;
  font-weight: 700;
  text-decoration: none;
}

.hero-button.primary {
  background: #1d4ed8;
  color: #fff;
}

.hero-button.ghost {
  border: 1px solid #cbd5e1;
  color: #1f2937;
}

.hero-stats {
  display: grid;
  gap: 14px;
}

.hero-stats article {
  padding: 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.76);
  box-shadow: inset 0 0 0 1px rgba(219, 230, 244, 0.9);
}

.hero-stats strong,
.hero-stats span {
  display: block;
}

.hero-stats strong {
  color: #111827;
  font-size: 30px;
}

.hero-stats span {
  margin-top: 6px;
  color: #64748b;
}

.toolbar {
  display: grid;
  gap: 16px;
  padding: 18px 22px;
}

.filters {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 160px 100px;
  gap: 12px;
}

.feed-section {
  padding: 22px;
}

.section-head h2,
.section-head p {
  margin: 0;
}

.section-head p {
  margin-top: 6px;
  color: #64748b;
}

.task-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 18px;
}

.pager {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 18px;
  color: #64748b;
}

@media (max-width: 1100px) {
  .hero {
    grid-template-columns: 1fr;
  }

  .task-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .filters {
    grid-template-columns: 1fr;
  }

  .pager {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
