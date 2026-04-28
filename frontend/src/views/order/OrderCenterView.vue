<template>
  <section class="page-surface">
    <div class="section-heading">
      <div>
        <h1 class="section-title">订单中心</h1>
        <p class="section-subtitle">发单与接单共用一套状态卡片，后续只需要替换真实接口数据。</p>
      </div>
      <el-radio-group v-model="activeRole" size="large">
        <el-radio-button label="poster">我的发单</el-radio-button>
        <el-radio-button label="helper">我的接单</el-radio-button>
      </el-radio-group>
    </div>

    <div class="list-stack">
      <article v-for="order in visibleOrders" :key="order.orderId" class="task-feed-card">
        <div class="row-between">
          <div>
            <h2 class="task-title">{{ order.taskTitle }}</h2>
            <div class="tag-row">
              <el-tag effect="plain">身份 {{ order.role }}</el-tag>
              <el-tag type="success" effect="plain">状态 {{ order.status }}</el-tag>
            </div>
          </div>
          <el-button type="primary" plain round @click="router.push(`/tasks/${order.orderId}`)">回到任务页</el-button>
        </div>
        <div class="task-meta">
          <span>对方用户：{{ order.counterpartName }}</span>
          <span>金额：¥{{ order.amount }}</span>
          <span>更新时间：{{ order.updatedAt }}</span>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { mockOrders } from '@/constants/mock'
import type { OrderRole } from '@/types'

const router = useRouter()
const activeRole = ref<OrderRole>('poster')
const visibleOrders = computed(() => mockOrders.filter((item) => item.role === activeRole.value))
</script>