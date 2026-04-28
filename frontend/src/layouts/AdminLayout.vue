<template>
  <div class="app-frame admin-layout">
    <aside class="admin-aside">
      <div class="brand-block" @click="router.push('/admin/stats')">
        <div class="brand-mark">A</div>
        <div>
          <div class="brand-name">Admin Console</div>
          <div class="brand-tagline">域内管理接口统一收口</div>
        </div>
      </div>

      <div class="nav-group-title">管理导航</div>
      <nav class="admin-nav">
        <RouterLink v-for="item in adminNavItems" :key="item.path" :to="item.path" class="admin-link" :class="{ 'is-active': route.path === item.path }">
          {{ item.label }}
        </RouterLink>
      </nav>

      <div class="dashboard-actions">
        <el-button plain @click="router.push('/')">返回前台</el-button>
        <el-button type="danger" plain @click="handleLogout">退出管理</el-button>
      </div>
    </aside>

    <main class="admin-content">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { RouterView, useRoute, useRouter } from 'vue-router'
import { adminNavItems } from '@/constants/app'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const userStore = useUserStore()

function handleLogout() {
  authStore.logout()
  userStore.clearProfile()
  router.push('/login')
}
</script>