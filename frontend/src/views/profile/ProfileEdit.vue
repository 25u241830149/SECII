<template>
  <section class="profile-page">
    <header class="page-header">
      <div>
        <p>个人资料</p>
        <h1>编辑个人信息</h1>
        <span>完善联系方式与头像资料；真实姓名和学院来自注册实名认证流程，不在此处修改。</span>
      </div>
      <RouterLink class="danger-link" to="/account/delete">注销账号</RouterLink>
    </header>

    <el-card class="profile-card" shadow="never">
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="5" animated />
      </div>

      <el-form v-else class="profile-form" :model="form" :rules="rules" label-position="top" @submit.prevent>
        <div class="avatar-preview">
          <el-avatar :size="76" :src="previewAvatarUrl || undefined">
            {{ avatarFallback }}
          </el-avatar>
          <div>
            <strong>{{ form.nickname || '未设置昵称' }}</strong>
            <span>学号：{{ authStore.user?.studentId }}</span>
          </div>
        </div>

        <div class="form-grid">
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" size="large" maxlength="64" show-word-limit placeholder="请输入昵称" />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" size="large" maxlength="128" placeholder="alice@example.com，可为空" />
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" size="large" maxlength="11" placeholder="13800000000，可为空" />
          </el-form-item>

          <el-form-item label="真实姓名">
            <el-input :model-value="displayRealName" size="large" disabled placeholder="注册实名认证后显示" />
          </el-form-item>

          <el-form-item label="所属院系">
            <el-input :model-value="displayDepartment" size="large" disabled placeholder="注册实名认证后显示" />
          </el-form-item>
        </div>

        <el-form-item label="上传头像">
          <el-upload
            class="avatar-upload"
            :auto-upload="false"
            :limit="1"
            accept="image/jpeg,image/png,image/webp"
            :show-file-list="false"
            :on-change="handleAvatarChange"
          >
            <el-button :loading="uploadingAvatar">选择并上传头像</el-button>
            <template #tip>
              <span class="upload-tip">支持 JPG、PNG、WebP，文件大小不超过 5MB。上传成功后会更新头像预览，点击“保存资料”后写入数据库。</span>
            </template>
          </el-upload>
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
          title="公开资料会展示昵称、头像和院系；邮箱和手机号仅用于个人资料维护与平台管理。头像地址不在界面中展示。"
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
import type { FormRules, UploadFile, UploadProps } from 'element-plus'

import { uploadAvatar } from '@/api/upload'
import { useAuthStore, useUserStore } from '@/stores'
import { resolveAssetUrl } from '@/utils/asset'
import { userRoleLabels, verificationStatusLabels } from '@/types'

const authStore = useAuthStore()
const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const uploadingAvatar = ref(false)
const form = reactive({
  email: '',
  phone: '',
  nickname: '',
  avatarUrl: '',
})

const rules: FormRules = {
  nickname: [
    { required: true, message: '昵称不能为空', trigger: 'blur' },
    { max: 64, message: '昵称不能超过 64 个字符', trigger: 'blur' },
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
    { max: 128, message: '邮箱不能超过 128 个字符', trigger: 'blur' },
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入 11 位中国大陆手机号', trigger: 'blur' },
  ],
  avatarUrl: [
    {
      validator: (_rule, value: string, callback) => {
        if (!value || value.startsWith('/uploads/avatars/')) {
          callback()
          return
        }
        try {
          const url = new URL(value)
          if (url.protocol === 'http:' || url.protocol === 'https:') {
            callback()
            return
          }
        } catch {
          // fall through
        }
        callback(new Error('头像 URL 格式不正确'))
      },
      trigger: 'blur',
    },
  ],
}

const currentUserId = computed(() => authStore.user?.userId)
const avatarFallback = computed(() => (form.nickname || authStore.user?.nickname || 'C').slice(0, 1))
const previewAvatarUrl = computed(() => resolveAssetUrl(form.avatarUrl))
const displayRealName = computed(() => userStore.profile?.realName || authStore.user?.realName || '')
const displayDepartment = computed(() => userStore.profile?.department || authStore.user?.department || '')
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
  form.email = profile?.email || authStore.user?.email || ''
  form.phone = profile?.phone || authStore.user?.phone || ''
  form.nickname = profile?.nickname || authStore.user?.nickname || ''
  form.avatarUrl = profile?.avatarUrl || authStore.user?.avatarUrl || ''
}

const isAllowedAvatarType = (file: File) => ['image/jpeg', 'image/png', 'image/webp'].includes(file.type)

const handleAvatarChange: UploadProps['onChange'] = async (uploadFile: UploadFile) => {
  const rawFile = uploadFile.raw
  if (!rawFile) return

  if (!isAllowedAvatarType(rawFile)) {
    ElMessage.warning('头像仅支持 JPG、PNG、WebP 图片')
    return
  }
  if (rawFile.size > 5 * 1024 * 1024) {
    ElMessage.warning('头像图片不能超过 5MB')
    return
  }

  uploadingAvatar.value = true
  try {
    const result = await uploadAvatar(rawFile)
    form.avatarUrl = result.fileUrl
    ElMessage.success('头像上传成功，请保存资料')
  } finally {
    uploadingAvatar.value = false
  }
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
  const email = form.email.trim()
  const phone = form.phone.trim()
  const avatarUrl = form.avatarUrl.trim()

  if (!nickname) {
    ElMessage.warning('昵称不能为空')
    return
  }

  if (email && !/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email)) {
    ElMessage.warning('邮箱格式不正确')
    return
  }

  if (phone && !/^1[3-9]\d{9}$/.test(phone)) {
    ElMessage.warning('请输入 11 位中国大陆手机号')
    return
  }

  if (avatarUrl && !avatarUrl.startsWith('/uploads/avatars/')) {
    try {
      const url = new URL(avatarUrl)
      if (url.protocol !== 'http:' && url.protocol !== 'https:') {
        ElMessage.warning('头像 URL 格式不正确')
        return
      }
    } catch {
      ElMessage.warning('头像 URL 格式不正确')
      return
    }
  }

  saving.value = true
  try {
    const profile = await userStore.updateProfile(currentUserId.value, {
      email: email || undefined,
      phone: phone || undefined,
      nickname,
      avatarUrl: avatarUrl || undefined,
    })

    if (authStore.user) {
      authStore.setSession({
        token: authStore.token,
        user: {
          ...authStore.user,
          email: profile.email,
          phone: profile.phone,
          nickname: profile.nickname,
          realName: authStore.user.realName,
          department: authStore.user.department,
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

.avatar-upload {
  width: 100%;
}

.upload-tip {
  display: block;
  margin-top: 6px;
  color: #8b95a5;
  font-size: 12px;
  line-height: 1.5;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
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

  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
