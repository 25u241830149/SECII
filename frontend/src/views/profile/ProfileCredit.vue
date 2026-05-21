<template>
  <section class="credit-page">
    <header class="credit-title">
      <div>
        <h1><el-icon><Medal /></el-icon> 信用分与等级</h1>
        <p>良好的信用让互助更安心，保持高分解锁更多权益。</p>
      </div>
      <RouterLink to="/help/credit">信用分规则说明</RouterLink>
    </header>

    <section class="credit-hero">
      <div>
        <span>当前信用分</span>
        <strong>{{ score }}</strong>
        <p>信用优秀，继续保持哦！</p>
      </div>
      <div class="medal">★</div>
      <div>
        <span>信用等级</span>
        <strong>{{ level }}</strong>
        <p>可信用户</p>
      </div>
      <div class="progress-area">
        <h2>距离下一级 Lv.4 诚信达人 还差 {{ nextScore }} 分</h2>
        <el-progress :percentage="progress" :show-text="false" color="#4169e1" />
        <p><span>达到 Lv.4 可解锁更多发布权限和专属标识</span><b>{{ score }} / 120</b></p>
      </div>
    </section>

    <section class="metric-grid">
      <article v-for="metric in metrics" :key="metric.label">
        <span :class="['metric-icon', metric.tone]">
          <el-icon><component :is="metric.icon" /></el-icon>
        </span>
        <div>
          <small>{{ metric.label }}</small>
          <strong>{{ metric.value }}</strong>
          <em>{{ metric.helper }}</em>
        </div>
      </article>
    </section>

    <div class="credit-columns">
      <section class="credit-card">
        <h2><el-icon><Document /></el-icon> 信用记录</h2>
        <div class="record-table">
          <div class="record-head">
            <span>时间</span>
            <span>变动原因</span>
            <span>分值变化</span>
            <span>变动后分值</span>
          </div>
          <div v-for="record in creditRecords" :key="record.id" class="record-row">
            <span>{{ record.time }}</span>
            <span>{{ record.reason }}</span>
            <strong :class="{ negative: record.delta < 0 }">
              {{ record.delta > 0 ? '+' : '' }}{{ record.delta }}
            </strong>
            <span>{{ record.scoreAfter }}</span>
          </div>
        </div>
        <button type="button">查看全部记录</button>
      </section>

      <section class="credit-card rule-card">
        <h2><el-icon><Document /></el-icon> 加减分说明</h2>
        <div class="rule-box plus">
          <h3>加分项</h3>
          <p><span>顺利完成任务（按订单金额/难度综合）</span><strong>+3 ~ +10分</strong></p>
          <p><span>获得互助双方好评</span><strong>+2分</strong></p>
          <p><span>高质量评价（内容详细、有帮助）</span><strong>+1分</strong></p>
          <p><span>连续完成多个任务（3单及以上）</span><strong>+2分</strong></p>
        </div>
        <div class="rule-box minus">
          <h3>减分项</h3>
          <p><span>无故取消任务（发起方）</span><strong>-5分</strong></p>
          <p><span>无故取消任务（接单方）</span><strong>-3分</strong></p>
          <p><span>收到差评</span><strong>-5分</strong></p>
          <p><span>违规行为（平台判定）</span><strong>-10 ~ -20分</strong></p>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import {
  CircleCheck,
  CircleClose,
  Comment,
  Document,
  Medal,
  Position,
} from '@element-plus/icons-vue'

import { getUserCredit } from '@/api/user'
import { useAuthStore } from '@/stores'
import type { CreditDTO } from '@/types'
import { creditRecords } from './profileMock'

const authStore = useAuthStore()
const credit = ref<CreditDTO | null>(null)

const score = computed(() => credit.value?.creditScore ?? authStore.user?.creditScore ?? 100)
const level = computed(() => credit.value?.creditLevel || 'Lv.3')
const completedRate = computed(() => Math.round((credit.value?.completedRate ?? 1) * 100))
const cancelledRate = computed(() => Math.round((credit.value?.cancelledRate ?? 0) * 100))
const nextScore = computed(() => Math.max(0, 120 - score.value))
const progress = computed(() => Math.min(100, Math.round((score.value / 120) * 100)))

const metrics = computed(() => [
  { label: '完成率', value: `${completedRate.value}%`, helper: '优秀', icon: CircleCheck, tone: 'green' },
  { label: '取消率', value: `${cancelledRate.value}%`, helper: '优秀', icon: CircleClose, tone: 'red' },
  { label: '评价数', value: 15, helper: '累计获得', icon: Comment, tone: 'orange' },
  { label: '发布任务数', value: 12, helper: '累计发布', icon: Position, tone: 'blue' },
  { label: '完成接单数', value: 8, helper: '累计完成', icon: CircleCheck, tone: 'green' },
])

