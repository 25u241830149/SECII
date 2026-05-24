<template>
  <section class="order-detail-page">
    <el-skeleton :loading="loading" animated :rows="8">
      <template #default>
        <article v-if="order" class="order-detail-card">
          <header class="top">
            <div>
              <h1>{{ order.taskTitle }}</h1>
              <p>{{ order.taskDescription }}</p>
            </div>
            <div class="top-side">
              <OrderStatusBadge :status="order.status" />
              <strong>¥{{ Number(order.reward).toFixed(2) }}</strong>
            </div>
          </header>

          <section class="info-grid">
            <div>
              <small>任务分类</small>
              <strong>{{ taskCategoryLabels[order.taskCategory] }}</strong>
            </div>
            <div>
              <small>任务地点</small>
              <strong>{{ order.taskLocation || '校内待定地点' }}</strong>
            </div>
            <div>
              <small>发布者</small>
              <strong>{{ order.posterName }} / {{ order.posterCreditScore ?? 0 }}</strong>
            </div>
            <div>
              <small>帮手</small>
              <strong>{{ order.helperName }} / {{ order.helperCreditScore ?? 0 }}</strong>
            </div>
          </section>

          <footer class="actions">
            <el-button @click="router.back()">返回</el-button>
            <el-button v-if="canConfirm" type="primary" :loading="submitting" @click="runAction('confirm')">
              确认订单
            </el-button>
            <el-button v-if="canComplete" type="primary" :loading="submitting" @click="runAction('complete')">
              完成订单
            </el-button>
            <el-button v-if="canCancel" :loading="submitting" @click="runAction('cancel')">
              取消订单
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

import { cancelOrder, completeOrder, confirmOrder, getOrderDetail } from '@/api/order'
import OrderStatusBadge from '@/components/OrderStatusBadge.vue'
import { useAuthStore } from '@/stores'
import { taskCategoryLabels } from '@/types'
import type { OrderDetailDTO } from '@/types'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const submitting = ref(false)
const order = ref<OrderDetailDTO | null>(null)

const canConfirm = computed(
  () =>
    order.value?.status === 'PENDING' &&
    authStore.user?.userId === order.value.posterId,
)
const canComplete = computed(
  () =>
    order.value?.status === 'CONFIRMED' &&
    (authStore.user?.userId === order.value.posterId || authStore.user?.userId === order.value.helperId),
)
const canCancel = computed(
  () =>
    order.value !== null &&
    (order.value.status === 'PENDING' || order.value.status === 'CONFIRMED'),
)

const loadOrder = async () => {
  loading.value = true
  try {
    order.value = await getOrderDetail(Number(route.params.orderId))
  } finally {
    loading.value = false
  }
}

const runAction = async (action: 'confirm' | 'complete' | 'cancel') => {
  if (!order.value) return
  submitting.value = true
  try {
    if (action === 'confirm') {
      await confirmOrder(order.value.orderId)
      ElMessage.success('订单已确认')
    } else if (action === 'complete') {
      await completeOrder(order.value.orderId)
      ElMessage.success('订单已完成')
    } else {
      await cancelOrder(order.value.orderId)
      ElMessage.success('订单已取消')
    }
    await loadOrder()
  } finally {
    submitting.value = false
  }
}

onMounted(loadOrder)
</script>

<style scoped>
.order-detail-page {
  display: grid;
}

.order-detail-card {
  display: grid;
  gap: 20px;
  padding: 28px;
  border: 1px solid #e7edf7;
  border-radius: 24px;
  background: #fff;
  box-shadow: 0 22px 42px rgba(15, 23, 42, 0.08);
}

.top {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 160px;
  gap: 20px;
}

.top h1,
.top p {
  margin: 0;
}

.top p {
  margin-top: 10px;
  color: #64748b;
  line-height: 1.6;
}

.top-side {
  display: grid;
  justify-items: end;
  align-content: start;
  gap: 12px;
}

.top-side strong {
  color: #ea580c;
  font-size: 30px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.info-grid > div {
  padding: 18px;
  border-radius: 18px;
  background: #f8fbff;
}

.info-grid small,
.info-grid strong {
  display: block;
}

.info-grid small {
  color: #64748b;
}

.info-grid strong {
  margin-top: 8px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 900px) {
  .top {
    grid-template-columns: 1fr;
  }

  .top-side {
    justify-items: start;
  }

  .info-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .actions {
    flex-wrap: wrap;
    justify-content: flex-start;
  }
}
</style>
