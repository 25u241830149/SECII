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

        <section class="chart-grid">
          <article class="chart-card completion-chart">
            <header>
              <h2>订单完成分布</h2>
              <span>{{ stats?.completedOrders || 0 }} / {{ stats?.totalOrders || 0 }}</span>
            </header>
            <div class="completion-body">
              <div class="donut" :style="orderRingStyle">
                <strong>{{ completionRate }}%</strong>
                <span>完成率</span>
              </div>
              <p>用于观察平台订单闭环情况，完成率越高说明履约链路越稳定。</p>
            </div>
          </article>

          <article class="chart-card">
            <header>
              <h2>任务状态概览</h2>
              <span>{{ taskTotal }} 个任务</span>
            </header>
            <div class="bar-list">
              <div v-for="row in taskStatusRows" :key="row.label" class="bar-row">
                <div class="bar-meta">
                  <span>{{ row.label }}</span>
                  <strong>{{ row.value }}</strong>
                </div>
                <div class="bar-track">
                  <i :class="row.tone" :style="{ width: row.percent + '%' }"></i>
                </div>
              </div>
            </div>
          </article>

          <article class="chart-card">
            <header>
              <h2>处理压力</h2>
              <span>{{ pressureTotal }} 项</span>
            </header>
            <div class="bar-list">
              <div v-for="row in pressureRows" :key="row.label" class="bar-row">
                <div class="bar-meta">
                  <span>{{ row.label }}</span>
                  <strong>{{ row.value }}</strong>
                </div>
                <div class="bar-track">
                  <i :class="row.tone" :style="{ width: row.percent + '%' }"></i>
                </div>
              </div>
            </div>
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

const taskTotal = computed(() => {
  const data = stats.value
  return (data?.openTasks || 0) + (data?.inProgressTasks || 0) + (data?.completedTasks || 0)
})

const pressureTotal = computed(() => {
  const data = stats.value
  return (data?.pendingReports || 0) + (data?.pendingVerifications || 0) + (data?.unreadMessages || 0)
})

const percentOf = (value: number, total: number) => (total ? Math.round((value / total) * 100) : 0)

const orderRingStyle = computed(() => ({
  '--progress': `${completionRate.value}%`,
}))

const taskStatusRows = computed(() => {
  const data = stats.value
  const total = taskTotal.value
  return [
    { label: '开放任务', value: data?.openTasks || 0, percent: percentOf(data?.openTasks || 0, total), tone: 'blue' },
    { label: '进行中', value: data?.inProgressTasks || 0, percent: percentOf(data?.inProgressTasks || 0, total), tone: 'green' },
    { label: '已完成', value: data?.completedTasks || 0, percent: percentOf(data?.completedTasks || 0, total), tone: 'orange' },
  ]
})

const pressureRows = computed(() => {
  const data = stats.value
  const total = pressureTotal.value
  return [
    { label: '待处理举报', value: data?.pendingReports || 0, percent: percentOf(data?.pendingReports || 0, total), tone: 'orange' },
    { label: '认证待审', value: data?.pendingVerifications || 0, percent: percentOf(data?.pendingVerifications || 0, total), tone: 'blue' },
    { label: '未读消息', value: data?.unreadMessages || 0, percent: percentOf(data?.unreadMessages || 0, total), tone: 'green' },
  ]
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
.chart-card,
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

.chart-grid {
  display: grid;
  grid-template-columns: 1.15fr 1fr 1fr;
  gap: 14px;
}

.chart-card {
  display: grid;
  gap: 18px;
}

.chart-card header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.chart-card h2,
.chart-card p {
  margin: 0;
}

.chart-card h2 {
  color: #111827;
  font-size: 18px;
}

.chart-card header span,
.chart-card p,
.bar-meta span {
  color: #64748b;
}

.completion-body {
  display: grid;
  grid-template-columns: 124px minmax(0, 1fr);
  align-items: center;
  gap: 20px;
}

.donut {
  display: grid;
  width: 124px;
  height: 124px;
  place-items: center;
  border-radius: 50%;
  background:
    radial-gradient(circle, #fff 0 55%, transparent 56%),
    conic-gradient(#1268ed var(--progress), #e8eef7 0);
}

.donut strong,
.donut span {
  grid-area: 1 / 1;
}

.donut strong {
  transform: translateY(-8px);
  color: #1268ed;
  font-size: 28px;
}

.donut span {
  transform: translateY(24px);
  color: #64748b;
  font-size: 13px;
}

.bar-list {
  display: grid;
  gap: 16px;
}

.bar-row {
  display: grid;
  gap: 8px;
}

.bar-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.bar-meta strong {
  color: #111827;
}

.bar-track {
  height: 10px;
  overflow: hidden;
  border-radius: 999px;
  background: #eef2f7;
}

.bar-track i {
  display: block;
  height: 100%;
  min-width: 4px;
  border-radius: inherit;
}

.bar-track .blue {
  background: #1268ed;
}

.bar-track .green {
  background: #21b485;
}

.bar-track .orange {
  background: #ff8a1f;
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

  .chart-grid {
    grid-template-columns: 1fr;
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
