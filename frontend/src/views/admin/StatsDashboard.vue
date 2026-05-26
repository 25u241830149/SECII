<template>
  <section class="admin-page">
    <el-skeleton :loading="loading" animated :rows="8">
      <template #default>
        <section class="stat-grid">
          <article v-for="item in statItems" :key="item.label" class="stat-card">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <small>{{ item.hint }}</small>
          </article>
        </section>

        <section class="summary-panel">
          <article>
            <h2>订单完成率</h2>
            <strong>{{ completionRate }}%</strong>
            <p>{{ stats?.completedOrders || 0 }} / {{ stats?.totalOrders || 0 }} 个订单已完成</p>
          </article>
          <article>
            <h2>平均评价</h2>
            <strong>{{ averageRating }}</strong>
            <p>基于 {{ stats?.totalReviews || 0 }} 条评价计算</p>
          </article>
          <article>
            <h2>待处理压力</h2>
            <strong>{{ stats?.pendingReports || 0 }}</strong>
            <p>{{ stats?.pendingVerifications || 0 }} 个认证待审</p>
          </article>
        </section>
      </template>
    </el-skeleton>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import { getDashboardStats } from '@/api/admin'
import type { AdminDashboardStatsDTO } from '@/types'

const loading = ref(false)
const stats = ref<AdminDashboardStatsDTO | null>(null)

const statItems = computed(() => {
  const data = stats.value
  return [
    { label: '用户总数', value: data?.totalUsers || 0, hint: `${data?.bannedUsers || 0} 个封禁账号` },
    { label: '待审认证', value: data?.pendingVerifications || 0, hint: '需要管理员复核' },
    { label: '开放任务', value: data?.openTasks || 0, hint: `${data?.inProgressTasks || 0} 个进行中` },
    { label: '已完成任务', value: data?.completedTasks || 0, hint: '历史任务沉淀' },
    { label: '订单总数', value: data?.totalOrders || 0, hint: `${data?.completedOrders || 0} 个已完成` },
    { label: '待处理举报', value: data?.pendingReports || 0, hint: `${data?.totalReports || 0} 条累计举报` },
    { label: '未读消息', value: data?.unreadMessages || 0, hint: '站内通知积压' },
    { label: '评价总数', value: data?.totalReviews || 0, hint: `平均 ${averageRating.value}` },
  ]
})

const completionRate = computed(() => {
  if (!stats.value?.totalOrders) return 0
  return Math.round((stats.value.completedOrders / stats.value.totalOrders) * 100)
})

const averageRating = computed(() => {
  const value = Number(stats.value?.averageRating || 0)
  return value ? value.toFixed(1) : '暂无'
})

const loadStats = async () => {
  loading.value = true
  try {
    stats.value = await getDashboardStats()
  } finally {
    loading.value = false
  }
}

onMounted(loadStats)
</script>

<style scoped>
.admin-page {
  display: grid;
  gap: 18px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.stat-card,
.summary-panel article {
  padding: 20px;
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 26px rgba(15, 23, 42, 0.06);
}

.stat-card {
  display: grid;
  gap: 8px;
}

.stat-card span,
.stat-card small,
.summary-panel p {
  color: #64748b;
}

.stat-card strong {
  color: #111827;
  font-size: 30px;
}

.summary-panel {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.summary-panel h2,
.summary-panel p {
  margin: 0;
}

.summary-panel h2 {
  color: #111827;
  font-size: 18px;
}

.summary-panel strong {
  display: block;
  margin: 12px 0 8px;
  color: #1268ed;
  font-size: 32px;
}

@media (max-width: 1100px) {
  .stat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .summary-panel {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .stat-grid {
    grid-template-columns: 1fr;
  }
}
</style>
