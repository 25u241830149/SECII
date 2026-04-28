<template>
  <section class="table-surface">
    <div class="section-heading">
      <div>
        <h1 class="section-title">任务管理</h1>
        <p class="section-subtitle">后续可对接任务下架、审核和推荐操作。</p>
      </div>
    </div>

    <el-table :data="rows" stripe>
      <el-table-column prop="title" label="任务标题" />
      <el-table-column prop="category" label="分类" />
      <el-table-column prop="reward" label="赏金" />
      <el-table-column prop="status" label="状态" />
      <el-table-column label="操作" width="180">
        <template #default>
          <div class="action-row">
            <el-button size="small" plain>查看</el-button>
            <el-button size="small" type="danger" plain>下架</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup lang="ts">
import { mockTasks } from '@/constants/mock'
import { getCategoryLabel } from '@/constants/app'

const rows = mockTasks.map((item) => ({
  title: item.title,
  category: getCategoryLabel(item.category),
  reward: `¥${item.reward}`,
  status: item.status,
}))
</script>