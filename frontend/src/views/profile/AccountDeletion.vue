<template>
  <section class="account-page">
    <header class="page-header">
      <p>账户安全</p>
      <h1>注销账号</h1>
      <span>注销会退出当前账号，并将账号交给后端执行逻辑删除。请谨慎操作。</span>
    </header>

    <el-card class="danger-card" shadow="never">
      <el-alert
        type="warning"
        :closable="false"
        show-icon
        title="注销前请确认没有未完成订单。当前 Sprint 1 后端暂未接入订单校验，后续会按 API 规范补充。"
      />

      <div class="risk-list">
        <h2>注销后会发生什么？</h2>
        <p>你将无法继续使用当前 token 访问受保护页面。</p>
        <p>公开资料会从普通查询入口隐藏，历史业务数据后续按订单/评价模块规则处理。</p>
        <p>如果误操作，需要联系管理员或重新注册账号。</p>
      </div>

      <el-form class="delete-form" label-position="top" @submit.prevent>
        <el-form-item label="请输入当前学号确认">
          <el-input
            v-model="confirmation"
            size="large"
            :placeholder="authStore.user?.studentId || '当前登录学号'"
          />
        </el-form-item>

        <div class="actions">
          <RouterLink class="back-link" to="/profile/edit">返回资料编辑</RouterLink>
          <el-button
            type="danger"
            :disabled="!canSubmit"
            :loading="deleting"
            @click="deleteCurrentAccount"
          >
            确认注销
          </el-button>
        </div>
      </el-form>
    </el-card>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

import { deleteAccount } from '@/api/user'
import { useAuthStore } from '@/stores'

const router = useRouter()
const authStore = useAuthStore()

const confirmation = ref('')
const deleting = ref(false)
const canSubmit = computed(() => {
  return Boolean(authStore.user?.studentId && confirmation.value.trim() === authStore.user.studentId)
})

const deleteCurrentAccount = async () => {
  const currentUser = authStore.user

  if (!currentUser) {
    ElMessage.warning('请先登录')
    return
  }

  if (!canSubmit.value) {
    ElMessage.warning('请输入当前学号完成确认')
    return
  }

  try {
    await ElMessageBox.confirm(
      '确认注销当前账号？该操作会清空本地登录状态。',
      '注销账号确认',
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
    authStore.logout()
    ElMessage.success('账号已注销')
    await router.replace('/login')
  } finally {
    deleting.value = false
  }
}
</script>

<style scoped>
.account-page {
  display: grid;
  gap: 18px;
}

.page-header,
.danger-card {
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.055);
}

.page-header {
  padding: 28px;
}

.page-header p {
  margin: 0 0 8px;
  color: #dc2626;
  font-weight: 800;
}

.page-header h1 {
  margin: 0 0 8px;
  color: #111827;
  font-size: 28px;
}

.page-header span {
  color: #687386;
}

.danger-card {
  max-width: 760px;
}

.risk-list {
  margin: 22px 0;
  padding: 18px;
  border: 1px solid #fee2e2;
  border-radius: 8px;
  background: #fff7f7;
}

.risk-list h2 {
  margin: 0 0 12px;
  color: #991b1b;
  font-size: 18px;
}

.risk-list p {
  margin: 8px 0;
  color: #6b2730;
}

.delete-form {
  display: grid;
  gap: 10px;
}

.actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.back-link {
  color: #1268ed;
  font-weight: 700;
  text-decoration: none;
}

@media (max-width: 720px) {
  .actions {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
