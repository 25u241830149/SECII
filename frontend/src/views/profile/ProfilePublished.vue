<template>
  <section class="manage-page">
    <header class="plain-header">
      <div>
        <h1>我的发单</h1>
        <p>管理您发布的需求，查看进度与历史记录</p>
      </div>
      <div class="header-art">📋</div>
    </header>

    <section class="stat-bar">
      <article v-for="item in stats" :key="item.label">
        <span :class="['stat-badge', item.tone]">
          <el-icon><component :is="item.icon" /></el-icon>
        </span>
        <div>
          <small>{{ item.label }}</small>
          <strong>{{ item.value }}</strong>
        </div>
      </article>
    </section>

    <section class="manage-card">
      <div class="table-toolbar">
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
        <el-select v-model="sortBy" class="sort-select">
          <el-option label="最新发布" value="latest" />
          <el-option label="奖励最高" value="reward" />
        </el-select>
      </div>

      <div class="published-table">
        <div class="table-head">
          <span>任务信息</span>
          <span>奖励</span>
          <span>发布时间</span>
          <span>截止时间</span>
          <span>状态</span>
          <span>接单人数 / 进度</span>
          <span>操作</span>
        </div>

        <article v-for="task in pagedTasks" :key="task.id" class="published-row">
          <div class="task-cell">
            <div class="task-image">{{ task.image }}</div>
            <div>
              <h2>{{ task.title }}</h2>
              <span>{{ profileTaskCategoryLabels[task.category] }}</span>
            </div>
          </div>
          <strong class="reward">￥{{ task.reward.toFixed(2) }}</strong>
          <time>{{ task.createdAt }}</time>
          <time>{{ task.deadline }}</time>
          <span :class="['status-pill', statusToneMap[task.status]]">
            {{ profileTaskStatusLabels[task.status] }}
          </span>
          <div class="progress-cell">
            <b>{{ task.applicants }} / {{ task.capacity }} 人</b>
            <el-progress
              :percentage="progressOf(task)"
              :show-text="false"
              :color="task.status === 'IN_PROGRESS' ? '#f59e0b' : '#35b779'"
            />
          </div>
          <div class="actions">
            <el-button>查看详情</el-button>
            <el-button>{{ task.status === 'IN_PROGRESS' ? '编辑' : '重新发布' }}</el-button>
            <el-button :icon="More" />
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
        <span>10 条/页</span>
      </footer>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Briefcase, CircleCheck, CircleClose, More, Timer } from '@element-plus/icons-vue'

import type { ProfileTaskItem } from './profileTypes'
import { profilePublishedTasks } from './profileMock'
import {
  profileTaskCategoryLabels,
  profileTaskStatusLabels,
  statusToneMap,
} from './profileLabels'
import type { TaskStatus } from '@/types'

