<template>
  <section class="detail-page">
    <el-skeleton :loading="loading" animated :rows="8">
      <template #default>
        <article v-if="task" class="detail-card">
          <div class="headline">
            <div>
              <span class="category-pill">{{ taskCategoryLabels[task.category] }}</span>
              <h1>{{ task.title }}</h1>
              <p>{{ task.description }}</p>
            </div>
            <div class="reward-box">
              <strong>¥{{ Number(task.reward).toFixed(2) }}</strong>
              <small>{{ taskStatusLabels[task.status] }}</small>
            </div>
          </div>

          <section class="meta-grid">
            <div>
              <small>发布者</small>
              <strong>{{ task.publisherName }}</strong>
            </div>
            <div>
              <small>信用分</small>
              <strong>{{ task.publisherCreditScore ?? 0 }}</strong>
            </div>
            <div>
              <small>地点</small>
              <strong>{{ task.location || '校内待定地点' }}</strong>
            </div>
            <div>
              <small>收藏人数</small>
              <strong>{{ task.favoriteCount ?? 0 }}</strong>
            </div>
            <div v-if="task.category === 'TEAM_UP'">
              <small>组队进度</small>
              <strong>{{ teamProgressText }}</strong>
            </div>
            <div v-if="task.deadlineTime">
              <small>截止时间</small>
              <strong>{{ formatDateTime(task.deadlineTime) }}</strong>
            </div>
            <div v-if="task.activityTime">
              <small>活动时间</small>
              <strong>{{ formatDateTime(task.activityTime) }}</strong>
            </div>
          </section>

          <section v-if="task.category === 'SECOND_HAND' && task.itemImageUrl" class="image-box">
            <img :src="resolveAssetUrl(task.itemImageUrl)" alt="物品图片" />
            <div>
              <h2>交易信息</h2>
              <p>交易地点：{{ task.location || '校内待定地点' }}</p>
              <p v-if="task.originalPrice !== null && task.originalPrice !== undefined">
                物品原价：¥{{ Number(task.originalPrice).toFixed(2) }}
              </p>
            </div>
          </section>

          <section v-if="task.category === 'TEAM_UP'" class="location-box">
            <h2>组队说明</h2>
            <p>{{ task.activityNote || '发布者未补充活动要求。' }}</p>
            <p>组队进度：{{ teamProgressText }}</p>
          </section>

          <section class="location-box">
            <h2>位置与说明</h2>
            <p>{{ task.location || '任务未提供明确位置。' }}</p>
            <p v-if="task.longitude !== null && task.longitude !== undefined && task.latitude !== null && task.latitude !== undefined">
              经纬度：{{ task.longitude }}, {{ task.latitude }}
            </p>
          </section>

          <footer class="actions">
            <el-button @click="router.back()">返回</el-button>
            <el-button @click="toggleFavorite">
              {{ task.favorited ? '取消收藏' : '收藏任务' }}
            </el-button>
            <el-button v-if="canCancelTask" :loading="cancelling" @click="handleCancelTask">
              取消需求
            </el-button>
            <el-button type="primary" :disabled="!canGrabTask" @click="handleGrab">
              {{ task.category === 'TEAM_UP' && task.status === 'IN_PROGRESS' ? '申请加入' : task.status === 'OPEN' ? '立即抢单' : taskStatusLabels[task.status] }}
            </el-button>
          </footer>
        </article>
      </template>
    </el-skeleton>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'

import { grabOrder } from '@/api/order'
import { deleteTask, favoriteTask, getTaskDetail, unfavoriteTask } from '@/api/task'
import { useAuthStore } from '@/stores'
import { taskCategoryLabels, taskStatusLabels } from '@/types'
import { rememberTaskView } from '@/utils/taskRecommendation'
import { resolveAssetUrl } from '@/utils/asset'
import type { TaskDetailDTO } from '@/types'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const cancelling = ref(false)
const task = ref<TaskDetailDTO | null>(null)

