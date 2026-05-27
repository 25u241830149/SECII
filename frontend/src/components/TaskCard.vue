<template>
  <article class="task-card">
    <header class="task-head">
      <div class="author">
        <el-avatar :src="resolveAssetUrl(task.publisherAvatarUrl || '')" :size="42">
          {{ task.publisherName.slice(0, 1) }}
        </el-avatar>
        <div>
          <strong>{{ task.publisherName }}</strong>
          <small>信用 {{ task.publisherCreditScore ?? 0 }}</small>
        </div>
      </div>
      <button
        v-if="showFavorite"
        type="button"
        class="favorite-button"
        :class="{ active: task.favorited }"
        @click="$emit('favorite', task)"
      >
        {{ task.favorited ? '已收藏' : '收藏' }}
      </button>
    </header>

    <div class="content">
      <div class="category-row">
        <span class="category-pill">{{ taskCategoryLabels[task.category] }}</span>
        <span class="meta">{{ formatTime(task.createdAt) }}</span>
      </div>
      <h3>{{ task.title }}</h3>
      <p>{{ task.description || '发布者没有补充更多描述。' }}</p>
    </div>

    <footer class="task-footer">
      <div class="footer-meta">
        <strong>¥{{ Number(task.reward).toFixed(2) }}</strong>
        <small>{{ task.location || '校内待定地点' }}</small>
      </div>
      <div class="actions">
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
    </footer>
  </article>
</template>

<script setup lang="ts">
import { taskCategoryLabels, taskStatusLabels } from '@/types'
import type { TaskListDTO } from '@/types'
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
  gap: 16px;
  padding: 18px;
  border: 1px solid #e7edf7;
  border-radius: 18px;
  background:
    radial-gradient(circle at top right, rgba(37, 99, 235, 0.08), transparent 35%),
    #fff;
  box-shadow: 0 18px 36px rgba(15, 23, 42, 0.08);
}

.task-head,
.author,
.task-footer,
.actions {
  display: flex;
  align-items: center;
}

.task-head,
.task-footer {
  justify-content: space-between;
  gap: 14px;
}

.author {
  gap: 12px;
}

.author strong,
.author small {
  display: block;
}

.author small,
.meta,
.content p,
.footer-meta small {
  color: #64748b;
}

.favorite-button {
  min-height: 34px;
  padding: 0 12px;
  border: 1px solid #dbe6f4;
  border-radius: 999px;
  background: #fff;
  color: #475569;
  font: inherit;
  cursor: pointer;
}

.favorite-button.active {
  border-color: #1d4ed8;
  background: #eaf1ff;
  color: #1d4ed8;
}

.category-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.category-pill {
  display: inline-flex;
  min-height: 30px;
  align-items: center;
  padding: 0 10px;
  border-radius: 999px;
  background: #eff6ff;
  color: #1d4ed8;
  font-size: 13px;
  font-weight: 700;
}

.content h3 {
  margin: 10px 0 8px;
  color: #111827;
  font-size: 20px;
}

.content p {
  margin: 0;
  line-height: 1.6;
}

.footer-meta strong,
.footer-meta small {
  display: block;
}

.footer-meta strong {
  color: #ea580c;
  font-size: 24px;
}

.actions {
  gap: 10px;
}

@media (max-width: 720px) {
  .task-head,
  .task-footer {
    align-items: flex-start;
    flex-direction: column;
  }

  .actions {
    width: 100%;
  }
}
</style>
