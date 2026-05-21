<template>
  <section class="profile-home">
    <section class="hero-card">
      <div class="identity">
        <el-avatar :size="92" :src="userAvatar">
          {{ avatarFallback }}
        </el-avatar>
        <div>
          <div class="name-line">
            <h1>{{ displayName }}</h1>
            <span>{{ creditLevel }} 可信用户</span>
          </div>
          <p>学号：{{ authStore.user?.studentId || '-' }} <b></b> 普通用户</p>
          <p class="bio">这个人很懒，什么都没有留下~</p>
          <div class="meta-line">
            <span>计算机学院</span>
            <span>上海大学</span>
          </div>
        </div>
      </div>

      <div class="credit-panel">
        <div>
          <span>信用分</span>
          <strong>{{ creditScore }}</strong>
        </div>
        <div class="medal">★</div>
        <div>
          <span>信用等级</span>
          <strong>{{ creditLevel }}</strong>
          <small>可信用户</small>
        </div>
      </div>
    </section>

    <section class="metric-strip">
      <article v-for="item in topStats" :key="item.label">
        <span :class="['metric-icon', item.tone]">
          <el-icon><component :is="item.icon" /></el-icon>
        </span>
        <div>
          <small>{{ item.label }}</small>
          <strong>{{ item.value }}</strong>
        </div>
      </article>
    </section>

    <section class="profile-card favorites-card">
      <header>
        <h2><el-icon><Star /></el-icon> 我的收藏</h2>
        <RouterLink to="/profile/favorites">更多</RouterLink>
      </header>
      <article v-for="task in favoritePreview" :key="task.id" class="favorite-row">
        <div class="task-thumb">{{ task.image }}</div>
        <div>
          <h3>{{ task.title }}</h3>
          <p>
            <span>{{ profileTaskCategoryLabels[task.category] }}</span>
            {{ task.location }}
          </p>
        </div>
        <time>收藏时间：{{ task.collectedAt }}</time>
        <button type="button" aria-label="收藏">☆</button>
      </article>
    </section>

    <div class="profile-grid">
      <section class="profile-card compact-card">
        <header>
          <h2><el-icon><Document /></el-icon> 我的发单</h2>
          <RouterLink to="/profile/published">更多</RouterLink>
        </header>
        <ul>
          <li><span class="dot orange"></span>进行中 <strong>3</strong></li>
          <li><span class="dot green"></span>已完成 <strong>7</strong></li>
          <li><span class="dot red"></span>已取消 <strong>2</strong></li>
        </ul>
      </section>

      <section class="profile-card compact-card">
        <header>
          <h2><el-icon><Memo /></el-icon> 我的接单</h2>
          <RouterLink to="/profile/orders">更多</RouterLink>
        </header>
        <ul>
          <li><span class="dot orange"></span>进行中 <strong>2</strong></li>
          <li><span class="dot green"></span>已完成 <strong>5</strong></li>
          <li><span class="dot red"></span>已取消 <strong>1</strong></li>
        </ul>
      </section>

      <section class="profile-card credit-card">
        <header>
          <h2><el-icon><Medal /></el-icon> 信用分与等级</h2>
          <RouterLink to="/profile/credit">更多</RouterLink>
        </header>
        <div class="credit-mini">
          <strong>{{ creditScore }}</strong>
          <div class="medal small">★</div>
          <div>
            <p>完成率 <span>100%</span></p>
            <el-progress :percentage="100" :show-text="false" color="#35b779" />
            <p>取消率 <span>0%</span></p>
            <el-progress :percentage="0" :show-text="false" color="#f05252" />
          </div>
        </div>
      </section>

      <section class="profile-card danger-card">
        <header>
          <h2><el-icon><Warning /></el-icon> 账户注销</h2>
          <RouterLink to="/profile/delete">更多</RouterLink>
        </header>
        <p>注销账户后，您的所有数据将被永久删除，且无法恢复。</p>
        <ul>
          <li>所有发布记录将被删除</li>
          <li>信用分与等级将被清空</li>
          <li>无法恢复，请谨慎操作</li>
        </ul>
        <RouterLink class="danger-action" to="/profile/delete">前往注销</RouterLink>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { Check, Document, Medal, Memo, Position, Star, Warning } from '@element-plus/icons-vue'