const canGrabTask = computed(() => {
  const currentTask = task.value
  if (!currentTask || currentTask.publisherId === authStore.user?.userId) return false
  return currentTask.status === 'OPEN' || (currentTask.category === 'TEAM_UP' && currentTask.status === 'IN_PROGRESS')
})

const canCancelTask = computed(() => {
  const currentTask = task.value
  return Boolean(
    currentTask &&
      currentTask.publisherId === authStore.user?.userId &&
      currentTask.status !== 'COMPLETED' &&
      currentTask.status !== 'CANCELLED' &&
      currentTask.status !== 'OFFLINE',
  )
})

const teamProgressText = computed(() => {
  if (!task.value) return '-'
  return `${task.value.teamCurrentMembers ?? 0}/${task.value.teamTotalMembers ?? 0}人`
})

const ensureAuthenticated = () => {
  if (authStore.isAuthenticated) return true
  ElMessage.warning('请先登录后再继续')
  router.push('/login')
  return false
}

const loadTask = async () => {
  loading.value = true
  try {
    const detail = await getTaskDetail(Number(route.params.taskId))
    task.value = detail
    rememberTaskView(detail, authStore.user?.userId)
  } finally {
    loading.value = false
  }
}

const toggleFavorite = async () => {
  if (!task.value || !ensureAuthenticated()) return
  if (task.value.favorited) {
    await unfavoriteTask(task.value.taskId)
  } else {
    await favoriteTask(task.value.taskId)
  }
  await loadTask()
}

const handleGrab = async () => {
  if (!task.value || !ensureAuthenticated()) return
  await grabOrder({ taskId: task.value.taskId })
  ElMessage.success(task.value.category === 'TEAM_UP' ? '加入申请已提交' : '抢单成功')
  router.push('/orders')
}

const handleCancelTask = async () => {
  if (!task.value || !ensureAuthenticated()) return
  cancelling.value = true
  try {
    await deleteTask(task.value.taskId)
    ElMessage.success('需求已取消')
    await loadTask()
  } finally {
    cancelling.value = false
  }
}

const formatDateTime = (value: string) => {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

onMounted(loadTask)
</script>

<style scoped>
.detail-page {
  display: grid;
}

.detail-card {
  display: grid;
  gap: 22px;
  padding: 28px;
  border: 1px solid #e7edf7;
  border-radius: 24px;
  background: #fff;
  box-shadow: 0 22px 40px rgba(15, 23, 42, 0.08);
}

.headline {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 180px;
  gap: 20px;
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

.headline h1 {
  margin: 14px 0 10px;
  color: #111827;
  font-size: 34px;
}

.headline p,
.location-box p,
.meta-grid small,
.reward-box small {
  color: #64748b;
}

.reward-box {
  padding: 18px;
  border-radius: 20px;
  background: linear-gradient(145deg, #fff7ed, #fff);
  text-align: center;
}

.reward-box strong,
.reward-box small {
  display: block;
}

.reward-box strong {
  color: #ea580c;
  font-size: 34px;
}

.reward-box small {
  margin-top: 8px;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.meta-grid > div,
.location-box {
  padding: 18px;
  border-radius: 18px;
  background: #f8fbff;
}

.meta-grid strong,
.meta-grid small {
  display: block;
}

.meta-grid strong {
  margin-top: 8px;
  color: #111827;
}

.location-box h2 {
  margin: 0 0 10px;
}

.image-box {
  display: grid;
  grid-template-columns: minmax(220px, 320px) minmax(0, 1fr);
  gap: 18px;
  align-items: center;
  padding: 18px;
  border-radius: 18px;
  background: #f8fbff;
}

.image-box img {
  width: 100%;
  aspect-ratio: 4 / 3;
  border-radius: 12px;
  object-fit: cover;
}

.image-box h2,
.image-box p {
  margin: 0;
}

.image-box p {
  margin-top: 10px;
  color: #64748b;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 900px) {
  .headline {
    grid-template-columns: 1fr;
  }

  .meta-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .actions {
    flex-wrap: wrap;
    justify-content: flex-start;
  }
}
</style>
