<template>
  <section class="page-surface">
    <div class="section-heading">
      <div>
        <h1 class="section-title">{{ categoryTitle }}</h1>
        <p class="section-subtitle">仿贴吧式主信息流，支持分类、关键词与时间/热度切换。</p>
      </div>
      <el-radio-group v-model="sortMode" size="large">
        <el-radio-button label="time">最新</el-radio-button>
        <el-radio-button label="hot">最热</el-radio-button>
      </el-radio-group>
    </div>

    <div class="feed-layout">
      <div class="list-stack">
        <el-skeleton v-if="loading" :rows="6" animated />
        <template v-else-if="feedStore.records.length > 0">
          <article v-for="task in feedStore.records" :key="task.taskId" class="task-feed-card">
            <div class="task-header">
              <div>
                <h2 class="task-title">{{ task.title }}</h2>
                <div class="tag-row">
                  <el-tag type="warning" effect="plain">{{ getCategoryLabel(task.category) }}</el-tag>
                  <el-tag effect="plain">赏金 ¥{{ task.reward }}</el-tag>
                  <el-tag effect="plain">信用 {{ task.publisherCredit }}</el-tag>
                </div>
              </div>
              <el-button type="primary" plain round @click="openDetail(task.taskId)">查看详情</el-button>
            </div>
            <p class="task-body">{{ task.description }}</p>
            <div class="task-meta">
              <span>发布者：{{ task.publisherName }}</span>
              <span>地点：{{ task.location }}</span>
              <span>时间：{{ task.publishTime }}</span>
              <span>热度：{{ task.hotScore }}</span>
            </div>
          </article>
        </template>
        <el-empty v-else description="暂无匹配任务" />
      </div>

      <aside class="list-stack">
        <div class="notice-card">
          <div class="section-heading">
            <div>
              <h2 class="section-title">热帖速览</h2>
              <p class="section-subtitle">右侧热榜先用前端组合视图承接。</p>
            </div>
          </div>
          <div class="hot-list">
            <div v-for="post in featuredPosts" :key="post.postId" class="hot-item">
              <strong>{{ post.title }}</strong>
              <p class="muted">{{ post.authorName }} · {{ post.commentCount }} 条评论</p>
            </div>
          </div>
        </div>

        <div class="notice-card">
          <div class="summary-grid">
            <div class="stat-card">
              <div class="stat-label">分类</div>
              <div class="stat-value">{{ categoryTitle }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">关键词</div>
              <div class="stat-value">{{ activeKeyword || '全部' }}</div>
            </div>
          </div>
        </div>
      </aside>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTaskList } from '@/api/task'
import { getCategoryLabel } from '@/constants/app'
import { mockPosts } from '@/constants/mock'
import { useFeedStore } from '@/stores/feed'
import type { FeedSort, PublishCategory } from '@/types'

const route = useRoute()
const router = useRouter()
const feedStore = useFeedStore()
const loading = ref(false)

const currentCategory = computed(() => {
  const value = route.params.category
  return typeof value === 'string' ? (value as PublishCategory) : undefined
})

const activeKeyword = computed(() => {
  const keyword = route.query.keyword
  return typeof keyword === 'string' ? keyword : feedStore.keyword
})

const sortMode = computed<FeedSort>({
  get: () => feedStore.sort,
  set: (value) => feedStore.setSort(value),
})

const categoryTitle = computed(() => getCategoryLabel(currentCategory.value))
const featuredPosts = computed(() => mockPosts.slice(0, 2))

async function fetchTasks() {
  loading.value = true

  try {
    feedStore.setCategory(currentCategory.value)
    feedStore.setKeyword(activeKeyword.value)

    const result = await getTaskList({
      category: currentCategory.value,
      keyword: activeKeyword.value || undefined,
      sort: feedStore.sort,
      page: 1,
      size: 10,
    })

    feedStore.setRecords(result.records)
  } catch {
    feedStore.setRecords([])
  } finally {
    loading.value = false
  }
}

watch(
  [currentCategory, activeKeyword, () => feedStore.sort],
  () => {
    void fetchTasks()
  },
  { immediate: true },
)

function openDetail(taskId: number) {
  router.push(`/tasks/${taskId}`)
}
</script>