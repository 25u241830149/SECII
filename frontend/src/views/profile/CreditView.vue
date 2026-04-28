<template>
  <section class="page-surface">
    <div class="section-heading">
      <div>
        <h1 class="section-title">信用等级</h1>
        <p class="section-subtitle">信用分、等级和最近评价在同页展示，更符合个人中心的集中阅读。</p>
      </div>
    </div>

    <div class="summary-grid">
      <div class="stat-card">
        <div class="stat-label">信用分</div>
        <div class="stat-value">{{ creditScore }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">等级</div>
        <div class="stat-value">{{ level }}</div>
      </div>
    </div>

    <el-progress :percentage="creditScore" :stroke-width="18" style="margin-top: 20px;" />

    <div class="review-list" style="margin-top: 24px;">
      <div v-for="review in mockReviews" :key="review.reviewId" class="notice-card">
        <div class="row-between">
          <strong>{{ review.reviewerName }}</strong>
          <el-rate :model-value="review.rating" disabled />
        </div>
        <p class="muted" style="margin-top: 8px;">{{ review.content }}</p>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { mockCurrentUser, mockReviews, mockUserHome } from '@/constants/mock'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const creditScore = computed(() => userStore.creditScore || mockCurrentUser.creditScore)
const level = computed(() => mockUserHome.level)
</script>