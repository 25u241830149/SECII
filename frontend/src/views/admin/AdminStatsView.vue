<template>
  <section class="page-surface">
    <div class="section-heading">
      <div>
        <h1 class="section-title">后台数据总览</h1>
        <p class="section-subtitle">对齐当前后端实际提供的 dashboard 统计接口入口。</p>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-label">用户总数</div>
        <div class="stat-value">{{ mockAdminStats.totalUsers }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">活跃任务</div>
        <div class="stat-value">{{ mockAdminStats.activeTasks }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">活跃订单</div>
        <div class="stat-value">{{ mockAdminStats.activeOrders }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">论坛帖子</div>
        <div class="stat-value">{{ mockAdminStats.forumPosts }}</div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { mockAdminStats } from '@/constants/mock'
</script>