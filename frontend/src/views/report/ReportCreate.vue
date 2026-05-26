<template>
  <section class="report-page">
    <article class="report-card">
      <header>
        <h1>提交举报</h1>
        <p>请填写被举报对象和原因，管理员会在后台处理并保留记录。</p>
      </header>

      <el-form label-position="top" class="report-form" @submit.prevent>
        <el-form-item label="举报对象类型">
          <el-select v-model="form.targetType">
            <el-option v-for="type in targetTypes" :key="type" :label="reportTargetTypeLabels[type]" :value="type" />
          </el-select>
        </el-form-item>
        <el-form-item label="对象 ID">
          <el-input-number v-model="form.targetId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="被举报用户 ID">
          <el-input-number v-model="form.targetUserId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="举报原因">
          <el-input
            v-model="form.reason"
            type="textarea"
            :rows="5"
            maxlength="255"
            show-word-limit
            placeholder="请说明违规行为、相关订单或沟通背景"
          />
        </el-form-item>
        <div class="actions">
          <el-button @click="router.back()">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submit">提交举报</el-button>
        </div>
      </el-form>
    </article>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'

import { createReport } from '@/api/report'
import { reportTargetTypeLabels } from '@/types'
import type { ReportCreateRequest, ReportTargetType } from '@/types'

const route = useRoute()
const router = useRouter()

const targetTypes: ReportTargetType[] = ['USER', 'TASK', 'ORDER', 'POST', 'COMMENT']
const submitting = ref(false)
const form = reactive<ReportCreateRequest>({
  targetType: normalizeTargetType(route.query.targetType),
  targetId: Number(route.query.targetId) || 1,
  targetUserId: Number(route.query.targetUserId) || undefined,
  reason: '',
})

function normalizeTargetType(value: unknown): ReportTargetType {
  return typeof value === 'string' && targetTypes.includes(value as ReportTargetType)
    ? (value as ReportTargetType)
    : 'USER'
}

const submit = async () => {
  if (!form.targetId || form.targetId <= 0) {
    ElMessage.warning('请填写对象 ID')
    return
  }
  if (form.targetType !== 'USER' && (!form.targetUserId || form.targetUserId <= 0)) {
    ElMessage.warning('请填写被举报用户 ID')
    return
  }
  if (!form.reason.trim()) {
    ElMessage.warning('请填写举报原因')
    return
  }

  submitting.value = true
  try {
    await createReport({ ...form, reason: form.reason.trim() })
    ElMessage.success('举报已提交')
    router.push('/messages')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.report-page {
  display: grid;
  justify-items: center;
}

.report-card {
  width: min(760px, 100%);
  padding: 26px;
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.07);
}

.report-card h1,
.report-card p {
  margin: 0;
}

.report-card h1 {
  color: #111827;
  font-size: 26px;
}

.report-card p {
  margin-top: 8px;
  color: #64748b;
}

.report-form {
  margin-top: 22px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
