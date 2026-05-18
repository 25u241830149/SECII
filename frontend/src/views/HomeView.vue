<template>
  <div class="home-grid">
    <section class="feed-column">
      <section class="hero-panel">
        <div class="hero-content">
          <h1>让校园互助更高效、更可信、更简单</h1>
          <p>发布需求、发现服务、完成协作，CampusHub 帮你把零散的校园互助连接起来。</p>

          <div class="quick-actions">
            <RouterLink to="/tasks/publish" class="quick-card">
              <span class="quick-icon blue">↗</span>
              <strong>发布互助需求</strong>
              <small>填写需求信息，快速发布</small>
            </RouterLink>
            <RouterLink to="/tasks" class="quick-card">
              <span class="quick-icon purple">⌕</span>
              <strong>浏览可接需求</strong>
              <small>发现合适的需求，提供帮助</small>
            </RouterLink>
            <RouterLink to="/orders" class="quick-card">
              <span class="quick-icon green">▤</span>
              <strong>查看我的订单</strong>
              <small>管理订单进度，查看历史记录</small>
            </RouterLink>
          </div>
        </div>
      </section>

      <section class="task-panel">
        <div class="tabs">
          <button
            v-for="tab in tabs"
            :key="tab"
            type="button"
            :class="{ active: activeTab === tab }"
            @click="activeTab = tab"
          >
            {{ tab }}
          </button>
        </div>

        <article v-for="task in visibleTasks" :key="task.title" class="task-row">
          <div class="task-thumb" :class="task.tone">{{ task.symbol }}</div>
          <div class="task-body">
            <h2><span>{{ task.category }}</span>{{ task.title }}</h2>
            <p class="meta">{{ task.publisher }} · 信用分 {{ task.credit }} · {{ task.location }} · {{ task.time }}</p>
            <p class="desc">{{ task.description }}</p>
          </div>
          <strong class="reward">{{ task.reward }}</strong>
          <div class="task-actions">
            <el-button>查看详情</el-button>
            <el-button type="primary">立即接单</el-button>
          </div>
        </article>
      </section>
    </section>

    <aside class="right-rail">
      <section class="profile-card">
        <div class="profile-head">
          <span class="profile-avatar"></span>
          <div>
            <h2>徐同学</h2>
            <p><span>需求方</span><span>服务方</span></p>
          </div>
          <strong>96</strong>
        </div>
        <div class="profile-stats">
          <div><b>18</b><span>已完成订单</span></div>
          <div><b>4.9</b><span>平均评分</span></div>
        </div>
      </section>

      <section class="right-card">
        <div class="right-title">
          <h2>我的订单状态</h2>
          <RouterLink to="/orders">查看全部</RouterLink>
        </div>
        <div class="order-grid">
          <div><b>2</b><span>待确认</span></div>
          <div><b>1</b><span>进行中</span></div>
          <div><b>3</b><span>待评价</span></div>
          <div><b>18</b><span>已完成</span></div>
        </div>
      </section>

      <section class="right-card">
        <div class="right-title">
          <h2>智能推荐</h2>
          <button type="button">换一换</button>
        </div>
        <p class="hint">你经常接取快递代取任务，以下需求可能适合你</p>
        <div class="mini-list">
          <div v-for="item in recommendations" :key="item.title">
            <span>{{ item.symbol }}</span>
            <p><b>{{ item.title }}</b><small>{{ item.location }}</small></p>
            <strong>{{ item.reward }}</strong>
          </div>
        </div>
      </section>

      <section class="right-card">
        <div class="right-title">
          <h2>最新通知</h2>
          <RouterLink to="/messages">查看全部</RouterLink>
        </div>
        <div class="notice-list">
          <p><span></span>王同学接取了你的需求 <small>2 分钟前</small></p>
          <p><span></span>你的订单已进入进行中状态 <small>15 分钟前</small></p>
          <p><span></span>李同学给你留下了评价 <small>1 小时前</small></p>
        </div>
      </section>
    </aside>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

const tabs = ['推荐需求', '最新发布']
const activeTab = ref(tabs[0])

