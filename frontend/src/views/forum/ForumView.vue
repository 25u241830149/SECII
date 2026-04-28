<template>
  <section class="feed-layout">
    <div class="page-surface">
      <div class="section-heading">
        <div>
          <h1 class="section-title">论坛中心</h1>
          <p class="section-subtitle">论坛模块保持独立，页面上预留帖子流、互动数和发帖入口。</p>
        </div>
        <el-button type="primary" round @click="handleCreatePost">发帖</el-button>
      </div>

      <div class="list-stack">
        <article v-for="post in mockPosts" :key="post.postId" class="task-feed-card">
          <div class="row-between">
            <div>
              <h2 class="task-title">{{ post.title }}</h2>
              <div class="tag-row">
                <el-tag v-if="post.isPinned" type="warning" effect="plain">置顶</el-tag>
                <el-tag v-if="post.isRecommended" type="success" effect="plain">推荐</el-tag>
              </div>
            </div>
            <span class="muted">{{ post.createdAt }}</span>
          </div>

          <p class="task-body">{{ post.content }}</p>

          <div class="task-meta">
            <span>作者：{{ post.authorName }}</span>
            <span>点赞 {{ post.likeCount }}</span>
            <span>收藏 {{ post.favoriteCount }}</span>
            <span>评论 {{ post.commentCount }}</span>
          </div>
        </article>
      </div>
    </div>

    <aside class="list-stack">
      <div class="notice-card">
        <div class="section-heading">
          <div><h2 class="section-title">热帖排行</h2></div>
        </div>
        <div class="hot-list">
          <div v-for="post in hotPosts" :key="post.postId" class="hot-item">
            <strong>{{ post.title }}</strong>
            <p class="muted">{{ post.likeCount }} 点赞 · {{ post.commentCount }} 回复</p>
          </div>
        </div>
      </div>
    </aside>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { mockPosts } from '@/constants/mock'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const hotPosts = computed(() => [...mockPosts].sort((left, right) => right.likeCount - left.likeCount))

async function handleCreatePost() {
  if (!authStore.isLoggedIn) {
    await router.push({ name: 'login', query: { redirect: '/forum' } })
    return
  }
  ElMessage.success('帖子编辑器骨架待接入')
}
</script>