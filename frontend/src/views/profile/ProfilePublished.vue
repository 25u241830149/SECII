<template>
  <section class="profile-page">
    <header class="page-head">
      <div>
        <h1>我的发布</h1>
        <p>这里展示当前账号发布过的任务，筛选和详情跳转使用 Sprint2 任务接口。</p>
      </div>
    </header>

    <article class="panel">
      <el-skeleton :loading="loading" animated :rows="6">
        <template #default>
          <el-empty v-if="!tasks.length" description="还没有发布任何任务" />
          <div v-else class="task-list">
            <TaskCard
              v-for="task in tasks"
              :key="task.taskId"
              :task="task"
              :show-favorite="false"
              :show-grab="false"
              @view="router.push(`/tasks/${task.taskId}`)"
            />
          </div>
        </template>
      </el-skeleton>

      <div class="pager">
        <span>共 {{ total }} 条</span>
        <el-pagination
          v-model:current-page="page"
          layout="prev, pager, next"
          :page-size="pageSize"
          :total="total"
          @current-change="loadTasks"
        />
      </div>
    </article>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { getPublishedTasks } from '@/api/task'
import TaskCard from '@/components/TaskCard.vue'
import { useAuthStore } from '@/stores'
import type { TaskListDTO } from '@/types'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const tasks = ref<TaskListDTO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 6

const loadTasks = async () => {
  if (!authStore.user) return
  loading.value = true
  try {
    const result = await getPublishedTasks({
      userId: authStore.user.userId,
      page: page.value,
      size: pageSize,
    })
    tasks.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

onMounted(loadTasks)
</script>

<style scoped>
.profile-page {
  display: grid;
  gap: 16px;
}

.page-head,
.panel {
  padding: 22px;
  border: 1px solid #e7edf7;
  border-radius: 24px;
  background: #fff;
  box-shadow: 0 18px 36px rgba(15, 23, 42, 0.08);
}

.page-head h1,
.page-head p {
  margin: 0;
}

.page-head p {
  margin-top: 8px;
  color: #64748b;
}

.task-list {
  display: grid;
  gap: 14px;
}

.pager {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 18px;
  color: #64748b;
}
</style>