const tasks = [
  {
    symbol: '□',
    tone: 'blue',
    category: '快递代取',
    title: '【快递代取】帮取南门菜鸟驿站快递',
    publisher: '张同学',
    credit: 95,
    location: '南门菜鸟驿站',
    time: '今天 12:30 前',
    description: '有两个快递，取件码已发，取完请送到紫荆公寓 3 栋楼下，感谢！',
    reward: '5 元',
  },
  {
    symbol: '▥',
    tone: 'purple',
    category: '学习辅导',
    title: '【学习辅导】高数期中考前答疑',
    publisher: '李学姐',
    credit: 98,
    location: '理学院教学楼 307',
    time: '明天 19:00-21:00',
    description: '高数期中快到了，想找学长学姐答疑解惑，重点章节：微分方程、二重积分。',
    reward: '30 元/小时',
  },
  {
    symbol: '◇',
    tone: 'green',
    category: '二手交易',
    title: '【二手交易】出售九成新电风扇',
    publisher: '王同学',
    credit: 92,
    location: '紫荆公寓',
    time: '发布于 今天 10:15',
    description: '九成新电风扇，使用半年，静音大风力，功能正常，支持自提。',
    reward: '45 元',
  },
  {
    symbol: '●',
    tone: 'orange',
    category: '活动组队',
    title: '【活动组队】羽毛球双打搭子招募',
    publisher: '陈同学',
    credit: 93,
    location: '东区体育馆',
    time: '周六 18:00-20:00',
    description: '周末打羽毛球双打，寻找水平相当的搭子，一起锻炼，开心运动！',
    reward: '互助组队',
  },
]

const recommendations = [
  { symbol: '□', title: '帮取快递（北门菜鸟驿站）', location: '北门菜鸟驿站', reward: '4 元' },
  { symbol: '□', title: '帮取快递（圆通 2 个件）', location: '南区快递点', reward: '6 元' },
  { symbol: '▥', title: '高数线代答疑辅导', location: '明天 18:30', reward: '25 元/小时' },
]

const visibleTasks = computed(() => (activeTab.value === '最新发布' ? [...tasks].reverse() : tasks))
</script>

<style scoped>
.home-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 22px;
}

.feed-column,
.right-rail {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 18px;
}

.hero-panel {
  position: relative;
  min-height: 246px;
  overflow: hidden;
  border-radius: 8px;
  background:
    radial-gradient(circle at 82% 65%, rgba(255, 255, 255, 0.42), transparent 15%),
    linear-gradient(120deg, #0878ff 0%, #376fff 45%, #7657ff 100%);
  color: #fff;
}

.hero-panel::after {
  position: absolute;
  right: 0;
  top: 0;
  width: 34%;
  height: 100%;
  background:
    linear-gradient(to top, rgba(255, 255, 255, 0.18), transparent 70%),
    repeating-linear-gradient(90deg, rgba(255, 255, 255, 0.28) 0 4px, transparent 4px 48px);
  content: "";
  opacity: 0.8;
}

.hero-content {
  position: relative;
  z-index: 1;
  padding: 30px 34px;
}

.hero-content h1 {
  margin: 0;
  font-size: 30px;
  line-height: 1.3;
}

.hero-content p {
  margin: 10px 0 0;
  opacity: 0.92;
}

.quick-actions {
  display: grid;
  max-width: 690px;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 28px;
}

.quick-card {
  display: grid;
  min-height: 92px;
  grid-template-columns: 50px 1fr;
  column-gap: 14px;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.96);
  color: #1f2937;
  text-decoration: none;
  box-shadow: 0 14px 26px rgba(8, 60, 160, 0.16);
}

.quick-icon {
  display: grid;
  width: 48px;
  height: 48px;
  grid-row: span 2;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  font-size: 24px;
  font-weight: 800;
}

.quick-icon.blue {
  background: #1677ff;
}

.quick-icon.purple {
  background: #7c3aed;
}

.quick-icon.green {
  background: #12a673;
}

.quick-card strong {
  align-self: end;
  font-size: 16px;
}

.quick-card small {
  align-self: start;
  color: #64748b;
}

.task-panel,
.right-card,
.profile-card {
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.055);
}

.task-panel {
  padding: 16px 16px 8px;
}

.tabs {
  display: flex;
  gap: 36px;
  border-bottom: 1px solid #e5ebf4;
}

.tabs button {
  position: relative;
  padding: 0 8px 12px;
  border: 0;
  background: transparent;
  color: #6b7280;
  font: inherit;
  font-size: 17px;
  font-weight: 800;
  cursor: pointer;
}

.tabs button.active {
  color: #1268ed;
}

.tabs button.active::after {
  position: absolute;
  right: 0;
  bottom: -1px;
  left: 0;
  height: 3px;
  border-radius: 999px;
  background: #1268ed;
  content: "";
}

.task-row {
  display: grid;
  grid-template-columns: 74px minmax(0, 1fr) 110px 184px;
  gap: 14px;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #eef2f7;
}

.task-row:last-child {
  border-bottom: 0;
}

.task-thumb {
  display: grid;
  width: 64px;
  height: 64px;
  place-items: center;
  border-radius: 8px;
  font-size: 34px;
  font-weight: 800;
}

