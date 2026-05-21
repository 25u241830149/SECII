<template>
  <section class="list-page">
    <header class="page-hero">
      <div>
        <p><el-icon><Star /></el-icon> 我的收藏</p>
        <h1>我的收藏</h1>
        <span>收藏的任务，随时查看，不错过任何机会</span>
      </div>
      <div class="hero-art">☆</div>
    </header>

    <section class="summary-strip">
      <article v-for="item in stats" :key="item.label">
        <span :class="['stat-icon', item.tone]">
          <el-icon><component :is="item.icon" /></el-icon>
        </span>
        <div>
          <small>{{ item.label }}</small>
          <strong>{{ item.value }}</strong>
        </div>
      </article>
    </section>

    <section class="table-card">
      <div class="toolbar">
        <div class="tabs">
          <button
            v-for="tab in tabs"
            :key="tab.value"
            type="button"
            :class="{ active: activeTab === tab.value }"
            @click="activeTab = tab.value"
          >
            {{ tab.label }}
          </button>
        </div>
        <div class="tools">
          <el-input v-model="keyword" :prefix-icon="Search" placeholder="搜索收藏的任务..." />
          <el-select v-model="sortBy" class="sort-select">
            <el-option label="最新收藏" value="latest" />
            <el-option label="奖励最高" value="reward" />
          </el-select>
        </div>
      </div>

      <div class="task-table">
        <article v-for="task in pagedTasks" :key="task.id" class="favorite-item">
          <div class="task-main">
            <div class="task-image">{{ task.image }}</div>
            <div>
              <h2>{{ task.title }}</h2>
              <p>
                <span>{{ profileTaskCategoryLabels[task.category] }}</span>
                <el-icon><Location /></el-icon>{{ task.location }}
              </p>
            </div>
          </div>
          <div class="money">
            <small>奖励</small>
            <strong>￥{{ task.reward }}</strong>
          </div>
          <div>
            <small>截止时间</small>
            <strong>{{ task.deadline }}</strong>
          </div>
          <div>
            <small>收藏时间</small>
            <strong>{{ task.collectedAt }}</strong>
          </div>
          <span :class="['status-pill', statusToneMap[task.status]]">
            {{ profileTaskStatusLabels[task.status] }}
          </span>
          <div class="row-actions">
            <el-button>查看详情</el-button>
            <el-button>取消收藏</el-button>
          </div>
        </article>
      </div>

      <footer class="table-footer">
        <span>共 {{ filteredTasks.length }} 条</span>
        <el-pagination
          v-model:current-page="page"
          layout="prev, pager, next"
          :page-size="pageSize"
          :total="filteredTasks.length"
        />
        <span>每页显示 {{ pageSize }} 条</span>
      </footer>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import {
  CircleCheck,
  Clock,
  Location,
  Plus,
  Search,
  Star,
  StarFilled,
} from '@element-plus/icons-vue'

import { profileFavorites } from './profileMock'
import {
  profileTaskCategoryLabels,
  profileTaskStatusLabels,
  statusToneMap,
} from './profileLabels'
import type { TaskStatus } from '@/types'

const tabs: Array<{ label: string; value: 'ALL' | TaskStatus }> = [
  { label: '全部', value: 'ALL' },
  { label: '任务收藏', value: 'IN_PROGRESS' },
  { label: '最近收藏', value: 'COMPLETED' },
  { label: '即将截止', value: 'OFFLINE' },
]

const activeTab = ref<'ALL' | TaskStatus>('ALL')
const keyword = ref('')
const sortBy = ref('latest')
const page = ref(1)
const pageSize = 6

const stats = computed(() => [
  { label: '收藏总数', value: profileFavorites.length, icon: StarFilled, tone: 'blue' },
  { label: '本周新增', value: 6, icon: Plus, tone: 'purple' },
  { label: '即将截止', value: profileFavorites.filter((item) => item.status === 'OFFLINE').length || 3, icon: Clock, tone: 'orange' },
  { label: '已完成（从收藏）', value: profileFavorites.filter((item) => item.status === 'COMPLETED').length + 10, icon: CircleCheck, tone: 'green' },
])

const filteredTasks = computed(() => {
  const term = keyword.value.trim().toLowerCase()
  const rows = profileFavorites.filter((task) => {
    const matchTab = activeTab.value === 'ALL' || task.status === activeTab.value
    const matchKeyword = !term || task.title.toLowerCase().includes(term)
    return matchTab && matchKeyword
  })

  if (sortBy.value === 'reward') {
    return [...rows].sort((left, right) => right.reward - left.reward)
  }

  return rows
})

