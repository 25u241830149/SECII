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
              <OrderStatusBadge
                :status="order.status"
                :label="displayStatusLabel"
                :tone-override="displayStatusTone"
              />
              <strong>{{ isTeamUp ? '组队需求' : `¥${Number(order.reward).toFixed(2)}` }}</strong>
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
              <small>{{ isTeamUp ? '组队发起人' : '发布者' }}</small>
              <strong>{{ order.posterName }} / {{ order.posterCreditScore ?? 0 }}</strong>
            </div>
            <div>
              <small>{{ isTeamUp ? '申请同学' : '帮手' }}</small>
              <strong>{{ order.helperName }} / {{ order.helperCreditScore ?? 0 }}</strong>
            </div>
            <div v-if="order.taskCategory === 'TEAM_UP'">
              <small>组队进度</small>
              <strong>{{ order.teamCurrentMembers ?? 0 }}/{{ order.teamTotalMembers ?? 0 }}人</strong>
            </div>
          </section>

          <footer class="actions">
            <el-button @click="goBack">返回</el-button>
            <el-button v-if="isParticipant" @click="goReport">{{ isTeamUp ? '举报此组队' : '举报此订单' }}</el-button>
            <el-button v-if="canConfirm" type="primary" :loading="submitting" @click="runAction('confirm')">
              {{ isTeamUp ? '通过申请' : '确认订单' }}
            </el-button>
            <el-button v-if="canConfirm" :loading="submitting" @click="runAction('reject')">
              {{ isTeamUp ? '拒绝申请' : '拒绝接单' }}
            </el-button>
            <el-button v-if="canComplete" type="primary" :loading="submitting" @click="runAction('complete')">
              完成订单
            </el-button>
            <el-button v-if="canCancel" :loading="submitting" @click="runAction('cancel')">
              {{ isTeamUp ? '取消组队' : '取消订单' }}
            </el-button>
            <el-button v-if="canAbandon" type="danger" plain :loading="submitting" @click="runAction('abandon')">
              {{ teamUpAbandonText }}
            </el-button>
          </footer>
        </article>
        <div v-if="order" class="discussion-grid">
          <TaskCommentSection :task-id="order.taskId" />
          <ChatPanel
            v-if="isParticipant"
            :order-id="order.orderId"
            :team-up="isTeamUp"
            :disabled="chatClosed"
            :disabled-reason="chatClosedReason"
          />
        </div>

        <section
          v-if="order?.status === 'COMPLETED' && order.taskCategory !== 'TEAM_UP'"
          class="review-area"
          :class="{ 'review-area-single': !canReview || !reviewTarget }"
        >
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

import { abandonOrder, cancelOrder, completeOrder, confirmOrder, getOrderDetail, rejectOrder } from '@/api/order'
import { getOrderReviews } from '@/api/review'
import ChatPanel from '@/components/ChatPanel.vue'
import OrderStatusBadge from '@/components/OrderStatusBadge.vue'
import ReviewForm from '@/components/ReviewForm.vue'
import ReviewList from '@/components/ReviewList.vue'
import TaskCommentSection from '@/components/TaskCommentSection.vue'
import { useAuthStore } from '@/stores'
import { taskCategoryLabels, taskStatusLabels } from '@/types'
import type { OrderDetailDTO, ReviewDTO } from '@/types'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const submitting = ref(false)
const order = ref<OrderDetailDTO | null>(null)
const reviews = ref<ReviewDTO[]>([])

const currentUserId = computed(() => authStore.user?.userId)
const isSameUser = (userId?: number | null) => (
  userId !== null &&
  userId !== undefined &&
  currentUserId.value !== null &&
  currentUserId.value !== undefined &&
  String(userId) === String(currentUserId.value)
)
const isPoster = computed(() => isSameUser(order.value?.posterId))
const isHelper = computed(() => isSameUser(order.value?.helperId))
const isParticipant = computed(() => Boolean(isPoster.value || isHelper.value))
const isTeamUp = computed(() => order.value?.taskCategory === 'TEAM_UP')
const displayStatusLabel = computed(() => (
  order.value?.status === 'CANCELLED' && order.value.taskStatus === 'OPEN' && isPoster.value
    ? taskStatusLabels.OPEN
    : undefined
))
const displayStatusTone = computed(() => (displayStatusLabel.value ? 'blue' : undefined))
const teamUpAbandonText = computed(() => {
  if (!isTeamUp.value) return '放弃接单'
  return order.value?.status === 'PENDING' ? '撤回申请' : '退出队伍'
})
const chatClosed = computed(() => order.value?.status === 'CANCELLED')
const chatClosedReason = computed(() => {
  if (!chatClosed.value) return ''
  if (isTeamUp.value) {
    if (isHelper.value) return '你的组队申请未通过，队伍沟通渠道已关闭，无法继续发送消息。'
    if (isPoster.value) return '该组队申请已结束，队伍沟通渠道已关闭，无法继续发送消息。'
    return '该组队沟通渠道已关闭，无法继续发送消息。'
  }
  if (order.value?.taskStatus === 'OPEN') {
    if (isHelper.value) return '你的接单已被发布者拒绝，订单沟通渠道已关闭，无法继续发送消息。'
    if (isPoster.value) return '你已拒绝该接单，订单沟通渠道已关闭，无法继续发送消息。'
  }
  return '该订单已取消，订单沟通渠道已关闭，无法继续发送消息。'
})

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
  reviews.value.some((review) => isSameUser(review.reviewerId)),
)

