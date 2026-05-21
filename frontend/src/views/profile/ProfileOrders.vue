<template>
  <section class="orders-page">
    <header class="orders-hero">
      <div>
        <h1>我的接单</h1>
        <p>管理您接单的任务，查看历史记录与状态</p>
      </div>
      <div class="hero-art">☑</div>
    </header>

    <section class="order-stats">
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

    <section class="orders-card">
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
        <el-select v-model="sortBy" class="sort-select">
          <el-option label="最新接单" value="latest" />
          <el-option label="奖励最高" value="reward" />
        </el-select>
      </div>

      <div class="order-table">
        <article v-for="order in pagedOrders" :key="order.id" class="order-row">
          <div class="task-main">
            <div class="task-image">{{ order.image }}</div>
            <div>
              <h2>{{ order.title }}</h2>
              <p>
                <span>{{ profileTaskCategoryLabels[order.category] }}</span>
                发布者：{{ order.publisher }}
                <el-icon><Location /></el-icon>
                地点：{{ order.location }}
              </p>
            </div>
          </div>
          <div class="reward">
            <small>报酬：</small>
            <strong>￥{{ order.reward.toFixed(2) }}</strong>
          </div>
          <div class="time-cell">
            <span>接单时间：{{ order.acceptedAt }}</span>
            <span>截止时间：{{ order.deadline }}</span>
          </div>
          <span :class="['status-pill', statusToneMap[order.status]]">
            {{ profileOrderStatusLabels[order.status] }}
          </span>
          <div class="actions">
            <el-button>查看详情</el-button>
            <el-button v-if="order.status === 'CONFIRMED'" type="primary">联系发布者</el-button>
            <el-button v-else-if="order.status === 'COMPLETED'" type="primary">评价</el-button>
          </div>
        </article>
      </div>

      <footer class="table-footer">
        <span>共 {{ filteredOrders.length }} 条</span>
        <el-pagination
          v-model:current-page="page"
          layout="prev, pager, next"
          :page-size="pageSize"
          :total="filteredOrders.length"
        />
        <span>10 条/页</span>
      </footer>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { CircleCheck, CircleClose, Connection, Location, Memo } from '@element-plus/icons-vue'

import { profileOrders } from './profileMock'
import {
  profileOrderStatusLabels,
  profileTaskCategoryLabels,
  statusToneMap,
} from './profileLabels'
import type { OrderStatus } from '@/types'

const tabs: Array<{ label: string; value: 'ALL' | OrderStatus }> = [
  { label: '全部', value: 'ALL' },
  { label: '进行中', value: 'CONFIRMED' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' },
]

const activeTab = ref<'ALL' | OrderStatus>('ALL')
const sortBy = ref('latest')
const page = ref(1)
const pageSize = 6

const stats = computed(() => [
  { label: '接单总数', value: profileOrders.length + 30, icon: Memo, tone: 'blue' },
  { label: '进行中', value: profileOrders.filter((item) => item.status === 'CONFIRMED').length + 5, icon: Connection, tone: 'green' },
  { label: '已完成', value: profileOrders.filter((item) => item.status === 'COMPLETED').length + 24, icon: CircleCheck, tone: 'orange' },
  { label: '已取消', value: profileOrders.filter((item) => item.status === 'CANCELLED').length + 1, icon: CircleClose, tone: 'red' },
])

const filteredOrders = computed(() => {
  const rows = profileOrders.filter((order) => {
    return activeTab.value === 'ALL' || order.status === activeTab.value
  })

  if (sortBy.value === 'reward') {
    return [...rows].sort((left, right) => right.reward - left.reward)
  }

  return rows
})

const pagedOrders = computed(() => {
  const start = (page.value - 1) * pageSize
  return filteredOrders.value.slice(start, start + pageSize)
})

watch([activeTab, sortBy], () => {
  page.value = 1
})
</script>

<style scoped>
.orders-page {
  display: grid;
  gap: 18px;
}

.orders-hero,
.order-stats,
.orders-card {
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.06);
}

.orders-hero {
  display: flex;
  min-height: 126px;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 28px 38px;
  background: linear-gradient(120deg, #fff, #f3f7ff);
}

.orders-hero h1 {
  margin: 0 0 10px;
  color: #111827;
  font-size: 32px;
}

.orders-hero p {
  margin: 0;
  color: #64748b;
}

.hero-art {
  color: rgba(65, 105, 225, 0.22);
  font-size: 88px;
}

.order-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  padding: 20px 28px;
}

.order-stats article {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 18px;
  min-height: 72px;
}

.order-stats article + article {
  border-left: 1px solid #e5ebf4;
}

.stat-icon {
  display: grid;
  width: 54px;
  height: 54px;
  place-items: center;
  border-radius: 50%;
  font-size: 27px;
}

.stat-icon.blue,
.status-pill.blue {
  background: #eaf1ff;
  color: #4169e1;
}

.stat-icon.green,
.status-pill.green {
  background: #e9f8ef;
  color: #35a968;
}

.stat-icon.orange,
.status-pill.orange {
  background: #fff4e7;
  color: #f28c28;
}

.stat-icon.red,
.status-pill.red {
  background: #fff1f2;
  color: #e5484d;
}

.order-stats small,
.order-stats strong {
  display: block;
}

.order-stats small {
  color: #64748b;
  font-weight: 700;
}

.order-stats strong {
  margin-top: 6px;
  color: #111827;
  font-size: 25px;
}

.orders-card {
  padding: 18px 22px;
}

.toolbar,
.tabs,
.actions {
  display: flex;
  align-items: center;
}

.toolbar {
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.tabs {
  gap: 24px;
}

.tabs button {
  min-height: 38px;
  padding: 0 16px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: #475569;
  font: inherit;
  font-weight: 800;
  cursor: pointer;
}

.tabs button.active {
  background: #4169e1;
  color: #fff;
}

.sort-select {
  width: 140px;
}

.order-table {
  overflow-x: auto;
  border: 1px solid #edf1f7;
  border-radius: 8px;
}

.order-row {
  display: grid;
  min-width: 1120px;
  grid-template-columns: minmax(390px, 1.6fr) 120px 230px 110px 220px;
  gap: 16px;
  align-items: center;
  padding: 14px 18px;
  border-bottom: 1px solid #edf1f7;
}

.order-row:last-child {
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

.task-main h2 {
  margin: 0 0 8px;
  color: #111827;
  font-size: 16px;
}

.task-main p {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.task-main p span {
  padding: 4px 7px;
  border-radius: 6px;
  background: #eaf1ff;
  color: #4169e1;
  font-weight: 800;
}

.reward small,
.time-cell span {
  display: block;
  color: #64748b;
}

.reward strong {
  display: block;
  margin-top: 6px;
  color: #ef4444;
}

.time-cell {
  display: grid;
  gap: 6px;
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
  .order-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .order-stats article + article {
    border-left: 0;
  }

  .toolbar,
  .table-footer {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>

