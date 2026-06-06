<template>
  <section class="profile-edit-page">
    <div class="profile-edit-grid">
      <el-card class="profile-card edit-panel" shadow="never">
        <header class="edit-panel-header">
          <div>
            <h1><el-icon><CircleCheck /></el-icon> 编辑个人信息</h1>
            <p>完善联系方式与头像资料；真实姓名和学院来自注册实名认证流程，不在此处修改。</p>
          </div>
          <RouterLink class="danger-link" to="/account/delete">注销账号</RouterLink>
        </header>

        <div v-if="loading" class="loading-state">
          <el-skeleton :rows="7" animated />
        </div>

        <el-form v-else class="profile-form" :model="form" :rules="rules" label-position="top" @submit.prevent>
          <div class="avatar-preview">
            <el-avatar :size="76" :src="previewAvatarUrl || undefined">
              {{ avatarFallback }}
            </el-avatar>
            <div>
              <strong>{{ form.nickname || '未设置昵称' }}</strong>
              <span>学号：{{ studentIdLabel }}</span>
            </div>
          </div>

          <div class="form-grid">
            <el-form-item label="昵称" prop="nickname" required>
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
              <el-button :loading="uploadingAvatar">
                <el-icon><Upload /></el-icon>
                选择并上传头像
              </el-button>
              <template #tip>
                <span class="upload-tip">支持 JPG、PNG、WebP，文件大小不超过 5MB。上传成功后会更新头像预览，点击“保存资料”后写入数据库。</span>
              </template>
            </el-upload>
          </el-form-item>

          <div class="readonly-grid">
            <div class="readonly-tile">
              <span class="tile-icon blue"><el-icon><User /></el-icon></span>
              <div>
                <span>角色</span>
                <strong>{{ roleLabel }}</strong>
              </div>
            </div>
            <div class="readonly-tile">
              <span class="tile-icon rose"><el-icon><Medal /></el-icon></span>
              <div>
                <span>信用分</span>
                <strong>{{ creditScore }}</strong>
              </div>
            </div>
            <div class="readonly-tile">
              <span class="tile-icon green"><el-icon><CircleCheck /></el-icon></span>
              <div>
                <span>实名认证</span>
                <strong>{{ verificationLabel }}</strong>
              </div>
            </div>
          </div>

          <el-alert
            class="inline-public-tip"
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

      <aside class="profile-edit-aside">
        <el-card class="profile-card security-card" shadow="never">
          <header>
            <div>
              <h2><el-icon><Lock /></el-icon> 账号安全</h2>
              <p>定期更新密码，保护账号安全。修改成功后，下次登录请使用新密码。</p>
            </div>
            <el-icon class="security-watermark"><Lock /></el-icon>
          </header>
          <el-button type="primary" plain @click="openPasswordDialog">
            <el-icon><Key /></el-icon>
            修改密码
          </el-button>
        </el-card>

        <el-card class="profile-card completion-card" shadow="never">
          <h2>资料完整度</h2>
          <div class="completion-content">
            <div class="completion-ring" :style="completionRingStyle">
              <span>{{ completionPercent }}%</span>
            </div>
            <div class="completion-copy">
              <strong>{{ completionTitle }}</strong>
              <p>{{ completionDescription }}</p>
              <div class="completion-checks">
                <span
                  v-for="item in completionItems"
                  :key="item.key"
                  :class="{ completed: item.completed }"
                >
                  <el-icon><CircleCheck /></el-icon>
                  {{ item.label }}
                </span>
              </div>
            </div>
          </div>
          <button class="suggestion-toggle" type="button" @click="showCompletionTips = !showCompletionTips">
            查看完善建议
            <span>{{ showCompletionTips ? '收起' : '展开' }}</span>
          </button>
          <ul v-if="showCompletionTips" class="completion-tips">
            <li v-for="tip in completionTips" :key="tip">{{ tip }}</li>
          </ul>
        </el-card>

        <section class="public-info-card">
          <h2><el-icon><InfoFilled /></el-icon> 公开展示信息说明</h2>
          <p>他人查看你的主页时，将展示以下信息：</p>
          <ul>
            <li>昵称、头像、所属院系</li>
            <li>信用分与实名认证状态</li>
          </ul>
        </section>

        <el-card class="profile-card preview-card" shadow="never">
          <header>
            <h2><el-icon><View /></el-icon> 个人主页预览</h2>
            <RouterLink to="/profile">查看我的主页</RouterLink>
          </header>
          <div class="preview-profile">
            <el-avatar :size="68" :src="previewAvatarUrl || undefined">
              {{ avatarFallback }}
            </el-avatar>
            <div>
              <strong>{{ form.nickname || '未设置昵称' }}</strong>
              <span>{{ displayDepartment || '暂未填写院系' }}</span>
              <div class="preview-badges">
                <em>信用分 {{ creditScore }}</em>
                <em :class="{ verified: verificationPassed }">实名认证 {{ verificationLabel }}</em>
              </div>
            </div>
          </div>
        </el-card>
      </aside>
    </div>

    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="420px"
      destroy-on-close
    >
      <el-form class="password-form" label-position="top" @submit.prevent>
        <el-form-item label="当前密码">
          <el-input
            v-model="passwordForm.currentPassword"
            type="password"
            placeholder="请输入当前密码"
            autocomplete="current-password"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            autocomplete="new-password"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            autocomplete="new-password"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="changingPassword" @click="submitPasswordChange">
          保存修改
        </el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormRules, UploadFile, UploadProps } from 'element-plus'
