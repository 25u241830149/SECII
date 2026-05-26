<template>
  <section class="admin-page">
    <article class="toolbar-card">
      <div>
        <h2>用户管理</h2>
        <p>当前后端已提供封禁和实名认证复核入口，可按用户 ID 执行操作。</p>
      </div>
    </article>

    <section class="operation-grid">
      <article class="operation-card">
        <header>
          <h3>封禁用户</h3>
          <p>记录封禁原因并将用户状态置为不可用。</p>
        </header>
        <el-form label-position="top">
          <el-form-item label="用户 ID">
            <el-input-number v-model="banForm.userId" :min="1" controls-position="right" />
          </el-form-item>
          <el-form-item label="封禁天数">
            <el-input-number v-model="banForm.days" :min="1" :max="365" controls-position="right" />
          </el-form-item>
          <el-form-item label="封禁原因">
            <el-input v-model="banForm.reason" type="textarea" :rows="4" maxlength="255" show-word-limit />
          </el-form-item>
          <el-button type="danger" :loading="banSubmitting" @click="submitBan">执行封禁</el-button>
        </el-form>
      </article>

      <article class="operation-card">
        <header>
          <h3>实名认证审核</h3>
          <p>通过或驳回用户提交的学生认证材料。</p>
        </header>
        <el-form label-position="top">
          <el-form-item label="用户 ID">
            <el-input-number v-model="verifyForm.userId" :min="1" controls-position="right" />
          </el-form-item>
          <el-form-item label="审核结果">
            <el-radio-group v-model="verifyForm.approved">
              <el-radio-button :label="true">通过</el-radio-button>
              <el-radio-button :label="false">驳回</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="审核备注">
            <el-input v-model="verifyForm.remark" type="textarea" :rows="4" maxlength="255" show-word-limit />
          </el-form-item>
          <el-button type="primary" :loading="verifySubmitting" @click="submitVerify">保存审核结果</el-button>
        </el-form>
      </article>
    </section>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { banUser, verifyUser } from '@/api/admin'

const banSubmitting = ref(false)
const verifySubmitting = ref(false)

const banForm = reactive({
  userId: 1,
  days: 7,
  reason: '',
})

const verifyForm = reactive({
  userId: 1,
  approved: true,
  remark: '',
})

const submitBan = async () => {
  if (!banForm.reason.trim()) {
    ElMessage.warning('请填写封禁原因')
    return
  }

  banSubmitting.value = true
  try {
    await banUser(banForm.userId, {
      days: banForm.days,
      reason: banForm.reason.trim(),
    })
    ElMessage.success('用户已封禁')
  } finally {
    banSubmitting.value = false
  }
}

const submitVerify = async () => {
  verifySubmitting.value = true
  try {
    await verifyUser(verifyForm.userId, {
      approved: verifyForm.approved,
      remark: verifyForm.remark.trim(),
    })
    ElMessage.success('审核结果已保存')
  } finally {
    verifySubmitting.value = false
  }
}
</script>

<style scoped>
.admin-page {
  display: grid;
  gap: 18px;
}

.toolbar-card,
.operation-card {
  padding: 20px;
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 26px rgba(15, 23, 42, 0.06);
}

.toolbar-card h2,
.toolbar-card p,
.operation-card h3,
.operation-card p {
  margin: 0;
}

.toolbar-card h2,
.operation-card h3 {
  color: #111827;
  font-size: 20px;
}

.toolbar-card p,
.operation-card p {
  margin-top: 6px;
  color: #64748b;
}

.operation-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.operation-card header {
  margin-bottom: 18px;
}

@media (max-width: 900px) {
  .operation-grid {
    grid-template-columns: 1fr;
  }
}
</style>
