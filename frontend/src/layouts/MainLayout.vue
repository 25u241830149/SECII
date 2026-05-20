<template>
  <div class="main-shell" :class="{ 'is-collapsed': appStore.sidebarCollapsed }">
    <header class="topbar">
      <RouterLink class="brand" to="/">
        <span class="brand-logo">C</span>
        <strong>CampusHub</strong>
        <span>校园互助平台</span>
      </RouterLink>

      <label class="search-box">
        <span>⌕</span>
        <input placeholder="搜索快递代取、学习辅导、二手交易、活动组队..." />
      </label>

      <nav class="top-actions" aria-label="快捷入口">
        <RouterLink class="publish-button" to="/tasks/publish">+ 发布需求</RouterLink>
        <RouterLink to="/orders">我的订单</RouterLink>
        <button type="button" class="notice-button" @click="appStore.markNoticesRead">
          消息通知
          <span v-if="appStore.unreadNoticeCount">{{ appStore.unreadNoticeCount }}</span>
        </button>
        <el-dropdown
          v-if="authStore.isAuthenticated"
          trigger="click"
          class="user-menu"
          @command="handleUserCommand"
        >
          <button type="button" class="user-entry user-menu-trigger">
            <span class="avatar"></span>
            <span>{{ authStore.user?.nickname || '个人中心' }}</span>
            <span class="chevron">⌄</span>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile-edit">资料编辑</el-dropdown-item>
              <el-dropdown-item command="account-delete">账号注销</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <RouterLink v-else class="auth-link primary" to="/login">登录 / 注册</RouterLink>
      </nav>
    </header>

    <div class="workspace">
      <aside class="sidebar">
        <section class="sidebar-section">
          <div class="section-title">
            <h2>需求分类</h2>
            <button type="button" @click="appStore.toggleSidebar">☰</button>
          </div>
          <button
            v-for="item in categories"
            :key="item.value"
            type="button"
            class="sidebar-item"
            :class="{ active: appStore.activeTaskCategory === item.value }"
            @click="appStore.setActiveTaskCategory(item.value)"
          >
            <span>{{ item.icon }}</span>
            <strong>{{ item.label }}</strong>
          </button>
        </section>

        <section class="sidebar-section filters">
          <h2>筛选条件</h2>
          <div class="filter-row">
            <b>时间</b>
            <span class="pill active">最新发布</span>
            <span class="pill">即将截止</span>
          </div>
          <div class="filter-row">
            <b>地点</b>
            <span class="pill active">附近</span>
            <span class="pill">本学院</span>
            <span class="pill">全校</span>
          </div>
          <div class="filter-row">
            <b>报酬</b>
            <span class="pill">有偿</span>
            <span class="pill">无偿</span>
            <span class="pill">交换互助</span>
          </div>
        </section>

        <section class="sidebar-section stats">
          <div>
            <span class="stat-dot blue">↗</span>
            <b>28</b>
            <small>今日新增</small>
          </div>
          <div>
            <span class="stat-dot green">◔</span>
            <b>16</b>
            <small>进行中</small>
          </div>
          <div>
            <span class="stat-dot orange">✓</span>
            <b>342</b>
            <small>已完成</small>
          </div>
        </section>
      </aside>

      <main class="content-area">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

import { useAppStore, useAuthStore, useUserStore } from '@/stores'

const appStore = useAppStore()
const authStore = useAuthStore()
const userStore = useUserStore()
const router = useRouter()

const categories = [
  { value: 'ALL' as const, label: '全部需求', icon: '▦' },
  { value: 'EXPRESS' as const, label: '快递代取', icon: '□' },
  { value: 'STUDY' as const, label: '学习辅导', icon: '▥' },
  { value: 'SECOND_HAND' as const, label: '二手交易', icon: '◇' },
  { value: 'TEAM_UP' as const, label: '活动组队', icon: '●' },
  { value: 'OTHER' as const, label: '其他', icon: '○' },
]

const handleUserCommand = (command: string | number | object) => {
  if (command === 'profile-edit') {
    router.push('/profile/edit')
    return
  }

  if (command === 'account-delete') {
    router.push('/account/delete')
    return
  }

  if (command === 'logout') {
    userStore.setProfile(null)
    authStore.logout()
    ElMessage.success('已退出登录')
    router.replace('/login')
  }
}
</script>

<style scoped>
.main-shell {
  min-height: 100vh;
  background: #f5f7fb;
}

.topbar {
  position: sticky;
  z-index: 10;
  top: 0;
  display: grid;
  min-height: 72px;
  grid-template-columns: minmax(260px, auto) minmax(260px, 520px) auto;
  gap: 24px;
  align-items: center;
  padding: 0 28px;
  border-bottom: 1px solid #e8edf5;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.04);
}

.brand,
.top-actions,
.user-entry {
  display: flex;
  align-items: center;
}

.brand {
  gap: 10px;
  color: #111827;
  text-decoration: none;
}

