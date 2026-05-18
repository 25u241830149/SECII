<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <RouterLink class="admin-brand" to="/">
        <span>C</span>
        <strong>CampusHub Admin</strong>
      </RouterLink>

      <nav>
        <RouterLink v-for="item in navItems" :key="item.to" :to="item.to">
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
        <RouterLink to="/">返回前台</RouterLink>
      </header>

      <RouterView />
    </section>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()

const navItems = [
  { to: '/admin', label: '数据看板' },
  { to: '/admin/users', label: '用户管理' },
  { to: '/admin/tasks', label: '任务管理' },
  { to: '/admin/reports', label: '举报处理' },
  { to: '/admin/notices', label: '公告管理' },
]
</script>

<style scoped>
.admin-layout {
  display: grid;
  min-height: 100vh;
  grid-template-columns: 248px minmax(0, 1fr);
  background: #f5f7fb;
}

.admin-sidebar {
  padding: 22px;
  border-right: 1px solid #e3e9f3;
  background: #fff;
}

.admin-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 28px;
  color: #111827;
  text-decoration: none;
}

.admin-brand span {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  border-radius: 8px;
  background: linear-gradient(135deg, #1478ff, #6b48ff);
  color: #fff;
  font-weight: 800;
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

nav a.router-link-active {
  background: #eef5ff;
  color: #1268ed;
  font-weight: 700;
}

.admin-main {
  min-width: 0;
  padding: 26px;
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 22px;
  padding: 22px;
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
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

.admin-header a {
  color: #1268ed;
  font-weight: 700;
  text-decoration: none;
}

@media (max-width: 840px) {
  .admin-layout {
    grid-template-columns: 1fr;
  }

  .admin-sidebar {
    border-right: 0;
    border-bottom: 1px solid #e3e9f3;
  }
}
</style>
