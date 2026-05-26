<template>
  <div class="auth-view register-view">
    <header>
      <h1>注册</h1>
      <p>创建你的 CampusHub 账号</p>
    </header>

    <el-form
      class="auth-form"
      :class="{ 'is-scrolling': formScrolling }"
      :model="form"
      label-position="top"
      @scroll.passive="handleFormScroll"
      @submit.prevent
    >
      <el-form-item label="学号">
        <el-input v-model="form.studentNo" size="large" placeholder="请输入学号" autocomplete="username" />
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="form.nickname" size="large" placeholder="请输入昵称" />
      </el-form-item>
      <el-form-item label="真实姓名">
        <el-input v-model="form.realName" size="large" placeholder="用于实名认证审核" />
      </el-form-item>
      <el-form-item label="所属学院">
        <el-input v-model="form.department" size="large" placeholder="例如：软件学院" />
      </el-form-item>
      <el-form-item label="学生证材料">
        <el-upload
          v-model:file-list="studentCardFiles"
          class="student-card-upload"
          :auto-upload="false"
          :limit="1"
          accept="image/*"
          :on-change="handleStudentCardChange"
          :on-remove="handleStudentCardRemove"
        >
          <el-button>选择学生证图片</el-button>
          <template #tip>
            <span class="upload-tip">支持 JPG、PNG、WebP等格式</span>
          </template>
        </el-upload>
      </el-form-item>
      <el-form-item label="密码">
        <el-input
          v-model="form.password"
          size="large"
          type="password"
          placeholder="请输入密码"
          autocomplete="new-password"
          show-password
        />
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input
          v-model="form.confirmPassword"
          size="large"
          type="password"
          placeholder="请再次输入密码"
          autocomplete="new-password"
          show-password
        />
      </el-form-item>

      <el-checkbox v-model="form.agreementAccepted">
        我已阅读并同意
        <RouterLink to="/agreement">《用户协议》</RouterLink>
        与
        <RouterLink to="/privacy">《隐私政策》</RouterLink>
      </el-checkbox>

      <el-button class="primary-action" type="primary" size="large" :loading="loading" @click="submit">
        注册
      </el-button>

      <p class="switch-line">
        已有账号？
        <RouterLink to="/login">立即登录</RouterLink>
      </p>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadFile, UploadFiles, UploadProps, UploadUserFile } from 'element-plus'
import { useRouter } from 'vue-router'

import { uploadStudentCard } from '@/api/upload'
import { registerUser } from '@/api/user'

const router = useRouter()

interface RegisterForm {
  studentNo: string
  nickname: string
  realName: string
  department: string
  password: string
  confirmPassword: string
  agreementAccepted: boolean
}

const MAX_STUDENT_CARD_SIZE = 5 * 1024 * 1024

const loading = ref(false)
const formScrolling = ref(false)
const selectedStudentCard = ref<File | null>(null)
const studentCardFiles = ref<UploadUserFile[]>([])
let formScrollTimer: ReturnType<typeof window.setTimeout> | undefined

const form = reactive<RegisterForm>({
  studentNo: '',
  nickname: '',
  realName: '',
  department: '',
  password: '',
  confirmPassword: '',
  agreementAccepted: false,
})

const removeRejectedFile = (uploadFile: UploadFile, uploadFiles: UploadFiles) => {
  studentCardFiles.value = uploadFiles.filter((file) => file.uid !== uploadFile.uid)
  selectedStudentCard.value = null
}

const handleStudentCardChange: UploadProps['onChange'] = (uploadFile, uploadFiles) => {
  const rawFile = uploadFile.raw

  if (!rawFile) {
    return
  }

  if (!rawFile.type.startsWith('image/')) {
    ElMessage.warning('学生证材料仅支持图片文件')
    removeRejectedFile(uploadFile, uploadFiles)
    return
  }

  if (rawFile.size > MAX_STUDENT_CARD_SIZE) {
    ElMessage.warning('学生证图片不能超过 5MB')
    removeRejectedFile(uploadFile, uploadFiles)
    return
  }

  selectedStudentCard.value = rawFile
  studentCardFiles.value = [uploadFile]
}

const handleStudentCardRemove: UploadProps['onRemove'] = () => {
  selectedStudentCard.value = null
}

const handleFormScroll = () => {
  formScrolling.value = true

  if (formScrollTimer) {
    window.clearTimeout(formScrollTimer)
  }

  formScrollTimer = window.setTimeout(() => {
    formScrolling.value = false
  }, 900)
}

const submit = async () => {
  const studentId = form.studentNo.trim()
  const nickname = form.nickname.trim()
  const realName = form.realName.trim()
  const department = form.department.trim()

  if (!studentId || !nickname || !realName || !department || !form.password) {
    ElMessage.warning('请完整填写注册信息')
    return
  }

  if (form.password.length < 6) {
    ElMessage.warning('密码长度不能少于 6 位')
    return
  }

  if (form.password !== form.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  if (!form.agreementAccepted) {
    ElMessage.warning('请先阅读并同意用户协议与隐私政策')
    return
  }

  if (!selectedStudentCard.value) {
    ElMessage.warning('请选择学生证材料图片')
    return
  }

  loading.value = true

  try {
    const uploadResult = await uploadStudentCard(selectedStudentCard.value, studentId)
    await registerUser({
      studentId,
      password: form.password,
      nickname,
      realName,
      department,
      studentCardImage: uploadResult.fileUrl,
    })
    ElMessage.success('注册成功，请登录')
    await router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-view header {
  flex: 0 0 auto;
  margin-bottom: 16px;
}

h1 {
  margin: 0;
  color: #111827;
  font-size: 32px;
}

p {
  margin: 10px 0 0;
  color: #718096;
}

.auth-form {
  display: grid;
  gap: 0;
}

.auth-form :deep(.el-form-item__label::before) {
  display: none;
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 12px;
}

.auth-form :deep(.el-form-item__label) {
  margin-bottom: 4px;
  line-height: 1.2;
}

.auth-form :deep(.el-input__wrapper) {
  min-height: 42px;
}

.auth-form :deep(.el-input__inner) {
  height: 42px;
}

.primary-action {
  width: 100%;
  margin-top: 4px;
  border: 0;
  border-radius: 8px;
  background: linear-gradient(135deg, #1478ff, #6b48ff);
}

.student-card-upload {
  width: 100%;
}

.upload-tip {
  display: block;
  margin-top: 4px;
  color: #8b95a5;
  font-size: 12px;
  line-height: 1.35;
}

a {
  color: #1268ed;
  font-weight: 700;
  text-decoration: none;
}

.switch-line {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 12px;
}
</style>