const tabs: Array<{ label: string; value: 'ALL' | TaskStatus }> = [
  { label: '全部', value: 'ALL' },
  { label: '进行中', value: 'IN_PROGRESS' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' },
]

const activeTab = ref<'ALL' | TaskStatus>('ALL')
const sortBy = ref('latest')
const page = ref(1)
const pageSize = 6

const stats = computed(() => [
  { label: '总发单数', value: profilePublishedTasks.length + 6, icon: Briefcase, tone: 'blue' },
  { label: '进行中', value: profilePublishedTasks.filter((item) => item.status === 'IN_PROGRESS').length + 2, icon: Timer, tone: 'orange' },
  { label: '已完成', value: profilePublishedTasks.filter((item) => item.status === 'COMPLETED').length + 4, icon: CircleCheck, tone: 'green' },
  { label: '已取消', value: profilePublishedTasks.filter((item) => item.status === 'CANCELLED').length, icon: CircleClose, tone: 'red' },
])

const filteredTasks = computed(() => {
  const rows = profilePublishedTasks.filter((task) => {
    return activeTab.value === 'ALL' || task.status === activeTab.value
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

const progressOf = (task: ProfileTaskItem) => {
  if (!task.capacity) return 0
  return Math.min(100, Math.round(((task.applicants || 0) / task.capacity) * 100))
}

watch([activeTab, sortBy], () => {
  page.value = 1
})
</script>

<style scoped>
.manage-page {
  display: grid;
  gap: 18px;
}

.plain-header,
.stat-bar,
.manage-card {
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.06);
}

.plain-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 126px;
  padding: 28px 38px;
  background: linear-gradient(120deg, #fff, #f3f7ff);
}

.plain-header h1 {
  margin: 0 0 10px;
  color: #111827;
  font-size: 32px;
}

.plain-header p {
  margin: 0;
  color: #64748b;
}

.header-art {
  color: rgba(65, 105, 225, 0.22);
  font-size: 84px;
}

.stat-bar {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  padding: 20px 28px;
}

.stat-bar article {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 18px;
  min-height: 72px;
}

.stat-bar article + article {
  border-left: 1px solid #e5ebf4;
}

.stat-badge {
  display: grid;
  width: 56px;
  height: 56px;
  place-items: center;
  border-radius: 50%;
  font-size: 28px;
}

.stat-badge.blue,
.status-pill.blue {
  background: #eaf1ff;
  color: #4169e1;
}

.stat-badge.orange,
.status-pill.orange {
  background: #fff4e7;
  color: #f28c28;
}

.stat-badge.green,
.status-pill.green {
  background: #e9f8ef;
  color: #35a968;
}

.stat-badge.red,
.status-pill.red {
  background: #fff1f2;
  color: #e5484d;
}

.stat-bar small,
.stat-bar strong {
  display: block;
}

.stat-bar small {
  color: #64748b;
  font-weight: 700;
}

.stat-bar strong {
  margin-top: 6px;
  color: #111827;
  font-size: 25px;
}

.manage-card {
  padding: 18px 22px;
}

.table-toolbar,
.tabs,
.actions {
  display: flex;
  align-items: center;
}

.table-toolbar {
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.tabs {
  gap: 8px;
}

.tabs button {
  min-height: 38px;
  padding: 0 18px;
  border: 1px solid #dbe3ef;
  border-radius: 8px;
  background: #fff;
  color: #475569;
  font: inherit;
  font-weight: 700;
  cursor: pointer;
}

.tabs button.active {
  border-color: #4169e1;
  background: #4169e1;
  color: #fff;
}

.sort-select {
  width: 140px;
}

.published-table {
  overflow-x: auto;
  border: 1px solid #edf1f7;
  border-radius: 8px;
}

.table-head,
.published-row {
  display: grid;
  min-width: 1120px;
  grid-template-columns: minmax(280px, 1.5fr) 110px 150px 150px 110px 160px 240px;
  gap: 14px;
  align-items: center;
}

.table-head {
  padding: 14px 18px;
  background: #f8fbff;
  color: #475569;
  font-weight: 800;
}

.published-row {
  padding: 14px 18px;
  border-top: 1px solid #edf1f7;
}

.task-cell {
  display: flex;
  min-width: 0;
  gap: 14px;
  align-items: center;
}

.task-image {
  display: grid;
  width: 52px;
  height: 52px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 8px;
  background: #f1f5f9;
  font-size: 28px;
}

.task-cell h2 {
  margin: 0 0 6px;
  color: #111827;
  font-size: 16px;
}

.task-cell span {
  padding: 4px 7px;
  border-radius: 6px;
  background: #eaf1ff;
  color: #4169e1;
  font-size: 13px;
  font-weight: 800;
}

.reward {
  color: #f97316;
}

.published-row time,
.progress-cell {
  color: #475569;
}

.status-pill {
  display: inline-flex;
  width: max-content;
  min-height: 28px;
  align-items: center;
  padding: 0 12px;
  border-radius: 8px;
  font-weight: 800;
}

.progress-cell b {
  display: block;
  margin-bottom: 8px;
  color: #334155;
}

.actions {
  justify-content: flex-end;
  gap: 8px;
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
  .stat-bar {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .stat-bar article + article {
    border-left: 0;
  }

  .table-toolbar,
  .table-footer {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>