.task-thumb.blue {
  background: #eef5ff;
  color: #1268ed;
}

.task-thumb.purple {
  background: #f1ecff;
  color: #7c3aed;
}

.task-thumb.green {
  background: #eafaf2;
  color: #12a673;
}

.task-thumb.orange {
  background: #fff5df;
  color: #f97316;
}

.task-body h2 {
  margin: 0 0 8px;
  color: #111827;
  font-size: 17px;
}

.task-body h2 span {
  margin-right: 8px;
  padding: 4px 8px;
  border-radius: 6px;
  background: #eaf3ff;
  color: #1268ed;
  font-size: 13px;
}

.meta,
.desc {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
}

.desc {
  margin-top: 8px;
  color: #4b5563;
  line-height: 1.45;
}

.reward {
  color: #f97316;
  font-size: 20px;
  text-align: center;
}

.task-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.profile-card,
.right-card {
  padding: 18px;
}

.profile-head {
  display: grid;
  grid-template-columns: 64px 1fr 68px;
  gap: 12px;
  align-items: center;
}

.profile-avatar {
  width: 64px;
  height: 64px;
  border: 4px solid #edf4ff;
  border-radius: 50%;
  background: radial-gradient(circle at 38% 28%, #f1f7ff, #b9cffc 40%, #24324f 42%, #111827 100%);
}

.profile-head h2,
.right-title h2 {
  margin: 0;
  color: #111827;
  font-size: 18px;
}

.profile-head p {
  display: flex;
  gap: 6px;
  margin: 8px 0 0;
}

.profile-head p span {
  padding: 4px 8px;
  border-radius: 6px;
  background: #eef5ff;
  color: #1268ed;
  font-size: 12px;
  font-weight: 700;
}

.profile-head strong {
  color: #1268ed;
  font-size: 34px;
  text-align: center;
}

.profile-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e8edf5;
  text-align: center;
}

.profile-stats div + div {
  border-left: 1px solid #e8edf5;
}

.profile-stats b {
  display: block;
  font-size: 22px;
}

.profile-stats span {
  color: #6b7280;
  font-size: 13px;
}

.right-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.right-title a,
.right-title button {
  border: 0;
  background: transparent;
  color: #7b8495;
  font: inherit;
  font-size: 13px;
  text-decoration: none;
}

.order-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.order-grid div {
  display: grid;
  min-height: 78px;
  place-items: center;
  border-radius: 8px;
  background: #f5f9ff;
}

.order-grid div:nth-child(2) {
  background: #ecfbf3;
}

.order-grid div:nth-child(3) {
  background: #fff5e7;
}

.order-grid div:nth-child(4) {
  background: #f3efff;
}

.order-grid b {
  align-self: end;
  font-size: 21px;
}

.order-grid span {
  align-self: start;
  color: #4b5563;
  font-size: 12px;
}

.hint {
  margin: -6px 0 12px;
  color: #7b8495;
  font-size: 13px;
}

.mini-list {
  display: grid;
  gap: 12px;
}

.mini-list div {
  display: grid;
  grid-template-columns: 34px 1fr 54px;
  gap: 10px;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 1px solid #eef2f7;
}

.mini-list div:last-child {
  border-bottom: 0;
  padding-bottom: 0;
}

.mini-list > div > span {
  display: grid;
  width: 32px;
  height: 32px;
  place-items: center;
  border-radius: 8px;
  background: #eef5ff;
  color: #1268ed;
  font-weight: 800;
}

.mini-list p,
.notice-list p {
  margin: 0;
}

.mini-list b,
.mini-list small {
  display: block;
}

.mini-list b {
  font-size: 14px;
}

.mini-list small {
  margin-top: 4px;
  color: #7b8495;
  font-size: 12px;
}

.mini-list strong {
  color: #f97316;
  font-size: 14px;
  text-align: right;
}

.notice-list {
  display: grid;
  gap: 12px;
}

.notice-list p {
  display: grid;
  grid-template-columns: 10px 1fr 68px;
  gap: 10px;
  align-items: center;
  color: #374151;
  font-size: 13px;
}

.notice-list span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #1677ff;
}

.notice-list p:nth-child(2) span {
  background: #21b485;
}

.notice-list p:nth-child(3) span {
  background: #8358ff;
}

.notice-list small {
  color: #8b95a5;
  text-align: right;
}

@media (max-width: 1180px) {
  .home-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 840px) {
  .quick-actions,
  .task-row {
    grid-template-columns: 1fr;
  }

  .task-actions {
    justify-content: flex-start;
  }
}
</style>
