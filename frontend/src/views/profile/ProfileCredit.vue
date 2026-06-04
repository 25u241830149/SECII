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
        <p>{{ scoreMessage }}</p>
      </div>
      <div class="medal">★</div>
      <div>
        <span>信用等级</span>
        <strong>{{ level }}</strong>
        <p>当前账户等级</p>
      </div>
      <div class="progress-area">
        <h2>{{ progressTitle }}</h2>
        <el-progress :percentage="progress" :show-text="false" color="#4169e1" />
        <p><span>{{ progressHint }}</span><b>{{ score }} / 100</b></p>
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
          <div v-for="record in creditRecords" :key="record.recordId" class="record-row">
            <span>{{ formatTime(record.createdAt) }}</span>
            <span>{{ record.reason }}</span>
            <strong :class="{ negative: record.delta < 0 }">
              {{ record.delta > 0 ? '+' : '' }}{{ record.delta }}
            </strong>
            <span>{{ record.scoreAfter }}</span>
          </div>
          <el-empty v-if="!creditRecords.length" description="暂无信用变动记录" :image-size="72" />
        </div>
        <p class="record-caption">展示最近 10 条信用变动记录</p>
      </section>

      <section class="credit-card rule-card">
        <h2><el-icon><Document /></el-icon> 加减分说明</h2>
        <div class="rule-box plus">
          <h3>加分项</h3>
          <p><span>发布的普通需求顺利完成</span><strong>+1分</strong></p>
          <p><span>接取的普通需求顺利完成</span><strong>+2分</strong></p>
          <p><span>收到五星评价</span><strong>+3分</strong></p>
          <p><span>收到四星评价</span><strong>+1分</strong></p>
        </div>
        <div class="rule-box minus">
          <h3>减分项</h3>
          <p><span>接单后、确认前撤回申请</span><strong>-2分</strong></p>
          <p><span>确认接单后主动退出</span><strong>-5分</strong></p>
          <p><span>发布者确认接单后取消需求</span><strong>-3分</strong></p>
          <p><span>收到二星 / 一星评价</span><strong>-2 / -4分</strong></p>
        </div>
        <p class="neutral-rule">收到三星评价不改变信用分；活动组队完成后不参与信用加分。</p>
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

const authStore = useAuthStore()
const credit = ref<CreditDTO | null>(null)

const score = computed(() => credit.value?.creditScore ?? authStore.user?.creditScore ?? 90)
const level = computed(() => credit.value?.creditLevel || '普通用户')
const completedRate = computed(() => Math.round((credit.value?.completedRate ?? 0) * 100))
const cancelledRate = computed(() => Math.round((credit.value?.cancelledRate ?? 0) * 100))
const creditRecords = computed(() => credit.value?.recentRecords ?? [])
const averageRating = computed(() => credit.value?.averageRating ?? null)
const finishedOrderCount = computed(() =>
  (credit.value?.completedOrderCount ?? 0) + (credit.value?.cancelledOrderCount ?? 0),
)
const progress = computed(() => Math.min(100, Math.max(0, score.value)))
const nextTier = computed(() => {
  if (score.value < 60) return { threshold: 60, name: '普通用户' }
  if (score.value < 80) return { threshold: 80, name: '可靠同学' }
  if (score.value < 90) return { threshold: 90, name: '诚信学生' }
  return null
})
const scoreMessage = computed(() => {
  if (score.value >= 90) return '信用表现优秀，请继续保持。'
  if (score.value >= 80) return '信用表现良好，继续积累可靠记录。'
  if (score.value >= 60) return '按约完成协作，可以逐步提升信用。'
  return '信用分较低，请优先完成已确认的协作。'
})
const progressTitle = computed(() =>
  nextTier.value
    ? `距离 ${nextTier.value.name} 还差 ${nextTier.value.threshold - score.value} 分`
    : '已达到最高信用等级',
)
const progressHint = computed(() =>
  nextTier.value ? `达到 ${nextTier.value.threshold} 分后升级为${nextTier.value.name}` : '信用分上限为 100 分',
)
const rateHelper = computed(() => finishedOrderCount.value ? `基于 ${finishedOrderCount.value} 次已结束接单` : '暂无已结束接单')

const metrics = computed(() => [
  { label: '完成率', value: `${completedRate.value}%`, helper: rateHelper.value, icon: CircleCheck, tone: 'green' },
  { label: '取消率', value: `${cancelledRate.value}%`, helper: rateHelper.value, icon: CircleClose, tone: 'red' },
  {
    label: '评价数',
    value: credit.value?.reviewCount ?? 0,
    helper: averageRating.value === null ? '暂无评分' : `平均 ${Number(averageRating.value).toFixed(1)} 分`,
    icon: Comment,
    tone: 'orange',
  },
  { label: '发布需求数', value: credit.value?.publishedTaskCount ?? 0, helper: '累计发布', icon: Position, tone: 'blue' },
  { label: '完成接单数', value: credit.value?.completedOrderCount ?? 0, helper: '累计完成', icon: CircleCheck, tone: 'green' },
])

const formatTime = (value: string) => new Date(value).toLocaleString('zh-CN', { hour12: false })

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

.record-caption {
  margin: 18px 0 0;
  color: #94a3b8;
  font-size: 13px;
  text-align: center;
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

.neutral-rule {
  margin: 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
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

