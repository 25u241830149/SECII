<template>
  <div class="auth-view">
    <header>
      <h1>注册</h1>
      <p>创建你的 CampusHub 账号</p>
    </header>

    <el-form class="auth-form" :model="form" label-position="top" @submit.prevent>
      <el-form-item label="学号">
        <el-input v-model="form.studentNo" size="large" placeholder="请输入学号" />
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="form.nickname" size="large" placeholder="请输入昵称" />
      </el-form-item>
      <el-form-item label="学院">
        <el-select v-model="form.college" size="large" placeholder="请选择学院">
          <el-option label="计算机学院" value="计算机学院" />
          <el-option label="软件学院" value="软件学院" />
          <el-option label="电子信息学院" value="电子信息学院" />
          <el-option label="经济管理学院" value="经济管理学院" />
        </el-select>
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="form.phone" size="large" placeholder="请输入手机号" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="form.password" size="large" type="password" placeholder="请输入密码" show-password />
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input
          v-model="form.confirmPassword"
          size="large"
          type="password"
          placeholder="请再次输入密码"
          show-password
        />
      </el-form-item>

      <el-checkbox v-model="form.agreementAccepted">
        我已阅读并同意
        <RouterLink to="/agreement">《用户协议》</RouterLink>
        与
        <RouterLink to="/privacy">《隐私政策》</RouterLink>
      </el-checkbox>

      <el-button class="primary-action" type="primary" size="large" @click="submit">注册</el-button>

      <p class="switch-line">
        已有账号？
        <RouterLink to="/login">立即登录</RouterLink>
      </p>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

import type { RegisterRequest } from '@/types'

const router = useRouter()

const form = reactive<RegisterRequest>({
  studentNo: '',
  nickname: '',
  college: '',
  phone: '',
  password: '',
  confirmPassword: '',
  agreementAccepted: false,
})

const submit = async () => {
  if (!form.studentNo || !form.nickname || !form.college || !form.phone || !form.password) {
    ElMessage.warning('请完整填写注册信息')
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

  ElMessage.success('注册表单已就绪，等待接入用户 API')
  await router.push('/login')
}
</script>

<style scoped>
.auth-view header {
  margin-bottom: 22px;
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
  gap: 2px;
}

.primary-action {
  width: 100%;
  margin-top: 8px;
  border: 0;
  border-radius: 8px;
  background: linear-gradient(135deg, #1478ff, #6b48ff);
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
  margin-top: 14px;
}
</style>
