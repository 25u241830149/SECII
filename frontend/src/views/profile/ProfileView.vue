<template>
  <section class="page-surface">
    <div class="section-heading">
      <div>
        <h1 class="section-title">个人资料</h1>
        <p class="section-subtitle">右上角头像区与个人中心共用同一份用户状态。</p>
      </div>
    </div>

    <el-form label-position="top" :model="form">
      <div class="form-grid">
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="学院/部门">
          <el-input v-model="form.department" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
      </div>
    </el-form>

    <div class="form-actions">
      <el-button type="primary" round @click="handleSave">保存资料</el-button>
    </div>
  </section>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { mockCurrentUser } from '@/constants/mock'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const source = userStore.profile ?? mockCurrentUser

const form = reactive({
  nickname: source.nickname,
  department: source.department ?? '',
  email: source.email,
  phone: source.phone,
})

function handleSave() {
  userStore.setProfile({
    ...source,
    nickname: form.nickname,
    department: form.department,
    email: form.email,
    phone: form.phone,
  })
  ElMessage.success('资料更新已写入本地状态骨架')
}
</script>