onMounted(async () => {
  const userId = authStore.user?.userId
  if (!userId) return

  try {
    credit.value = await getUserCredit(userId)
  } catch {
    credit.value = null
  }
})
</script>

<style scoped>
.credit-page {
  display: grid;
  gap: 18px;
}

.credit-title,
.credit-hero,
.metric-grid article,
.credit-card {
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.06);
}

.credit-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 28px 34px;
}

.credit-title h1 {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0 0 8px;
  color: #111827;
  font-size: 28px;
}

.credit-title p {
  margin: 0;
  color: #64748b;
}

.credit-title a {
  color: #4169e1;
  font-weight: 800;
  text-decoration: none;
}

.credit-hero {
  display: grid;
  grid-template-columns: 160px 150px 160px minmax(0, 1fr);
  gap: 24px;
  align-items: center;
  padding: 30px 90px;
  background: linear-gradient(120deg, #eef4ff, #f3f0ff);
}

.credit-hero > div:not(.progress-area) {
  text-align: center;
}

.credit-hero span {
  color: #334155;
  font-weight: 700;
}

.credit-hero strong {
  display: block;
  margin: 10px 0;
  color: #4169e1;
  font-size: 46px;
  line-height: 1;
}

.credit-hero p {
  margin: 0;
  color: #64748b;
}

.medal {
  display: grid;
  width: 92px;
  height: 92px;
  place-items: center;
  margin: 0 auto;
  border: 8px solid #bcd3ff;
  border-radius: 50%;
  background: linear-gradient(135deg, #82a9ff, #4f77de);
  color: #fff;
  font-size: 40px;
}

.progress-area {
  padding-left: 36px;
  border-left: 1px solid #dbe3ef;
}

.progress-area h2 {
  margin: 0 0 18px;
  color: #1e293b;
  font-size: 18px;
}

.progress-area p {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
}

.progress-area b {
  color: #475569;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 18px;
}

.metric-grid article {
  display: flex;
  align-items: center;
  gap: 18px;
  min-height: 88px;
  padding: 18px 22px;
}

.metric-icon {
  display: grid;
  width: 48px;
  height: 48px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 50%;
  font-size: 24px;
}

.metric-icon.green {
  background: #e9f8ef;
  color: #35a968;
}

.metric-icon.red {
  background: #fff1f2;
  color: #e5484d;
}

.metric-icon.orange {
  background: #fff4e7;
  color: #f28c28;
}

.metric-icon.blue {
  background: #eaf1ff;
  color: #4169e1;
}

.metric-grid small,
.metric-grid strong,
.metric-grid em {
  display: block;
}

.metric-grid small {
  color: #64748b;
}

.metric-grid strong {
  margin-top: 5px;
  color: #111827;
  font-size: 24px;
}

.metric-grid em {
  margin-top: 4px;
  color: #35a968;
  font-style: normal;
}

.credit-columns {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 0.94fr);
  gap: 18px;
}

.credit-card {
  padding: 24px 28px;
}

.credit-card h2 {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 0 22px;
  color: #111827;
  font-size: 20px;
}

.record-table {
  display: grid;
}

.record-head,
.record-row {
  display: grid;
  grid-template-columns: 1.15fr 1.25fr 0.8fr 0.8fr;
  gap: 12px;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #edf1f7;
}

.record-head {
  color: #64748b;
  font-weight: 800;
}

.record-row {
  color: #475569;
}

.record-row strong {
  color: #22a766;
}

.record-row strong.negative {
  color: #ef4444;
}

.credit-card > button {
  display: block;
  margin: 18px auto 0;
  border: 0;
  background: transparent;
  color: #4169e1;
  font: inherit;
  font-weight: 800;
  cursor: pointer;
}

.rule-card {
  display: grid;
  gap: 14px;
}

.rule-box {
  padding: 16px 18px;
  border-radius: 8px;
}

.rule-box.plus {
  background: #effaf4;
}

.rule-box.minus {
  background: #fff1f2;
}

.rule-box h3 {
  margin: 0 0 12px;
  font-size: 16px;
}

.rule-box.plus h3 {
  color: #22a766;
}

.rule-box.minus h3 {
  color: #ef4444;
}

.rule-box p {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin: 8px 0;
  color: #475569;
}

.rule-box.plus p::before,
.rule-box.minus p::before {
  flex: 0 0 auto;
  font-weight: 800;
}

.rule-box.plus p::before {
  color: #22a766;
  content: "•";
}

.rule-box.minus p::before {
  color: #ef4444;
  content: "•";
}

.rule-box strong {
  flex: 0 0 auto;
}

.rule-box.plus strong {
  color: #111827;
}

.rule-box.minus strong {
  color: #ef4444;
}

@media (max-width: 1180px) {
  .credit-hero,
  .metric-grid,
  .credit-columns {
    grid-template-columns: 1fr;
  }

  .progress-area {
    padding-left: 0;
    border-left: 0;
  }
}
</style>