import { useAuthStore, useUserStore } from '@/stores'
import { resolveAssetUrl } from '@/utils/asset'
import { profileTaskCategoryLabels } from './profileLabels'
import { profileFavorites } from './profileMock'

const authStore = useAuthStore()
const userStore = useUserStore()

const displayName = computed(() => userStore.home?.nickname || authStore.user?.nickname || 'lyh')
const userAvatar = computed(() => resolveAssetUrl(userStore.home?.avatarUrl || authStore.user?.avatarUrl || ''))
const avatarFallback = computed(() => displayName.value.slice(0, 1).toUpperCase())
const creditScore = computed(() => userStore.home?.creditScore ?? authStore.user?.creditScore ?? 100)
const creditLevel = computed(() => userStore.home?.creditLevel || 'Lv.3')
const favoritePreview = computed(() => profileFavorites.slice(0, 3))

const topStats = computed(() => [
  { label: '发布任务数', value: userStore.home?.publishedTaskCount ?? 12, icon: Position, tone: 'blue' },
  { label: '完成接单数', value: userStore.home?.completedOrderCount ?? 8, icon: Check, tone: 'green' },
  { label: '评价数', value: userStore.home?.reviewCount ?? 15, icon: Memo, tone: 'orange' },
])

onMounted(async () => {
  const userId = authStore.user?.userId
  if (!userId) return

  try {
    await userStore.fetchHome(userId)
  } catch {
    // Mock dashboard data remains visible while the backend aggregate endpoints are incomplete.
  }
})
</script>

<style scoped>
.profile-home {
  display: grid;
  gap: 18px;
}

.hero-card,
.metric-strip,
.profile-card {
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.06);
}

.hero-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: 28px;
  align-items: center;
  padding: 36px;
  background:
    linear-gradient(120deg, rgba(239, 246, 255, 0.96), rgba(244, 240, 255, 0.92)),
    #fff;
}

.identity {
  display: flex;
  gap: 24px;
  align-items: center;
}

.name-line {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.name-line h1 {
  margin: 0;
  color: #111827;
  font-size: 30px;
}

.name-line span,
.favorite-row p span {
  padding: 4px 8px;
  border-radius: 6px;
  background: #dbeafe;
  color: #2563eb;
  font-size: 13px;
  font-weight: 800;
}

.identity p {
  margin: 10px 0 0;
  color: #667085;
}

.identity p b {
  display: inline-block;
  width: 1px;
  height: 12px;
  margin: 0 10px;
  background: #cbd5e1;
}

.bio {
  color: #475569 !important;
}

.meta-line {
  display: flex;
  gap: 24px;
  margin-top: 22px;
  color: #64748b;
}

.credit-panel {
  display: grid;
  grid-template-columns: 1fr 110px 1fr;
  align-items: center;
  padding: 28px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.54);
  box-shadow: inset 0 0 0 1px rgba(226, 232, 240, 0.6);
  text-align: center;
}

.credit-panel > div + div {
  border-left: 1px solid #dbe3f0;
}

.credit-panel span {
  color: #334155;
  font-weight: 700;
}

.credit-panel strong {
  display: block;
  margin-top: 8px;
  color: #4169e1;
  font-size: 42px;
  line-height: 1;
}

.credit-panel small {
  display: block;
  margin-top: 8px;
  color: #475569;
  font-weight: 700;
}

.medal {
  display: grid;
  width: 84px;
  height: 84px;
  place-items: center;
  margin: 0 auto;
  border: 8px solid #bcd3ff;
  border-radius: 50%;
  background: linear-gradient(135deg, #82a9ff, #4f77de);
  color: #fff;
  font-size: 36px;
  box-shadow: 0 12px 24px rgba(79, 119, 222, 0.28);
}

.metric-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  padding: 24px 30px;
}

