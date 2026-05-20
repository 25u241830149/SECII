<template>
  <div class="auth-view">
    <header>
      <h1>登录</h1>
      <p>登录你的 CampusHub 账号</p>
    </header>

    <el-form class="auth-form" :model="form" label-position="top" @submit.prevent>
      <el-form-item label="学号">
        <el-input v-model="form.studentNo" size="large" placeholder="请输入学号" autocomplete="username" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input
          v-model="form.password"
          size="large"
          type="password"
          placeholder="请输入密码"
          autocomplete="current-password"
          show-password
        />
      </el-form-item>

      <div class="form-row">
        <el-checkbox v-model="rememberMe">记住我</el-checkbox>
        <RouterLink to="/forgot-password">忘记密码？</RouterLink>
      </div>

      <el-button class="primary-action" type="primary" size="large" :loading="loading" @click="submit">
        登录
      </el-button>

      <div class="divider"><span>或</span></div>

      <el-button class="outline-action" size="large">使用统一身份认证登录</el-button>

      <p class="switch-line">
        还没有账号？
        <RouterLink to="/register">立即注册</RouterLink>
      </p>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
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
const form = reactive<LoginForm>({
  studentNo: '',
  password: '',
})

const getRedirectPath = () => {
  const redirect = route.query.redirect
  return typeof redirect === 'string' && redirect.startsWith('/') ? redirect : '/'
}

const submit = async () => {
  const studentId = form.studentNo.trim()
  const password = form.password

  if (!studentId || !password) {
    ElMessage.warning('请填写学号和密码')
    return
  }

  loading.value = true

  try {
    await authStore.login({ studentId, password })
    ElMessage.success('登录成功')
    await router.push(getRedirectPath())
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
