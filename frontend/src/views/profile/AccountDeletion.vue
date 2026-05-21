<template>
  <section class="delete-page">
    <header class="delete-header">
      <h1>账号注销</h1>
      <p><el-icon><Warning /></el-icon> 注销操作不可逆，请仔细阅读以下内容并谨慎操作</p>
    </header>

    <section class="impact-card">
      <div class="impact-title">
        <el-icon><WarningFilled /></el-icon>
        <strong>注销后将产生以下影响</strong>
      </div>
      <div class="impact-grid">
        <article>
          <span class="impact-icon red"><el-icon><Coin /></el-icon></span>
          <div>
            <h2>数据不可恢复</h2>
            <p>注销后，您的所有数据将被永久删除，且无法恢复。</p>
          </div>
        </article>
        <article>
          <span class="impact-icon orange"><el-icon><Document /></el-icon></span>
          <div>
            <h2>历史记录将被清除</h2>
            <p>您的发布、接单、评价、收藏等所有历史记录将被清除。</p>
          </div>
        </article>
        <article>
          <span class="impact-icon blue"><el-icon><Medal /></el-icon></span>
          <div>
            <h2>信用信息无法找回</h2>
            <p>您的信用分、等级及相关权益将被永久清除。</p>
          </div>
        </article>
      </div>
    </section>

    <section class="confirm-card">
      <h2>注销前请确认以下事项</h2>
      <ul>
        <li v-for="item in checklist" :key="item">{{ item }}</li>
      </ul>
    </section>

    <section class="input-card">
      <h2>请输入您的昵称进行确认</h2>
      <p>请输入您的昵称“{{ displayName }}”或输入“确认注销”以继续</p>
      <el-input
        v-model="confirmation"
        maxlength="10"
        show-word-limit
        placeholder="请输入昵称 或 确认注销"
      />
    </section>

    <footer class="delete-actions">
      <p><el-icon><InfoFilled /></el-icon> 如有任何疑问，请联系客服或查看 <a href="/help">帮助中心</a></p>
      <div>
        <el-button size="large" @click="router.push('/profile')">取消</el-button>
        <el-button
          type="danger"
          size="large"
          :disabled="!canSubmit"
          :loading="deleting"
          @click="deleteCurrentAccount"
        >
          确认注销账号
        </el-button>
      </div>
    </footer>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Coin,
  Document,
  InfoFilled,
  Medal,
  Warning,
  WarningFilled,
} from '@element-plus/icons-vue'

import { deleteAccount } from '@/api/user'
import { useAuthStore, useUserStore } from '@/stores'

const router = useRouter()
const authStore = useAuthStore()
const userStore = useUserStore()

const confirmation = ref('')
const deleting = ref(false)

const displayName = computed(() => authStore.user?.nickname || 'lyh')
const canSubmit = computed(() => {
  const value = confirmation.value.trim()
  return value === displayName.value || value === '确认注销'
})

const checklist = [
  '我已知晓并理解账号注销的所有后果',
  '我已确认账号内无进行中的订单、任务或未完成的交易',
  '我已确认账号内无未提现的余额或奖励',
  '我已备份或不再需要账号内的任何信息',
  '我自愿申请注销账号，并知晓注销后无法恢复',
]

const deleteCurrentAccount = async () => {
  const currentUser = authStore.user

  if (!currentUser) {
    ElMessage.warning('请先登录')
    return
  }

  if (!canSubmit.value) {
    ElMessage.warning('请输入当前昵称或“确认注销”完成确认')
    return
  }

  try {
    await ElMessageBox.confirm(
      '确认注销当前账号？该操作会清空本地登录状态，且无法恢复。',
      '账号注销确认',
      {
        type: 'warning',
        confirmButtonText: '确认注销',
        cancelButtonText: '取消',
      },
    )
  } catch {
    return
  }

  deleting.value = true
  try {
    await deleteAccount(currentUser.userId)
    userStore.setProfile(null)
    authStore.logout()
    ElMessage.success('账号已注销')
    await router.replace('/login')
  } finally {
    deleting.value = false
  }
}
</script>

<style scoped>
.delete-page {
  display: grid;
  gap: 18px;
}

.delete-header,
.impact-card,
.confirm-card,
.input-card {
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.06);
}

.delete-header {
  padding: 28px 34px 8px;
  box-shadow: none;
}

.delete-header h1 {
  margin: 0 0 10px;
  color: #111827;
  font-size: 30px;
}

.delete-header p {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  color: #64748b;
}

.delete-header :deep(.el-icon) {
  color: #ef4444;
}

.impact-card {
  padding: 28px 30px;
  border-color: #fecaca;
  background: linear-gradient(120deg, #fff7f7, #fff);
}

.impact-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  color: #dc2626;
  font-size: 18px;
}

.impact-title :deep(.el-icon) {
  font-size: 28px;
}

.impact-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 22px;
}

.impact-grid article {
  display: flex;
  align-items: center;
  gap: 18px;
  min-width: 0;
}

.impact-grid article + article {
  border-left: 1px solid #e5ebf4;
  padding-left: 22px;
}

.impact-icon {
  display: grid;
  width: 62px;
  height: 62px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 50%;
  font-size: 30px;
}

.impact-icon.red {
  background: #fee2e2;
  color: #e5484d;
}

.impact-icon.orange {
  background: #fff4e7;
  color: #f28c28;
}

.impact-icon.blue {
  background: #eaf1ff;
  color: #4169e1;
}

.impact-grid h2 {
  margin: 0 0 8px;
  color: #111827;
  font-size: 17px;
}

.impact-grid p {
  margin: 0;
  color: #64748b;
  line-height: 1.6;
}

.confirm-card,
.input-card {
  padding: 24px 28px;
}

.confirm-card h2,
.input-card h2 {
  margin: 0 0 16px;
  color: #111827;
  font-size: 20px;
}

.confirm-card ul {
  display: grid;
  gap: 12px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.confirm-card li {
  color: #475569;
}

.confirm-card li::before {
  margin-right: 10px;
  color: #54b889;
  content: "✓";
  font-weight: 800;
}

.input-card p {
  margin: -4px 0 14px;
  color: #64748b;
}

.input-card :deep(.el-input) {
  max-width: 660px;
}

.delete-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.delete-actions p {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 52px;
  flex: 1;
  margin: 0;
  padding: 0 24px;
  border-radius: 8px;
  background: #f5f7fb;
  color: #64748b;
}

.delete-actions a {
  color: #4169e1;
  font-weight: 800;
  text-decoration: none;
}

.delete-actions > div {
  display: flex;
  gap: 14px;
}

.delete-actions :deep(.el-button) {
  min-width: 124px;
}

.delete-actions :deep(.el-button--danger) {
  min-width: 170px;
}

@media (max-width: 980px) {
  .impact-grid {
    grid-template-columns: 1fr;
  }

  .impact-grid article + article {
    border-left: 0;
    padding-left: 0;
  }

  .delete-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .delete-actions > div {
    justify-content: flex-end;
  }
}
</style>