.metric-strip article {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 22px;
}

.metric-strip article + article {
  border-left: 1px solid #e5ebf4;
}

.metric-icon {
  display: grid;
  width: 54px;
  height: 54px;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  font-size: 28px;
}

.metric-icon.blue {
  background: #4169e1;
}

.metric-icon.green {
  background: #3dbb78;
}

.metric-icon.orange {
  background: #f59e0b;
}

.metric-strip small,
.metric-strip strong {
  display: block;
}

.metric-strip small {
  color: #475569;
  font-weight: 700;
}

.metric-strip strong {
  margin-top: 8px;
  color: #111827;
  font-size: 26px;
}

.profile-card {
  padding: 24px 28px;
}

.profile-card header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.profile-card h2 {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0;
  color: #111827;
  font-size: 18px;
}

.profile-card header a {
  color: #64748b;
  font-weight: 700;
  text-decoration: none;
}

.favorite-row {
  display: grid;
  grid-template-columns: 76px minmax(0, 1fr) 190px 42px;
  gap: 18px;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #eef2f7;
}

.favorite-row:last-child {
  border-bottom: 0;
}

.task-thumb {
  display: grid;
  width: 68px;
  height: 68px;
  place-items: center;
  border-radius: 8px;
  background: #f1f5f9;
  font-size: 34px;
}

.favorite-row h3 {
  margin: 0 0 10px;
  color: #111827;
  font-size: 17px;
}

.favorite-row p {
  display: flex;
  gap: 14px;
  align-items: center;
  margin: 0;
  color: #64748b;
}

.favorite-row time {
  color: #94a3b8;
}

.favorite-row button {
  width: 38px;
  height: 38px;
  border: 1px solid #dbe3ef;
  border-radius: 8px;
  background: #fff;
  color: #64748b;
  cursor: pointer;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.compact-card ul,
.danger-card ul {
  display: grid;
  gap: 16px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.compact-card li {
  display: grid;
  grid-template-columns: 12px 1fr 40px;
  gap: 10px;
  align-items: center;
  color: #475569;
}

.dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
}

.dot.orange {
  background: #f59e0b;
}

.dot.green {
  background: #35b779;
}

.dot.red {
  background: #f05252;
}

.credit-mini {
  display: grid;
  grid-template-columns: 78px 110px minmax(0, 1fr);
  gap: 22px;
  align-items: center;
}

.credit-mini > strong {
  color: #4169e1;
  font-size: 44px;
}

.medal.small {
  width: 72px;
  height: 72px;
  font-size: 30px;
}

.credit-mini p {
  display: flex;
  justify-content: space-between;
  margin: 8px 0 4px;
  color: #475569;
}

.danger-card p {
  margin: 0 0 16px;
  color: #475569;
}

.danger-card li {
  color: #475569;
}

.danger-card li::before {
  color: #35b779;
  content: "✓ ";
  font-weight: 800;
}

.danger-action {
  display: inline-flex;
  min-height: 42px;
  align-items: center;
  justify-content: center;
  margin-top: 20px;
  padding: 0 20px;
  border-radius: 8px;
  background: #ef4444;
  color: #fff;
  font-weight: 800;
  text-decoration: none;
}

@media (max-width: 980px) {
  .hero-card,
  .profile-grid {
    grid-template-columns: 1fr;
  }

  .metric-strip {
    grid-template-columns: 1fr;
    gap: 18px;
  }

  .metric-strip article + article {
    border-left: 0;
    border-top: 1px solid #e5ebf4;
    padding-top: 18px;
  }

  .favorite-row {
    grid-template-columns: 64px minmax(0, 1fr);
  }

  .favorite-row time,
  .favorite-row button {
    grid-column: 2;
  }
}
</style>
