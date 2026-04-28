<template>
  <section class="page-surface">
    <div class="section-heading">
      <div>
        <h1 class="section-title">我的收藏</h1>
        <p class="section-subtitle">任务收藏和帖子收藏共用一页，后续按接口聚合即可。</p>
      </div>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="任务收藏" name="tasks">
        <div class="list-stack">
          <div v-for="task in favoriteTasks" :key="task.taskId" class="task-feed-card">
            <strong>{{ task.title }}</strong>
            <p class="muted" style="margin-top: 8px;">{{ task.description }}</p>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="帖子收藏" name="posts">
        <div class="list-stack">
          <div v-for="post in favoritePosts" :key="post.postId" class="task-feed-card">
            <strong>{{ post.title }}</strong>
            <p class="muted" style="margin-top: 8px;">{{ post.content }}</p>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { mockPosts, mockTasks } from '@/constants/mock'

const activeTab = ref('tasks')
const favoriteTasks = mockTasks.filter((item) => item.favorite)
const favoritePosts = mockPosts.filter((item) => item.favoriteCount >= 10)
</script>