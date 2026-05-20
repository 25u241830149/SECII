<template>
  <section class="profile-page">
    <header class="page-header">
      <div>
        <p>个人资料</p>
        <h1>编辑公开资料</h1>
        <span>当前阶段支持修改昵称和头像地址，更多资料字段需等待后端 schema 扩展。</span>
      </div>
      <RouterLink class="danger-link" to="/account/delete">注销账号</RouterLink>
    </header>

    <el-card class="profile-card" shadow="never">
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="5" animated />
      </div>

      <el-form v-else class="profile-form" :model="form" label-position="top" @submit.prevent>
        <div class="avatar-preview">
          <el-avatar :size="76" :src="form.avatarUrl || undefined">
            {{ avatarFallback }}
          </el-avatar>
          <div>
            <strong>{{ form.nickname || '未设置昵称' }}</strong>
            <span>学号：{{ authStore.user?.studentId }}</span>
          </div>
        </div>

        <el-form-item label="昵称">
          <el-input v-model="form.nickname" size="large" maxlength="30" show-word-limit />
        </el-form-item>

        <el-form-item label="头像 URL">
          <el-input v-model="form.avatarUrl" size="large" placeholder="https://example.com/avatar.png，可为空" />
        </el-form-item>

        <div class="readonly-grid">
          <div>
            <span>角色</span>
            <strong>{{ roleLabel }}</strong>
          </div>
          <div>
            <span>信用分</span>
            <strong>{{ userStore.profile?.creditScore ?? authStore.user?.creditScore ?? '-' }}</strong>
          </div>
          <div>
            <span>实名认证</span>
            <strong>{{ verificationLabel }}</strong>
          </div>
        </div>

        <el-alert
          type="info"
          :closable="false"
          show-icon
          title="邮箱、手机号、学院等字段当前后端暂未入库，本页不展示可编辑入口。"
        />

        <div class="actions">
          <el-button @click="resetForm">重置</el-button>
          <el-button type="primary" :loading="saving" @click="saveProfile">保存资料</el-button>
        </div>
      </el-form>
    </el-card>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { useAuthStore, useUserStore } from '@/stores'
import { userRoleLabels, verificationStatusLabels } from '@/types'

const authStore = useAuthStore()
const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const form = reactive({
  nickname: '',
  avatarUrl: '',
})

const currentUserId = computed(() => authStore.user?.userId)
const avatarFallback = computed(() => (form.nickname || authStore.user?.nickname || 'C').slice(0, 1))
const roleLabel = computed(() => {
  const role = userStore.profile?.role || authStore.user?.role
  return role ? userRoleLabels[role] : '-'
})
const verificationLabel = computed(() => {
  const status = userStore.profile?.verificationStatus || authStore.user?.verificationStatus
  return status ? verificationStatusLabels[status] : '-'
})

const syncForm = () => {
  const profile = userStore.profile
  form.nickname = profile?.nickname || authStore.user?.nickname || ''
  form.avatarUrl = profile?.avatarUrl || authStore.user?.avatarUrl || ''
}

onMounted(async () => {
  if (!currentUserId.value) {
    ElMessage.warning('请先登录')
    return
  }

  loading.value = true
  try {
    await userStore.fetchProfile(currentUserId.value)
    syncForm()
  } finally {
    loading.value = false
  }
})

const resetForm = () => {
  syncForm()
}

const saveProfile = async () => {
  if (!currentUserId.value) {
    ElMessage.warning('请先登录')
    return
  }

  const nickname = form.nickname.trim()
  const avatarUrl = form.avatarUrl.trim()

  if (!nickname) {
    ElMessage.warning('昵称不能为空')
    return
  }

  saving.value = true
  try {
    const profile = await userStore.updateProfile(currentUserId.value, {
      nickname,
      avatarUrl,
    })

    if (authStore.user) {
      authStore.setSession({
        token: authStore.token,
        user: {
          ...authStore.user,
          nickname: profile.nickname,
          avatarUrl: profile.avatarUrl,
          creditScore: profile.creditScore,
          verificationStatus: profile.verificationStatus,
        },
      })
    }

    syncForm()
    ElMessage.success('资料已保存')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.profile-page {
  display: grid;
  gap: 18px;
}

.page-header,
.profile-card {
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.055);
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding: 28px;
}

.page-header p {
  margin: 0 0 8px;
  color: #1268ed;
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

.danger-link {
  color: #dc2626;
  font-weight: 700;
  text-decoration: none;
  white-space: nowrap;
}

.profile-card {
  max-width: 760px;
}

.loading-state {
  padding: 8px;
}

.profile-form {
  display: grid;
  gap: 8px;
}

.avatar-preview {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 10px;
}

.avatar-preview strong,
.avatar-preview span {
  display: block;
}

.avatar-preview strong {
  color: #111827;
  font-size: 20px;
}

.avatar-preview span {
  margin-top: 4px;
  color: #687386;
}

.readonly-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin: 4px 0 12px;
}

.readonly-grid div {
  padding: 14px;
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #f8fbff;
}

.readonly-grid span,
.readonly-grid strong {
  display: block;
}

.readonly-grid span {
  color: #6b7280;
  font-size: 13px;
}

.readonly-grid strong {
  margin-top: 6px;
  color: #111827;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 10px;
}

@media (max-width: 720px) {
  .page-header {
    flex-direction: column;
  }

  .readonly-grid {
    grid-template-columns: 1fr;
  }
}
</style>
