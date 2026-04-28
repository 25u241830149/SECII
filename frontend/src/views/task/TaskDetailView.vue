<template>
  <section class="detail-grid">
    <article class="detail-card">
      <div class="detail-header">
        <div>
          <h1 class="detail-title">{{ detail.title }}</h1>
          <div class="tag-row">
            <el-tag type="warning" effect="plain">{{ getCategoryLabel(detail.category) }}</el-tag>
            <el-tag effect="plain">赏金 ¥{{ detail.reward }}</el-tag>
            <el-tag effect="plain">状态 {{ detail.status }}</el-tag>
          </div>
        </div>
        <div class="action-row">
          <el-button type="primary" round @click="handleGrab">立即接单</el-button>
          <el-button round @click="router.push('/orders')">查看订单区</el-button>
        </div>
      </div>

      <p class="detail-copy">{{ detail.content }}</p>

      <div class="detail-meta">
        <span>发布人：{{ detail.publisherName }}</span>
        <span>信用分：{{ detail.publisherCredit }}</span>
        <span>发布时间：{{ detail.publishTime }}</span>
        <span>地点：{{ detail.location }}</span>
      </div>

      <div class="tag-row">
        <el-tag v-for="tag in detail.tags" :key="tag" effect="plain">{{ tag }}</el-tag>
      </div>
    </article>

    <article class="detail-card">
      <div class="summary-grid">
        <div class="stat-card">
          <div class="stat-label">任务编号</div>
          <div class="stat-value">{{ taskId }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">预估成交</div>
          <div class="stat-value">¥{{ detail.reward }}</div>
        </div>
      </div>
      <div class="kv-list">
        <span>交接提示：到宿舍前电话确认</span>
        <span>聊天入口：订单创建后自动打开</span>
        <span>评价入口：订单完成后跳转评价弹窗</span>
      </div>
    </article>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getCategoryLabel } from '@/constants/app'
import { mockTasks } from '@/constants/mock'
import { useAuthStore } from '@/stores/auth'
import type { TaskDetail } from '@/types'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const taskId = computed(() => Number(route.params.id) || mockTasks[0].taskId)

const detail = computed<TaskDetail>(() => {
  const task = mockTasks.find((item) => item.taskId === taskId.value) ?? mockTasks[0]
  return { ...task, content: `${task.description} 这里预留给订单流程、聊天入口和评价卡片的联动承接。`, tags: ['任务详情', '订单承接', '聊天沟通'] }
})

async function handleGrab() {
  if (!authStore.isLoggedIn) {
    await router.push({ name: 'login', query: { redirect: `/tasks/${taskId.value}` } })
    return
  }
  ElMessage.success('已进入接单流程骨架页')
  await router.push('/orders')
}
</script>