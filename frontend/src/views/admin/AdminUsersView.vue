<template>
  <section class="table-surface">
    <div class="section-heading">
      <div>
        <h1 class="section-title">用户管理</h1>
        <p class="section-subtitle">保留封禁与实名认证审核的界面承接面。</p>
      </div>
    </div>

    <el-table :data="rows" stripe>
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="department" label="学院" />
      <el-table-column prop="credit" label="信用分" />
      <el-table-column prop="verification" label="认证状态" />
      <el-table-column label="操作" width="220">
        <template #default>
          <div class="action-row">
            <el-button size="small" plain>通过认证</el-button>
            <el-button size="small" type="danger" plain>封禁</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup lang="ts">
const rows = [
  { nickname: '林同学', department: '计算机学院', credit: 98, verification: '已通过' },
  { nickname: '苏同学', department: '管理学院', credit: 93, verification: '待审核' },
]
</script>