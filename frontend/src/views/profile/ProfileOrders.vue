<template>
  <section class="profile-page">
    <header class="page-head">
      <div>
        <h1>我的接单</h1>
        <p>当前账号作为帮手参与的订单</p>
      </div>
    </header>

    <article class="panel">
      <el-skeleton :loading="loading" animated :rows="6">
        <template #default>
          <el-empty v-if="!orders.length" description="还没有接单记录" />
          <div v-else class="order-list">
            <article v-for="order in orders" :key="order.orderId" class="order-row">
              <div class="main">
                <h2>{{ order.taskTitle }}</h2>
                <p>{{ taskCategoryLabels[order.taskCategory] }} · {{ order.taskLocation || '校内待定地点' }}</p>
                <small>发布者：{{ order.posterName }}</small>
              </div>
              <div class="price">¥{{ Number(order.reward).toFixed(2) }}</div>
              <OrderStatusBadge :status="order.status" />
              <el-button @click="router.push(`/orders/${order.orderId}`)">查看详情</el-button>
            </article>
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
          @current-change="loadOrders"
        />
      </div>
    </article>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { getOrders } from '@/api/order'
import OrderStatusBadge from '@/components/OrderStatusBadge.vue'
import { useAuthStore } from '@/stores'
import { taskCategoryLabels } from '@/types'
import type { OrderListDTO } from '@/types'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const orders = ref<OrderListDTO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 6

const loadOrders = async () => {
  if (!authStore.user) return
  loading.value = true
  try {
    const result = await getOrders({
      userId: authStore.user.userId,
      role: 'helper',
      page: page.value,
      size: pageSize,
    })
    orders.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

onMounted(loadOrders)
</script>

<style scoped>
.profile-page {
  display: grid;
  height: 100%;
  min-height: 0;
  grid-template-rows: auto minmax(0, 1fr);
  gap: 16px;
}

.page-head,
.panel {
  display: flex;
  min-height: 0;
  flex-direction: column;
  padding: 22px;
  border: 1px solid #e7edf7;
  border-radius: 24px;
  background: #fff;
  box-shadow: 0 18px 36px rgba(15, 23, 42, 0.08);
}

.panel :deep(.el-skeleton) {
  flex: 1 1 auto;
  min-height: 0;
  overflow: hidden;
}

.page-head h1,
.page-head p {
  margin: 0;
}

.page-head p {
  margin-top: 8px;
  color: #64748b;
}

.order-list {
  display: grid;
  gap: 14px;
  min-height: 0;
  overflow-y: auto;
  padding-right: 4px;
}

.order-list::-webkit-scrollbar {
  width: 8px;
}

.order-list::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: #cbd5e1;
}

.order-list::-webkit-scrollbar-track {
  background: transparent;
}

.order-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 120px 110px 100px;
  gap: 16px;
  align-items: center;
  padding: 14px 16px;
  border: 1px solid #edf2f7;
  border-radius: 18px;
}

.main h2,
.main p,
.main small {
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.main p,
.main small {
  margin-top: 6px;
  color: #64748b;
}

.price {
  color: #ea580c;
  font-size: 22px;
  font-weight: 700;
  text-align: center;
}

.pager {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 18px;
  color: #64748b;
}
</style>
