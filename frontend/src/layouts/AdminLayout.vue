<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <RouterLink class="admin-brand" to="/">
        <CampusHubLogo size="sm" />
        <strong>Admin</strong>
      </RouterLink>

      <nav>
        <RouterLink v-for="item in navItems" :key="item.to" :to="item.to" :class="{ active: isNavActive(item.to) }">
          {{ item.label }}
        </RouterLink>
      </nav>
    </aside>

    <section class="admin-main">
      <header class="admin-header">
        <div>
          <p>管理后台</p>
          <h1>{{ route.meta.title || '数据看板' }}</h1>
        </div>
        <div class="admin-actions">
          <RouterLink to="/">返回前台</RouterLink>
          <button type="button" @click="handleLogout">退出登录</button>
        </div>
      </header>

      <div class="admin-content">
        <RouterView />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'

import CampusHubLogo from '@/components/CampusHubLogo.vue'
import { useAuthStore, useUserStore } from '@/stores'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const userStore = useUserStore()

const navItems = [
  { to: '/admin', label: '数据看板' },
  { to: '/admin/users', label: '用户管理' },
  { to: '/admin/tasks', label: '任务管理' },
  { to: '/admin/reports', label: '举报处理' },
  { to: '/admin/notices', label: '公告管理' },
]

const isNavActive = (path: string) => (path === '/admin' ? route.path === '/admin' : route.path.startsWith(path))

const handleLogout = () => {
  userStore.setProfile(null)
  authStore.logout()
  ElMessage.success('已退出登录')
  router.replace('/login')
}
</script>

<style scoped>
.admin-layout {
  display: grid;
  height: 100vh;
  grid-template-columns: 248px minmax(0, 1fr);
  background: #f5f7fb;
  overflow: hidden;
}

.admin-sidebar {
  min-height: 0;
  padding: 22px;
  border-right: 1px solid #e3e9f3;
  background: #fff;
  overflow-y: auto;
}

.admin-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 28px;
  color: #111827;
  text-decoration: none;
}

nav {
  display: grid;
  gap: 8px;
}

nav a {
  padding: 11px 12px;
  border-radius: 8px;
  color: #4b5563;
  text-decoration: none;
}

nav a.active {
  background: #eef5ff;
  color: #1268ed;
  font-weight: 700;
}

.admin-main {
  display: flex;
  flex-direction: column;
  height: 100vh;
  min-width: 0;
  min-height: 0;
  padding: 26px;
  overflow: hidden;
}

.admin-header {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 22px;
  padding: 22px;
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
}

.admin-content {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  padding-right: 4px;
}

.admin-header p,
.admin-header h1 {
  margin: 0;
}

.admin-header p {
  color: #687386;
}

.admin-header h1 {
  margin-top: 4px;
  color: #111827;
  font-size: 26px;
}

.admin-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-header a,
.admin-actions button {
  color: #1268ed;
  font-weight: 700;
  text-decoration: none;
}

.admin-actions button {
  padding: 8px 14px;
  border: 1px solid #d7e8ff;
  border-radius: 8px;
  background: #eef5ff;
  cursor: pointer;
  font: inherit;
}

@media (max-width: 840px) {
  .admin-layout {
    height: auto;
    min-height: 100vh;
    grid-template-columns: 1fr;
    overflow: visible;
  }

  .admin-sidebar {
    border-right: 0;
    border-bottom: 1px solid #e3e9f3;
    overflow: visible;
  }

  .admin-main {
    height: auto;
    min-height: 0;
    overflow: visible;
  }

  .admin-content {
    overflow: visible;
  }
}
</style>
