<template>
  <article class="task-card">
    <div class="main">
      <div class="meta-line">
        <el-avatar :src="resolveAssetUrl(task.publisherAvatarUrl || '')" :size="34">
          {{ task.publisherName.slice(0, 1) }}
        </el-avatar>
        <strong>{{ task.publisherName }}</strong>
        <span>信用 {{ task.publisherCreditScore ?? 0 }}</span>
        <i>{{ taskCategoryLabels[task.category] }}</i>
        <time>{{ formatTime(task.createdAt) }}</time>
      </div>
      <h3>{{ task.title }}</h3>
      <p>
        <span>{{ task.description || '发布者没有补充更多描述。' }}</span>
        <em>{{ task.location || '校内待定地点' }}</em>
      </p>
    </div>

    <div class="price">¥{{ Number(task.reward).toFixed(2) }}</div>
    <span :class="['status-pill', statusTone(task.status)]">{{ taskStatusLabels[task.status] }}</span>

    <div class="actions">
      <button
        v-if="showFavorite"
        type="button"
        class="favorite-button"
        :class="{ active: task.favorited }"
        @click="$emit('favorite', task)"
      >
        {{ task.favorited ? '已收藏' : '收藏' }}
      </button>
      <el-button @click="$emit('view', task)">查看详情</el-button>
      <el-button
        v-if="showGrab"
        type="primary"
        :disabled="!isJoinableTask(task)"
        @click="$emit('grab', task)"
      >
        {{ task.category === 'TEAM_UP' && task.status === 'IN_PROGRESS' ? '申请加入' : task.status === 'OPEN' ? '立即抢单' : taskStatusLabels[task.status] }}
      </el-button>
    </div>
  </article>
</template>

<script setup lang="ts">
import { taskCategoryLabels, taskStatusLabels } from '@/types'
import type { TaskListDTO, TaskStatus } from '@/types'
import { resolveAssetUrl } from '@/utils/asset'

defineProps<{
  task: TaskListDTO
  showFavorite?: boolean
  showGrab?: boolean
}>()

defineEmits<{
  view: [task: TaskListDTO]
  grab: [task: TaskListDTO]
  favorite: [task: TaskListDTO]
}>()

const isJoinableTask = (task: TaskListDTO) =>
  task.status === 'OPEN' || (task.category === 'TEAM_UP' && task.status === 'IN_PROGRESS')

const statusTone = (status: TaskStatus) => {
  if (status === 'OPEN') return 'blue'
  if (status === 'IN_PROGRESS') return 'green'
  if (status === 'COMPLETED') return 'purple'
  if (status === 'OFFLINE') return 'gray'
  return 'orange'
}

const formatTime = (value: string) => {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}
</script>

<style scoped>
.task-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 120px 112px auto;
  gap: 16px;
  align-items: center;
  padding: 14px 16px;
  border: 1px solid #edf2f7;
  border-radius: 18px;
  background: #fff;
}

.main {
  min-width: 0;
}

.meta-line {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 9px;
  color: #64748b;
  font-size: 13px;
}

.meta-line strong {
  color: #334155;
  font-size: 14px;
}

.meta-line span,
.meta-line time {
  white-space: nowrap;
}

.meta-line i {
  flex: 0 0 auto;
  padding: 3px 7px;
  border-radius: 999px;
  background: #eff6ff;
  color: #1d4ed8;
  font-style: normal;
  font-weight: 800;
}

.main h3 {
  margin: 8px 0 6px;
  overflow: hidden;
  color: #111827;
  font-size: 18px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.main p {
  display: flex;
  min-width: 0;
  gap: 14px;
  margin: 0;
  color: #64748b;
}

.main p span,
.main p em {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.main p span {
  flex: 1 1 auto;
}

.main p em {
  flex: 0 1 220px;
  font-style: normal;
}

.price {
  color: #ea580c;
  font-size: 22px;
  font-weight: 800;
  text-align: center;
  white-space: nowrap;
}

.status-pill {
  justify-self: center;
  padding: 5px 10px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 800;
  white-space: nowrap;
}

.status-pill.blue {
  background: #eaf1ff;
  color: #1268ed;
}

.status-pill.green {
  background: #e9f8ef;
  color: #14935b;
}

.status-pill.orange {
  background: #fff7ed;
  color: #f97316;
}

.status-pill.purple {
  background: #f1ebff;
  color: #7c3aed;
}

.status-pill.gray {
  background: #f1f5f9;
  color: #64748b;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  white-space: nowrap;
}

.favorite-button {
  min-height: 32px;
  padding: 0 12px;
  border: 1px solid #dbe6f4;
  border-radius: 8px;
  background: #fff;
  color: #475569;
  font: inherit;
  cursor: pointer;
}

.favorite-button.active {
  border-color: #1d4ed8;
  background: #eaf1ff;
  color: #1d4ed8;
  font-weight: 700;
}

@media (max-width: 980px) {
  .task-card {
    grid-template-columns: minmax(0, 1fr) auto;
  }

  .status-pill {
    justify-self: end;
  }

  .actions {
    grid-column: 1 / -1;
    justify-content: flex-start;
  }
}
</style>
