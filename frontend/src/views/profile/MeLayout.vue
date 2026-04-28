<template>
  <section class="profile-shell">
    <aside class="profile-side">
      <div class="profile-summary">
        <el-avatar :size="72" :src="profile.avatar" />
        <div class="toolbar-user-meta">
          <span class="toolbar-user-name">{{ profile.nickname }}</span>
          <span class="toolbar-user-subtitle">{{ summary.level }}</span>
        </div>
      </div>

      <div class="summary-grid" style="margin-bottom: 20px;">
        <div class="stat-card">
          <div class="stat-label">信用分</div>
          <div class="stat-value">{{ summary.creditScore }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">已完成</div>
          <div class="stat-value">{{ summary.completedOrders }}</div>
        </div>
      </div>

      <nav class="profile-nav">
        <RouterLink v-for="item in profileNavItems" :key="item.path" :to="item.path" class="profile-link" :class="{ 'is-active': route.path === item.path }">
          {{ item.label }}
        </RouterLink>
      </nav>
    </aside>

    <main class="profile-main">
      <RouterView />
    </main>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import { profileNavItems } from '@/constants/app'
import { mockCurrentUser, mockUserHome } from '@/constants/mock'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()
if (!userStore.profile) {
  userStore.setProfile(mockCurrentUser)
}

const profile = computed(() => userStore.profile ?? mockCurrentUser)
const summary = computed(() => mockUserHome)
</script>