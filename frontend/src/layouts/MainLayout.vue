<template>
  <div class="app-frame">
    <header class="app-topbar">
      <div class="brand-block" @click="router.push('/')">
        <div class="brand-mark">C</div>
        <div>
          <div class="brand-name">CampusHub</div>
          <div class="brand-tagline">校园互助平台前端骨架</div>
        </div>
      </div>

      <el-input v-model="searchKeyword" size="large" placeholder="搜索任务、帖子或关键词" @keyup.enter="handleSearch">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <div class="toolbar-actions">
        <el-dropdown @command="handlePublish">
          <el-button type="primary" round>
            <el-icon><Plus /></el-icon>
            发布
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-for="item in categoryOptions" :key="item.value" :command="item.path">{{ item.label }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <el-badge :hidden="!authStore.isLoggedIn || messageStore.unreadCount === 0" :value="messageStore.unreadCount">
          <el-button circle @click="goToMessageCenter">
            <el-icon><Bell /></el-icon>
          </el-button>
        </el-badge>

        <div v-if="!authStore.isLoggedIn" class="toolbar-auth">
          <el-avatar :size="38" :icon="UserFilled" />
          <el-button plain round @click="router.push('/register')">注册</el-button>
          <el-button round @click="router.push('/login')">登录</el-button>
        </div>

        <el-dropdown v-else @command="handleUserCommand">
          <div class="toolbar-user">
            <el-avatar :size="42" :src="displayAvatar" />
            <div class="toolbar-user-meta">
              <span class="toolbar-user-name">{{ displayName }}</span>
              <span class="toolbar-user-subtitle">{{ currentCategoryLabel }}</span>
            </div>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="/me/profile">个人主页</el-dropdown-item>
              <el-dropdown-item command="/me/favorites">我的收藏</el-dropdown-item>
              <el-dropdown-item command="/me/orders">我的接单</el-dropdown-item>
              <el-dropdown-item v-if="authStore.role === 'ADMIN'" command="/admin/stats">管理台</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <div class="layout-meta">
      <div class="meta-chip">当前版面：{{ currentCategoryLabel }}</div>
      <div class="muted">顶部栏 + 左侧导航 + 中央内容区。</div>
    </div>

    <div class="app-shell">
      <aside class="left-rail">
        <nav class="nav-stack">
          <RouterLink v-for="item in mainNavItems" :key="item.path" :to="item.path" class="nav-link" :class="{ 'is-active': isActive(item.path) }">
            <span>{{ item.label }}</span>
            <el-tag v-if="item.path === '/messages' && authStore.isLoggedIn" type="danger" effect="plain">{{ messageStore.unreadCount }}</el-tag>
          </RouterLink>
        </nav>

        <div class="nav-group-title">任务分类</div>
        <nav class="category-stack">
          <RouterLink v-for="item in categoryOptions" :key="item.value" :to="`/category/${item.value}`" class="category-link" :class="{ 'is-active': route.path === `/category/${item.value}` }">
            {{ item.label }}
          </RouterLink>
        </nav>

        <div class="nav-group-title">账号区</div>
        <el-button text @click="goToProfile">查看个人中心</el-button>
      </aside>

      <main class="content-panel">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Bell, Plus, Search, UserFilled } from '@element-plus/icons-vue'
import { computed, ref } from 'vue'
import { RouterView, useRoute, useRouter } from 'vue-router'
import { categoryOptions, getCategoryLabel, mainNavItems } from '@/constants/app'
import { mockCurrentUser } from '@/constants/mock'
import { useAuthStore } from '@/stores/auth'
import { useFeedStore } from '@/stores/feed'
import { useMessageStore } from '@/stores/message'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const userStore = useUserStore()
const messageStore = useMessageStore()
const feedStore = useFeedStore()

if (authStore.isLoggedIn && !userStore.profile) {
  userStore.setProfile(mockCurrentUser)
}

if (authStore.isLoggedIn && messageStore.unreadCount === 0) {
  messageStore.setUnreadCount(3)
}

const searchKeyword = ref(feedStore.keyword)
const displayName = computed(() => userStore.profile?.nickname ?? '校园用户')
const displayAvatar = computed(() => userStore.profile?.avatar ?? mockCurrentUser.avatar)
const currentCategoryLabel = computed(() => getCategoryLabel(route.params.category as string | undefined))

function handleSearch() {
  const keyword = searchKeyword.value.trim()
  feedStore.setKeyword(keyword)
  if (typeof route.params.category === 'string') {
    router.push({ name: 'category', params: { category: route.params.category }, query: keyword ? { keyword } : undefined })
    return
  }
  router.push({ name: 'home', query: keyword ? { keyword } : undefined })
}

function handlePublish(command: string) {
  if (!authStore.isLoggedIn) {
    router.push({ name: 'login', query: { redirect: command } })
    return
  }
  router.push(command)
}

function goToMessageCenter() {
  if (!authStore.isLoggedIn) {
    router.push({ name: 'login', query: { redirect: '/messages' } })
    return
  }
  router.push('/messages')
}

function goToProfile() {
  if (!authStore.isLoggedIn) {
    router.push({ name: 'login', query: { redirect: '/me/profile' } })
    return
  }
  router.push('/me/profile')
}

function handleUserCommand(command: string) {
  if (command === 'logout') {
    authStore.logout()
    userStore.clearProfile()
    router.push('/')
    return
  }
  router.push(command)
}

function isActive(path: string) {
  return path === '/' ? route.path === '/' : route.path.startsWith(path)
}
</script>