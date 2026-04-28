<template>
  <section class="table-surface">
    <div class="section-heading">
      <div>
        <h1 class="section-title">帖子管理</h1>
        <p class="section-subtitle">与论坛模块保持独立，后台只承担置顶与推荐类动作。</p>
      </div>
    </div>

    <el-table :data="rows" stripe>
      <el-table-column prop="title" label="帖子标题" />
      <el-table-column prop="author" label="作者" />
      <el-table-column prop="interactions" label="互动" />
      <el-table-column label="操作" width="220">
        <template #default>
          <div class="action-row">
            <el-button size="small" plain>置顶</el-button>
            <el-button size="small" type="success" plain>推荐</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup lang="ts">
import { mockPosts } from '@/constants/mock'

const rows = mockPosts.map((item) => ({
  title: item.title,
  author: item.authorName,
  interactions: `${item.likeCount} 赞 / ${item.commentCount} 评`,
}))
</script>