<template>
  <section class="review-list">
    <header>
      <h2>订单评价</h2>
      <span>{{ reviews.length }} 条</span>
    </header>

    <el-empty v-if="!reviews.length" description="暂无评价" />
    <template v-else>
      <article v-for="review in reviews" :key="review.reviewId" class="review-item">
        <el-avatar :size="38" :src="avatarUrl(review.reviewerAvatarUrl)">
          {{ fallbackName(review.reviewerName) }}
        </el-avatar>
        <div class="review-body">
          <div class="review-meta">
            <strong>{{ review.reviewerName }}</strong>
            <span>评价 {{ review.targetUserName }}</span>
            <time>{{ formatTime(review.createdAt) }}</time>
          </div>
          <el-rate :model-value="review.rating" disabled />
          <p>{{ review.content || '对方没有填写文字评价。' }}</p>
        </div>
      </article>
    </template>
  </section>
</template>

<script setup lang="ts">
import { resolveAssetUrl } from '@/utils/asset'
import type { ReviewDTO } from '@/types'

defineProps<{
  reviews: ReviewDTO[]
}>()

const formatTime = (value: string) => new Date(value).toLocaleString('zh-CN', { hour12: false })
const fallbackName = (name: string) => (name || '同学').slice(0, 1).toUpperCase()
const avatarUrl = (value?: string | null) => resolveAssetUrl(value || '')
</script>

<style scoped>
.review-list {
  display: grid;
  gap: 14px;
  padding: 22px;
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.07);
}

.review-list header,
.review-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.review-list header {
  justify-content: space-between;
}

.review-list h2,
.review-item p {
  margin: 0;
}

.review-list h2 {
  color: #111827;
  font-size: 20px;
}

.review-list header span {
  color: #64748b;
  font-size: 13px;
}

.review-item {
  display: grid;
  grid-template-columns: 38px minmax(0, 1fr);
  gap: 12px;
  padding-top: 14px;
  border-top: 1px solid #edf2f7;
}

.review-body {
  display: grid;
  gap: 8px;
}

.review-meta {
  color: #64748b;
  font-size: 13px;
}

.review-meta strong {
  color: #111827;
}

.review-item p {
  color: #374151;
  line-height: 1.55;
}
</style>