.brand-logo {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border-radius: 8px;
  background: linear-gradient(135deg, #1478ff, #6b48ff);
  color: #fff;
  font-size: 20px;
  font-weight: 800;
}

.brand strong {
  color: #1168e8;
  font-size: 22px;
}

.brand span:last-child {
  color: #374151;
  font-size: 17px;
  font-weight: 700;
}

.search-box {
  display: flex;
  height: 46px;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  border: 1px solid #dfe6f2;
  border-radius: 8px;
  background: #fff;
  color: #8b95a5;
}

.search-box input {
  width: 100%;
  border: 0;
  outline: 0;
  background: transparent;
  color: #475569;
  font: inherit;
}

.top-actions {
  justify-content: flex-end;
  gap: 18px;
  color: #374151;
}

.top-actions a,
.notice-button,
.user-menu-trigger {
  border: 0;
  background: transparent;
  color: inherit;
  font: inherit;
  text-decoration: none;
  white-space: nowrap;
}

.publish-button {
  height: 42px;
  padding: 0 22px;
  border-radius: 8px;
  background: linear-gradient(135deg, #1478ff, #6b48ff) !important;
  color: #fff !important;
  font-weight: 700;
  line-height: 42px;
  box-shadow: 0 10px 20px rgba(79, 124, 255, 0.26);
}

.notice-button {
  position: relative;
  cursor: pointer;
}

.notice-button span {
  position: absolute;
  top: -14px;
  right: -16px;
  display: grid;
  width: 20px;
  height: 20px;
  place-items: center;
  border-radius: 50%;
  background: #ff3b4e;
  color: #fff;
  font-size: 12px;
  font-weight: 800;
}

.user-entry {
  gap: 9px;
}

.user-menu-trigger {
  cursor: pointer;
}

.chevron {
  color: #8b95a5;
  font-size: 16px;
  line-height: 1;
}

.auth-link {
  height: 40px;
  padding: 0 16px;
  border-radius: 8px;
  line-height: 40px;
}

.auth-link.primary {
  background: #eef5ff;
  color: #1268ed !important;
  font-weight: 700;
}

.avatar {
  width: 38px;
  height: 38px;
  border: 3px solid #edf4ff;
  border-radius: 50%;
  background: radial-gradient(circle at 35% 30%, #e8f1ff, #b8cffd 42%, #354f87 43%, #172033 100%);
}

.workspace {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 22px;
  padding: 22px 28px 28px;
}

.sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sidebar-section {
  padding: 18px;
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.055);
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-title button {
  display: none;
}

.sidebar-section h2 {
  margin: 0 0 14px;
  color: #111827;
  font-size: 18px;
}

.sidebar-item {
  display: flex;
  width: 100%;
  height: 44px;
  align-items: center;
  gap: 12px;
  margin-top: 9px;
  padding: 0 14px;
  border: 0;
  border-radius: 8px;
  background: #f8fafc;
  color: #4b5563;
  font: inherit;
  text-align: left;
  cursor: pointer;
}

.sidebar-item.active,
.sidebar-item:hover {
  background: #eef5ff;
  color: #1268ed;
}

.sidebar-item span {
  display: grid;
  width: 24px;
  height: 24px;
  place-items: center;
  color: #1268ed;
  font-size: 20px;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin: 13px 0;
}

.filter-row b {
  width: 42px;
  color: #374151;
  font-weight: 600;
}

.pill {
  padding: 7px 11px;
  border: 1px solid #e1e8f2;
  border-radius: 8px;
  background: #fff;
  color: #5b6472;
  font-size: 13px;
}

.pill.active {
  border-color: #d7e8ff;
  background: #edf5ff;
  color: #176fed;
  font-weight: 700;
}

.stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0;
  padding: 20px 8px;
  text-align: center;
}

.stats div + div {
  border-left: 1px solid #e5eaf2;
}

.stat-dot {
  display: grid;
  width: 32px;
  height: 32px;
  place-items: center;
  margin: 0 auto 8px;
  border-radius: 50%;
  color: #fff;
  font-weight: 800;
}

.blue {
  background: #1677ff;
}

.green {
  background: #21b485;
}

.orange {
  background: #ff8a1f;
}

.stats b {
  display: block;
  font-size: 26px;
}

.stats small {
  display: block;
  color: #6b7280;
}

.content-area {
  min-width: 0;
}

@media (max-width: 1180px) {
  .topbar {
    grid-template-columns: 1fr;
    padding: 16px 20px;
  }

  .top-actions {
    justify-content: flex-start;
    overflow-x: auto;
    padding-bottom: 4px;
  }

  .workspace {
    grid-template-columns: 1fr;
    padding: 18px;
  }

  .sidebar {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .stats {
    grid-column: 1 / -1;
  }
}

@media (max-width: 720px) {
  .sidebar {
    grid-template-columns: 1fr;
  }

  .brand span:last-child {
    display: none;
  }
}
</style>
