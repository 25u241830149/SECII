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
            <el-button v-if="isParticipant" @click="goReport">举报此订单</el-button>
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

        <ChatPanel v-if="order && isParticipant" :order-id="order.orderId" />

        <section v-if="order?.status === 'COMPLETED'" class="review-area">
          <ReviewForm
            v-if="canReview && reviewTarget"
            :order-id="order.orderId"
            :target-user-id="reviewTarget.userId"
            :target-user-name="reviewTarget.name"
            @submitted="handleReviewSubmitted"
          />
          <ReviewList :reviews="reviews" />
        </section>
      </template>
    </el-skeleton>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'

import { cancelOrder, completeOrder, confirmOrder, getOrderDetail } from '@/api/order'
import { getOrderReviews } from '@/api/review'
import ChatPanel from '@/components/ChatPanel.vue'
import OrderStatusBadge from '@/components/OrderStatusBadge.vue'
import ReviewForm from '@/components/ReviewForm.vue'
import ReviewList from '@/components/ReviewList.vue'
import { useAuthStore } from '@/stores'
import { taskCategoryLabels } from '@/types'
import type { OrderDetailDTO, ReviewDTO } from '@/types'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const submitting = ref(false)
const order = ref<OrderDetailDTO | null>(null)
const reviews = ref<ReviewDTO[]>([])

const currentUserId = computed(() => authStore.user?.userId)
const isPoster = computed(() => order.value?.posterId === currentUserId.value)
const isHelper = computed(() => order.value?.helperId === currentUserId.value)
const isParticipant = computed(() => Boolean(isPoster.value || isHelper.value))

const reviewTarget = computed(() => {
  if (!order.value) return null
  if (isPoster.value) {
    return { userId: order.value.helperId, name: order.value.helperName }
  }
  if (isHelper.value) {
    return { userId: order.value.posterId, name: order.value.posterName }
  }
  return null
})

const hasSubmittedReview = computed(() =>
  reviews.value.some((review) => review.reviewerId === currentUserId.value),
)

const canConfirm = computed(
  () => order.value?.status === 'PENDING' && isPoster.value,
)
const canComplete = computed(
  () => order.value?.status === 'CONFIRMED' && isParticipant.value,
)
const canCancel = computed(
  () =>
    order.value !== null &&
    (order.value.status === 'PENDING' || order.value.status === 'CONFIRMED') &&
    isParticipant.value,
)
const canReview = computed(
  () => order.value?.status === 'COMPLETED' && isParticipant.value && !hasSubmittedReview.value,
)

const loadReviews = async () => {
  if (!order.value || order.value.status !== 'COMPLETED') {
    reviews.value = []
    return
  }
  reviews.value = await getOrderReviews(order.value.orderId)
}

const loadOrder = async () => {
  loading.value = true
  try {
    order.value = await getOrderDetail(Number(route.params.orderId))
    await loadReviews()
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

const handleReviewSubmitted = (review: ReviewDTO) => {
  reviews.value = [review, ...reviews.value]
}

const goReport = () => {
  if (!order.value || !reviewTarget.value) return
  router.push({
    name: 'report-create',
    query: {
      targetType: 'ORDER',
      targetId: String(order.value.orderId),
      targetUserId: String(reviewTarget.value.userId),
    },
  })
}

onMounted(loadOrder)
</script>

<style scoped>
.order-detail-page {
  display: grid;
  gap: 18px;
}

.order-detail-card {
  display: grid;
  gap: 20px;
  padding: 28px;
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.07);
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

.top h1 {
  color: #111827;
  font-size: 26px;
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
  border-radius: 8px;
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
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 12px;
}

.review-area {
  display: grid;
  grid-template-columns: minmax(280px, 360px) minmax(0, 1fr);
  gap: 18px;
}

@media (max-width: 900px) {
  .top,
  .review-area {
    grid-template-columns: 1fr;
  }

  .top-side {
    justify-items: start;
  }

  .info-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .actions {
    justify-content: flex-start;
  }
}
</style>
