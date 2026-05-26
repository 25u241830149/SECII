<template>
  <div class="auth-view">
    <header>
      <h1>登录</h1>
      <p>登录你的 CampusHub 账号</p>
    </header>

    <el-form
      ref="formRef"
      class="auth-form"
      :model="form"
      :rules="rules"
      label-position="top"
      @submit.prevent="submit"
    >
      <el-form-item label="学号" prop="studentNo">
        <el-input
          v-model="form.studentNo"
          size="large"
          placeholder="请输入学号"
          autocomplete="username"
          :disabled="loading"
        />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input
          v-model="form.password"
          size="large"
          type="password"
          placeholder="请输入密码"
          autocomplete="current-password"
          show-password
          :disabled="loading"
        />
      </el-form-item>

      <div class="form-row">
        <el-checkbox v-model="rememberMe" :disabled="loading">记住我</el-checkbox>
        <el-button class="text-action" type="primary" link :disabled="loading" @click="showForgotPasswordTodo">
          忘记密码？
        </el-button>
      </div>

      <el-button class="primary-action" type="primary" size="large" native-type="submit" :loading="loading">
        登录
      </el-button>

      <div class="divider"><span>或</span></div>

      <el-button class="outline-action" size="large" :disabled="loading" @click="showSsoUnavailable">
        使用统一身份认证登录
      </el-button>

      <p class="switch-line">
        还没有账号？
        <RouterLink to="/register">立即注册</RouterLink>
      </p>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'

import { useAuthStore } from '@/stores'
const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

interface LoginForm {
  studentNo: string
  password: string
}

const loading = ref(false)
const rememberMe = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<LoginForm>({
  studentNo: '',
  password: '',
})

const validatePassword = (
  _rule: unknown,
  value: string,
  callback: (error?: Error) => void,
) => {
  if (!value) {
    callback(new Error('请输入密码'))
    return
  }

  if (value.length < 6) {
    callback(new Error('密码长度不能少于 6 位'))
    return
  }

  callback()
}

const rules = reactive<FormRules<LoginForm>>({
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  password: [{ validator: validatePassword, trigger: 'blur' }],
})

const getRedirectPath = () => {
  const redirect = route.query.redirect
  return typeof redirect === 'string' && redirect.startsWith('/') ? redirect : '/'
}

const showSsoUnavailable = () => {
  ElMessage.info('统一身份认证暂未接入')
}

const showForgotPasswordTodo = () => {
  ElMessage.info('忘记密码流程暂未开放，请先联系管理员处理')
}

const submit = async () => {
  if (loading.value) return

  const studentId = form.studentNo.trim()
  const password = form.password

  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    ElMessage.warning('请按要求填写登录信息')
    return
  }

  loading.value = true

  try {
    await authStore.login({ studentId, password }, { rememberMe: rememberMe.value })
    ElMessage.success('登录成功')
    await router.push(getRedirectPath())
  } catch {
    // The request interceptor already displays the login error toast.
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-view header {
  margin-bottom: 28px;
}

h1 {
  margin: 0;
  color: #111827;
  font-size: 34px;
}

p {
  margin: 10px 0 0;
  color: #718096;
}

.auth-form {
  display: grid;
  gap: 6px;
}

.auth-form :deep(.el-form-item__label::before) {
  display: none;
}

.form-row,
.switch-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.form-row {
  margin: 2px 0 18px;
}

a {
  color: #1268ed;
  font-weight: 700;
  text-decoration: none;
}

.primary-action,
.outline-action {
  width: 100%;
  border-radius: 8px;
}

.text-action {
  height: auto;
  padding: 0;
  font-weight: 700;
}

.primary-action {
  border: 0;
  background: linear-gradient(135deg, #1478ff, #6b48ff);
}

.outline-action {
  color: #1268ed;
  font-weight: 700;
}

.divider {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 20px 0 18px;
  color: #8b95a5;
}

.divider::before,
.divider::after {
  flex: 1;
  height: 1px;
  background: #e5ebf4;
  content: "";
}

.switch-line {
  justify-content: center;
  gap: 8px;
  margin-top: 18px;
}
</style>
