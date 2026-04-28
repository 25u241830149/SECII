<template>
  <div>
    <div class="panel-header">
      <h1 class="panel-title">注册账号</h1>
      <p class="panel-description">注册页只保留最基础字段，后续可继续接入实名认证与头像上传。</p>
    </div>

    <el-form label-position="top" :model="form">
      <div class="form-grid">
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
      </div>
    </el-form>

    <div class="form-actions">
      <el-button type="primary" round @click="handleRegister">注册并前往登录</el-button>
      <el-button round @click="router.push('/login')">已有账号</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const form = reactive({ nickname: '新同学', email: 'student@example.com', username: 'student_001', password: '123456' })

async function handleRegister() {
  ElMessage.success(`注册信息已填写完成：${form.username}`)
  await router.push('/login')
}
</script>