import { CircleCheck, InfoFilled, Key, Lock, Medal, Upload, User, View } from '@element-plus/icons-vue'

import { changePassword } from '@/api/user'
import { uploadAvatar } from '@/api/upload'
import { useAuthStore, useUserStore } from '@/stores'
import { resolveAssetUrl } from '@/utils/asset'
import { userRoleLabels, verificationStatusLabels } from '@/types'

const authStore = useAuthStore()
const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const uploadingAvatar = ref(false)
const passwordDialogVisible = ref(false)
const changingPassword = ref(false)
const showCompletionTips = ref(false)
const form = reactive({
  email: '',
  phone: '',
  nickname: '',
  avatarUrl: '',
})
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
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
const savedProfile = computed(() => userStore.profile || authStore.user)
const savedNickname = computed(() => savedProfile.value?.nickname || '')
const savedEmail = computed(() => savedProfile.value?.email || '')
const savedPhone = computed(() => savedProfile.value?.phone || '')
const savedAvatarUrl = computed(() => savedProfile.value?.avatarUrl || '')
const avatarFallback = computed(() => (form.nickname || authStore.user?.nickname || 'C').slice(0, 1))
const previewAvatarUrl = computed(() => resolveAssetUrl(form.avatarUrl))
const studentIdLabel = computed(() => authStore.user?.studentId || '-')
const displayRealName = computed(() => userStore.profile?.realName || authStore.user?.realName || '')
const displayDepartment = computed(() => userStore.profile?.department || authStore.user?.department || '')
const creditScore = computed(() => userStore.profile?.creditScore ?? authStore.user?.creditScore ?? '-')
const roleLabel = computed(() => {
  const role = userStore.profile?.role || authStore.user?.role
  return role ? userRoleLabels[role] : '-'
})
const verificationStatus = computed(() => userStore.profile?.verificationStatus || authStore.user?.verificationStatus)
const verificationLabel = computed(() => {
  const status = verificationStatus.value
  return status ? verificationStatusLabels[status] : '-'
})
const verificationPassed = computed(() => verificationStatus.value === 'APPROVED')
const completionItems = computed(() => [
  {
    key: 'basic',
    label: '基本信息',
    weight: 30,
    completed: Boolean(savedNickname.value.trim() && (savedEmail.value.trim() || savedPhone.value.trim())),
    tip: '补充昵称，并至少填写邮箱或手机号，方便平台联系。',
  },
  {
    key: 'department',
    label: '院系信息',
    weight: 20,
    completed: Boolean(displayDepartment.value),
    tip: '完成实名认证后会同步展示所属院系。',
  },
  {
    key: 'verification',
    label: '实名认证',
    weight: 25,
    completed: verificationPassed.value,
    tip: '提交并通过学生实名认证，提升账号可信度。',
  },
  {
    key: 'avatar',
    label: '头像设置',
    weight: 25,
    completed: Boolean(savedAvatarUrl.value),
    tip: '上传头像，让其他同学更容易识别你。',
  },
])
const completionPercent = computed(() => completionItems.value.reduce((total, item) => total + (item.completed ? item.weight : 0), 0))
const completionRingStyle = computed(() => ({ '--completion': `${completionPercent.value}%` }))
const completionTitle = computed(() => {
  if (completionPercent.value >= 90) return '资料完整度优秀'
  if (completionPercent.value >= 70) return '资料完整度良好'
  if (completionPercent.value >= 40) return '资料仍可完善'
  return '资料待完善'
})
const completionDescription = computed(() => {
  if (completionPercent.value >= 90) return '资料完整清晰，更容易获得互助机会。'
  if (completionPercent.value >= 70) return '完善资料可提升可信度，获得更多互助机会。'
  return '建议补充关键信息，提升他人对你的信任。'
})
const completionTips = computed(() => {
  const tips = completionItems.value.filter((item) => !item.completed).map((item) => item.tip)
  return tips.length ? tips : ['当前资料已经较完整，继续保持即可。']
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

const resetPasswordForm = () => {
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

const openPasswordDialog = () => {
  resetPasswordForm()
  passwordDialogVisible.value = true
}

const submitPasswordChange = async () => {
  const currentPassword = passwordForm.currentPassword
  const newPassword = passwordForm.newPassword
  const confirmPassword = passwordForm.confirmPassword

  if (!currentPassword || !newPassword || !confirmPassword) {
    ElMessage.warning('请完整填写密码信息')
    return
  }

  if (newPassword !== confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }

  if (newPassword === currentPassword) {
    ElMessage.warning('新密码不能与当前密码相同')
    return
  }

  changingPassword.value = true
  try {
    await changePassword({ currentPassword, newPassword, confirmPassword })
    ElMessage.success('密码已修改')
    passwordDialogVisible.value = false
    resetPasswordForm()
  } finally {
    changingPassword.value = false
  }
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
.profile-edit-page {
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.profile-edit-grid {
  display: grid;
  grid-template-columns: minmax(560px, 1.28fr) minmax(360px, 0.82fr);
  gap: 16px;
  align-items: stretch;
  height: 100%;
  min-height: 0;
}

.profile-card,
.public-info-card {
  border: 1px solid #e2e9f5;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.055);
}

.profile-card :deep(.el-card__body) {
  padding: 18px 20px;
}

.edit-panel :deep(.el-card__body) {
  padding: 22px 24px 18px;
}

.edit-panel {
  min-height: 0;
  overflow-y: auto;
}

.edit-panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 14px;
}

.edit-panel-header h1,
.security-card h2,
.completion-card h2,
.public-info-card h2,
.preview-card h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  color: #111827;
  font-weight: 800;
}

.edit-panel-header h1 {
  font-size: 24px;
}

.edit-panel-header h1 :deep(.el-icon),
.completion-card h2 :deep(.el-icon),
.preview-card h2 :deep(.el-icon) {
  color: #1677ff;
}

.edit-panel-header p,
.security-card p,
.completion-card p,
.public-info-card p {
  margin: 8px 0 0;
  color: #687386;
  line-height: 1.65;
}

.danger-link {
  color: #f04444;
  font-weight: 800;
  text-decoration: none;
  white-space: nowrap;
}

.profile-edit-aside {
  display: flex;
  flex-direction: column;
  gap: 18px;
  height: 100%;
  min-height: 0;
  overflow-y: auto;
  padding-right: 2px;
  scrollbar-gutter: stable;
}

.profile-edit-aside > * {
  flex-shrink: 0;
}

.profile-edit-aside::-webkit-scrollbar {
  width: 8px;
}

.profile-edit-aside::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: #c8d3e3;
}

.profile-edit-aside::-webkit-scrollbar-track {
  background: transparent;
}

.profile-edit-aside .profile-card :deep(.el-card__body) {
  padding: 22px 24px;
}

.loading-state {
  padding: 8px 0;
}

.profile-form {
  display: grid;
  gap: 6px;
}

.avatar-preview {
  display: flex;
  align-items: center;
  gap: 18px;
  margin: 0 0 8px;
}

.avatar-preview strong,
.avatar-preview span {
  display: block;
}

.avatar-preview strong {
  color: #111827;
  font-size: 22px;
  line-height: 1.2;
}

.avatar-preview span {
  margin-top: 6px;
  color: #66758d;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 18px;
}

.profile-form :deep(.el-form-item) {
  margin-bottom: 12px;
}

.profile-form :deep(.el-form-item__label) {
  color: #39465b;
  font-weight: 600;
}

.profile-form :deep(.el-input__wrapper) {
  border-radius: 6px;
  box-shadow: 0 0 0 1px #dbe4f1 inset;
}

.avatar-upload {
  width: 100%;
}

.avatar-upload :deep(.el-button) {
  min-width: 168px;
}

.upload-tip {
  display: block;
  margin-top: 7px;
  color: #8b95a5;
  font-size: 12px;
  line-height: 1.55;
}

.readonly-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin: 0 0 10px;
}

.readonly-tile {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 62px;
  padding: 12px;
  border: 1px solid #e1e8f4;
  border-radius: 8px;
  background: linear-gradient(135deg, #fbfdff, #f6f9ff);
}

.tile-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  flex: 0 0 auto;
  border-radius: 50%;
  font-size: 21px;
}

.tile-icon.blue {
  color: #1677ff;
  background: #eaf3ff;
}

.tile-icon.rose {
  color: #f04465;
  background: #fff0f4;
}

.tile-icon.green {
  color: #22b86f;
  background: #eaf8ef;
}

.readonly-tile span:not(.tile-icon),
.readonly-tile strong {
  display: block;
}

.readonly-tile span:not(.tile-icon) {
  color: #6b7280;
  font-size: 13px;
}

.readonly-tile strong {
  margin-top: 5px;
  color: #111827;
  font-size: 17px;
}

.inline-public-tip {
  border: 0;
  border-radius: 6px;
  background: #f2f7ff;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 0;
}

.security-card {
  position: relative;
  overflow: hidden;
}

.security-card header {
  position: relative;
  min-height: 86px;
  margin-bottom: 14px;
}

.security-card h2,
.completion-card h2,
.public-info-card h2,
.preview-card h2 {
  font-size: 19px;
}

.security-card h2 :deep(.el-icon) {
  color: #111827;
}

.security-card :deep(.el-button) {
  width: 100%;
  min-height: 42px;
  font-weight: 700;
}

.security-watermark {
  position: absolute;
  right: -18px;
  bottom: -38px;
  color: rgba(22, 119, 255, 0.06);
  font-size: 116px;
}

.completion-card :deep(.el-card__body) {
  padding-bottom: 18px;
}

.completion-content {
  display: grid;
  grid-template-columns: 92px minmax(0, 1fr);
  gap: 18px;
  align-items: center;
  margin-top: 14px;
}

.completion-ring {
  position: relative;
  display: grid;
  place-items: center;
  width: 86px;
  height: 86px;
  border-radius: 50%;
  background: conic-gradient(#1677ff var(--completion), #e8f0ff 0);
}

.completion-ring::after {
  position: absolute;
  inset: 10px;
  content: '';
  border-radius: inherit;
  background: #fff;
}

.completion-ring span {
  position: relative;
  z-index: 1;
  color: #1677ff;
  font-size: 22px;
  font-weight: 900;
}

.completion-copy strong {
  color: #111827;
  font-weight: 800;
}

.completion-copy p {
  margin-top: 6px;
  font-size: 13px;
}

.completion-checks {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
  margin-top: 10px;
  color: #8a97aa;
  font-size: 12px;
}

.completion-checks span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.completion-checks .completed {
  color: #22a864;
}

.suggestion-toggle {
  width: 100%;
  height: 38px;
  margin-top: 14px;
  border: 0;
  border-radius: 6px;
  background: #f1f6ff;
  color: #1268ed;
  font-weight: 700;
  cursor: pointer;
}

.suggestion-toggle span {
  margin-left: 8px;
  color: #6d7f99;
  font-weight: 600;
}

.completion-tips {
  display: grid;
  gap: 6px;
  margin: 12px 0 0;
  padding: 10px 14px 10px 28px;
  border-radius: 6px;
  background: #f8fbff;
  color: #5f6f85;
  font-size: 13px;
}

.public-info-card {
  padding: 18px 20px;
  border-color: #f3c979;
  background: linear-gradient(135deg, #fffaf0, #fffdf8);
  box-shadow: none;
}

.public-info-card h2 {
  color: #a55c08;
  font-size: 17px;
}

.public-info-card ul {
  display: grid;
  gap: 4px;
  margin: 8px 0 0;
  padding-left: 20px;
  color: #48566b;
  line-height: 1.6;
}

.preview-card header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  margin-bottom: 18px;
}

.preview-card {
  flex: 1 0 auto;
  min-height: 180px;
}

.preview-card header a {
  color: #1268ed;
  font-weight: 800;
  text-decoration: none;
  white-space: nowrap;
}

.preview-profile {
  display: flex;
  align-items: center;
  gap: 16px;
}

.preview-profile strong,
.preview-profile span {
  display: block;
}

.preview-profile strong {
  color: #111827;
  font-size: 18px;
}

.preview-profile span {
  margin-top: 5px;
  color: #66758d;
}

.preview-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 9px;
}

.preview-badges em {
  display: inline-flex;
  align-items: center;
  height: 22px;
  padding: 0 9px;
  border: 1px solid #aee8c9;
  border-radius: 999px;
  color: #18a55d;
  background: #f0fff6;
  font-style: normal;
  font-size: 12px;
  font-weight: 700;
}

.preview-badges em:not(.verified) {
  border-color: #bfdbfe;
  color: #1677ff;
  background: #eff6ff;
}

.password-form {
  display: grid;
  gap: 4px;
}

@media (max-width: 1180px) {
  .profile-edit-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .edit-panel-header,
  .preview-card header {
    flex-direction: column;
    align-items: flex-start;
  }

  .form-grid,
  .readonly-grid,
  .completion-content {
    grid-template-columns: 1fr;
  }

  .completion-ring {
    justify-self: start;
  }
}
</style>