const canConfirm = computed(
  () => order.value?.status === 'PENDING' && isPoster.value,
)
const canComplete = computed(
  () => order.value?.status === 'CONFIRMED' && isPoster.value && order.value.taskCategory !== 'TEAM_UP',
)
const canCancel = computed(
  () =>
    order.value !== null &&
    (order.value.status === 'PENDING' || order.value.status === 'CONFIRMED') &&
    isPoster.value,
)
const canAbandon = computed(
  () =>
    order.value !== null &&
    (order.value.status === 'PENDING' || order.value.status === 'CONFIRMED') &&
    isHelper.value,
)
const canReview = computed(
  () =>
    order.value?.status === 'COMPLETED' &&
    order.value.taskCategory !== 'TEAM_UP' &&
    isParticipant.value &&
    !hasSubmittedReview.value,
)

const loadReviews = async () => {
  if (!order.value || order.value.status !== 'COMPLETED' || order.value.taskCategory === 'TEAM_UP') {
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

const runAction = async (action: 'confirm' | 'reject' | 'complete' | 'cancel' | 'abandon') => {
  if (!order.value) return
  submitting.value = true
  try {
    if (action === 'confirm') {
      await confirmOrder(order.value.orderId)
      ElMessage.success(isTeamUp.value ? '组队申请已通过' : '订单已确认')
    } else if (action === 'reject') {
      await rejectOrder(order.value.orderId)
      ElMessage.success(isTeamUp.value ? '已拒绝组队申请' : '已拒绝接单，任务重新开放')
    } else if (action === 'complete') {
      await completeOrder(order.value.orderId)
      ElMessage.success('订单已完成')
    } else if (action === 'cancel') {
      await cancelOrder(order.value.orderId)
      ElMessage.success(isTeamUp.value ? '组队需求已取消' : '订单已取消')
    } else {
      await abandonOrder(order.value.orderId)
      ElMessage.success(
        isTeamUp.value
          ? order.value.status === 'PENDING' ? '已撤回组队申请' : '已退出当前队伍'
          : '已放弃接单，任务重新开放',
      )
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

const goBack = () => {
  if (!route.query.fromRole && !route.query.fromStatus && !route.query.fromPage) {
    router.back()
    return
  }

  const fromRole = route.query.fromRole === 'accepted' ? 'accepted' : 'published'
  const fromStatus = typeof route.query.fromStatus === 'string' ? route.query.fromStatus : 'ALL'
  const fromPage = typeof route.query.fromPage === 'string' ? route.query.fromPage : '1'
  router.push({
    path: '/orders',
    query: {
      role: fromRole,
      status: fromStatus,
      page: fromPage,
    },
  })
}

onMounted(loadOrder)
</script>

<style scoped>
.order-detail-page {
  display: grid;
  height: calc(100vh - 122px);
  gap: 18px;
  min-height: 0;
  align-content: start;
  overflow-y: auto;
  padding-right: 8px;
  scrollbar-gutter: stable;
}

.order-detail-page::-webkit-scrollbar {
  width: 8px;
}

.order-detail-page::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: #cbd5e1;
}

.order-detail-page::-webkit-scrollbar-track {
  background: transparent;
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

.discussion-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
  align-items: stretch;
}

.discussion-grid > * {
  min-width: 0;
}

.review-area {
  display: grid;
  grid-template-columns: minmax(280px, 360px) minmax(0, 1fr);
  gap: 18px;
}

.review-area-single {
  grid-template-columns: minmax(0, 1fr);
}

@media (max-width: 900px) {
  .top,
  .discussion-grid,
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

@media (max-width: 1180px) {
  .order-detail-page {
    height: auto;
    overflow: visible;
    padding-right: 0;
  }
}
</style>
