<template>
  <section class="orders-page">
    <article class="toolbar-card">
      <div>
        <h1>订单列表</h1>
        <p>按身份和状态筛选，查看当前抢单、确认、完成和取消流转。</p>
      </div>
      <div class="filters">
        <el-select v-model="role" @change="loadOrders">
          <el-option label="全部身份" value="all" />
          <el-option label="我是发布者" value="poster" />
          <el-option label="我是帮手" value="helper" />
        </el-select>
        <el-select v-model="status" @change="loadOrders">
          <el-option label="全部状态" value="all" />
          <el-option label="待确认" value="PENDING" />
          <el-option label="进行中" value="CONFIRMED" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
      </div>
    </article>

    <article class="list-card">
      <el-skeleton :loading="loading" animated :rows="6">
        <template #default>
          <el-empty v-if="!orders.length" description="暂无订单记录" />
          <div v-else class="order-list">
            <article v-for="order in orders" :key="order.orderId" class="order-row">
              <div class="main">
                <h2>{{ order.taskTitle }}</h2>
                <p>{{ taskCategoryLabels[order.taskCategory] }} · {{ order.taskLocation || '校内待定地点' }}</p>
                <small>发布者：{{ order.posterName }} · 帮手：{{ order.helperName }}</small>
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
import type { OrderListDTO, OrderStatus } from '@/types'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const orders = ref<OrderListDTO[]>([])
const page = ref(1)
const pageSize = 8
const total = ref(0)
const role = ref<'all' | 'poster' | 'helper'>('all')
const status = ref<'all' | OrderStatus>('all')

const loadOrders = async () => {
  if (!authStore.user) return
  loading.value = true
  try {
    const result = await getOrders({
      userId: authStore.user.userId,
      role: role.value === 'all' ? undefined : role.value,
      status: status.value === 'all' ? undefined : status.value,
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
.orders-page {
  display: grid;
  gap: 18px;
}

.toolbar-card,
.list-card {
  padding: 22px;
  border: 1px solid #e7edf7;
  border-radius: 24px;
  background: #fff;
  box-shadow: 0 18px 36px rgba(15, 23, 42, 0.08);
}

.toolbar-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.toolbar-card h1,
.toolbar-card p {
  margin: 0;
}

.toolbar-card p {
  margin-top: 8px;
  color: #64748b;
}

.filters {
  display: flex;
  gap: 12px;
}

.order-list {
  display: grid;
  gap: 14px;
}

.order-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 120px 110px 100px;
  gap: 16px;
  align-items: center;
  padding: 18px;
  border: 1px solid #edf2f7;
  border-radius: 18px;
}

.main h2,
.main p,
.main small {
  margin: 0;
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 18px;
  color: #64748b;
}

@media (max-width: 900px) {
  .toolbar-card,
  .order-row,
  .pager {
    align-items: flex-start;
    flex-direction: column;
  }

  .filters {
    width: 100%;
    flex-direction: column;
  }
}
</style>