const pagedTasks = computed(() => {
  const start = (page.value - 1) * pageSize
  return filteredTasks.value.slice(start, start + pageSize)
})

watch([activeTab, keyword, sortBy], () => {
  page.value = 1
})
</script>

<style scoped>
.list-page {
  display: grid;
  gap: 16px;
}

.page-hero,
.summary-strip,
.table-card {
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.06);
}

.page-hero {
  display: flex;
  min-height: 132px;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 26px 38px;
  background: linear-gradient(120deg, #fff, #f3f6ff);
}

.page-hero p {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0;
  color: #4169e1;
  font-weight: 800;
}

.page-hero h1 {
  margin: 10px 0 8px;
  color: #111827;
  font-size: 30px;
}

.page-hero span {
  color: #64748b;
}

.hero-art {
  color: rgba(65, 105, 225, 0.18);
  font-size: 96px;
  font-weight: 800;
}

.summary-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  padding: 18px 24px;
}

.summary-strip article {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 18px;
  min-height: 74px;
}

.summary-strip article + article {
  border-left: 1px solid #e5ebf4;
}

.stat-icon {
  display: grid;
  width: 50px;
  height: 50px;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  font-size: 25px;
}

.stat-icon.blue,
.status-pill.blue {
  background: #eaf1ff;
  color: #4169e1;
}

.stat-icon.purple {
  background: #f1ecff;
  color: #6d5dfc;
}

.stat-icon.orange,
.status-pill.orange {
  background: #fff4e7;
  color: #f28c28;
}

.stat-icon.green,
.status-pill.green {
  background: #e9f8ef;
  color: #35a968;
}

.status-pill.red {
  background: #fff1f2;
  color: #e5484d;
}

.summary-strip small,
.summary-strip strong {
  display: block;
}

.summary-strip small {
  color: #64748b;
}

.summary-strip strong {
  margin-top: 4px;
  color: #111827;
  font-size: 24px;
}

.table-card {
  padding: 18px 22px;
}

.toolbar,
.tools,
.tabs {
  display: flex;
  align-items: center;
}

.toolbar {
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.tabs {
  gap: 8px;
}

.tabs button {
  min-height: 36px;
  padding: 0 18px;
  border: 1px solid transparent;
  border-radius: 8px;
  background: transparent;
  color: #475569;
  font: inherit;
  font-weight: 700;
  cursor: pointer;
}

.tabs button.active {
  background: #4169e1;
  color: #fff;
}

.tools {
  gap: 14px;
}

.tools :deep(.el-input) {
  width: 300px;
}

.sort-select {
  width: 140px;
}

.task-table {
  border: 1px solid #edf1f7;
  border-radius: 8px;
  overflow-x: auto;
}

.favorite-item {
  display: grid;
  min-width: 1040px;
  grid-template-columns: minmax(310px, 1.5fr) 120px 150px 150px 120px 160px;
  gap: 16px;
  align-items: center;
  padding: 13px 14px;
  border-bottom: 1px solid #edf1f7;
}

.favorite-item:last-child {
  border-bottom: 0;
}

.task-main {
  display: flex;
  min-width: 0;
  gap: 14px;
  align-items: center;
}

.task-image {
  display: grid;
  width: 62px;
  height: 62px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 8px;
  background: #f1f5f9;
  font-size: 32px;
}

.favorite-item h2 {
  margin: 0 0 8px;
  color: #111827;
  font-size: 16px;
}

.favorite-item p {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.favorite-item p span {
  padding: 4px 7px;
  border-radius: 6px;
  background: #eaf1ff;
  color: #4169e1;
  font-weight: 800;
}

.favorite-item small {
  display: block;
  color: #94a3b8;
  font-size: 12px;
}

.favorite-item strong {
  display: block;
  margin-top: 5px;
  color: #334155;
  font-size: 15px;
}

.money strong {
  color: #f97316;
}

.status-pill {
  display: inline-flex;
  width: max-content;
  min-height: 30px;
  align-items: center;
  justify-content: center;
  padding: 0 14px;
  border-radius: 8px;
  font-weight: 800;
}

.row-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.table-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding-top: 18px;
  color: #64748b;
}

@media (max-width: 900px) {
  .summary-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .summary-strip article + article {
    border-left: 0;
  }

  .toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .tools {
    align-items: stretch;
    flex-direction: column;
  }

  .tools :deep(.el-input),
  .sort-select {
    width: 100%;
  }

  .table-footer {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>

