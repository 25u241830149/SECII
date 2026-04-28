<template>
  <section class="page-surface">
    <div class="section-heading">
      <div>
        <h1 class="section-title">我的发单</h1>
        <p class="section-subtitle">后续接入真实列表接口后，可按状态筛选进行细分。</p>
      </div>
    </div>

    <div class="list-stack">
      <div v-for="task in myTasks" :key="task.taskId" class="task-feed-card">
        <div class="row-between">
          <strong>{{ task.title }}</strong>
          <el-tag effect="plain">¥{{ task.reward }}</el-tag>
        </div>
        <p class="muted" style="margin-top: 8px;">{{ task.description }}</p>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { mockTasks } from '@/constants/mock'

const myTasks = mockTasks.slice(0, 3)
</script>