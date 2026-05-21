<template>
  <div class="profile-shell">
    <aside class="profile-sidebar">
      <RouterLink
        v-for="item in menuItems"
        :key="item.to"
        class="profile-nav-item"
        :class="{ active: isActive(item.to) }"
        :to="item.to"
      >
        <el-icon><component :is="item.icon" /></el-icon>
        <span>{{ item.label }}</span>
      </RouterLink>
    </aside>

    <main class="profile-content">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute } from 'vue-router'
import { Document, Edit, Medal, Memo, Star, User, Warning } from '@element-plus/icons-vue'

const route = useRoute()

const menuItems = [
  { label: '个人主页', to: '/profile', icon: User },
  { label: '编辑资料', to: '/profile/edit', icon: Edit },
  { label: '我的收藏', to: '/profile/favorites', icon: Star },
  { label: '我的发单', to: '/profile/published', icon: Document },
  { label: '我的接单', to: '/profile/orders', icon: Memo },
  { label: '信用分与等级', to: '/profile/credit', icon: Medal },
  { label: '账户注销', to: '/profile/delete', icon: Warning },
]

const currentPath = computed(() => route.path)

const isActive = (target: string) => {
  if (target === '/profile') {
    return currentPath.value === '/profile'
  }

  return currentPath.value.startsWith(target)
}
</script>

<style scoped>
.profile-shell {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 24px;
}

.profile-sidebar {
  position: sticky;
  top: 94px;
  display: flex;
  min-height: calc(100vh - 124px);
  flex-direction: column;
  gap: 14px;
  padding: 24px 14px;
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.06);
}

.profile-nav-item {
  display: flex;
  min-height: 50px;
  align-items: center;
  gap: 14px;
  padding: 0 16px;
  border-radius: 8px;
  color: #475569;
  font-size: 16px;
  font-weight: 700;
  text-decoration: none;
}

.profile-nav-item :deep(.el-icon) {
  width: 24px;
  height: 24px;
  color: #64748b;
  font-size: 22px;
}

.profile-nav-item.active,
.profile-nav-item:hover {
  background: #eef4ff;
  color: #2563eb;
}

.profile-nav-item.active :deep(.el-icon),
.profile-nav-item:hover :deep(.el-icon) {
  color: #2563eb;
}

.profile-nav-item:last-child {
  margin-top: 18px;
  color: #ef4444;
}

.profile-nav-item:last-child :deep(.el-icon) {
  color: #ef4444;
}

.profile-nav-item:last-child.active,
.profile-nav-item:last-child:hover {
  background: #fff1f2;
  color: #ef4444;
}

.profile-content {
  min-width: 0;
}

@media (max-width: 1080px) {
  .profile-shell {
    grid-template-columns: 1fr;
  }

  .profile-sidebar {
    position: static;
    min-height: auto;
    flex-direction: row;
    flex-wrap: wrap;
  }

  .profile-nav-item {
    flex: 1 1 160px;
  }
}
</style>

