<template>
  <div>
    <div class="panel-header">
      <h1 class="panel-title">登录 CampusHub</h1>
      <p class="panel-description">先打通头像区、消息入口和个人中心的登录态切换。</p>
    </div>

    <el-form ref="formRef" label-position="top" :model="form" :rules="rules">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" placeholder="输入用户名或学号" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="form.password" type="password" show-password placeholder="输入密码" />
      </el-form-item>
    </el-form>

    <div class="form-actions">
      <el-button type="primary" round :loading="submitting" @click="handleLogin">登录并进入主页</el-button>
      <el-button round @click="router.push('/register')">去注册</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getUserProfile, login } from '@/api/user'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const form = reactive({ username: '', password: '' })

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名或学号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' },
  ],
}

async function handleLogin() {
  if (!formRef.value) {
    return
  }

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  submitting.value = true
  try {
    const loginResult = await login({
      username: form.username,
      password: form.password,
    })

    authStore.loginSuccess(loginResult)

    const profile = await getUserProfile(loginResult.userId)
    userStore.setProfile(profile)

    ElMessage.success(`欢迎回来，${profile.nickname || form.username}`)

    const redirect = typeof route.query.redirect === 'string'
      ? route.query.redirect
      : loginResult.role === 'ADMIN'
        ? '/admin/stats'
        : '/'

    await router.push(redirect)
  } finally {
    submitting.value = false
  }
}
</script>