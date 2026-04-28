<template>
  <section class="page-surface">
    <div class="section-heading">
      <div>
        <h1 class="section-title">我的接单</h1>
        <p class="section-subtitle">与订单中心保持同构，但聚焦个人履约状态。</p>
      </div>
    </div>

    <div class="list-stack">
      <div v-for="order in myOrders" :key="order.orderId" class="task-feed-card">
        <div class="row-between">
          <strong>{{ order.taskTitle }}</strong>
          <el-tag type="success" effect="plain">{{ order.status }}</el-tag>
        </div>
        <div class="task-meta">
          <span>对方：{{ order.counterpartName }}</span>
          <span>金额：¥{{ order.amount }}</span>
          <span>{{ order.updatedAt }}</span>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { mockOrders } from '@/constants/mock'

const myOrders = mockOrders.filter((item) => item.role === 'helper')
</script>