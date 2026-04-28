<template>
  <section class="page-surface">
    <div class="section-heading">
      <div>
        <h1 class="section-title">账户设置</h1>
        <p class="section-subtitle">保留退出登录和注销账号两条动作链，方便后续接入真实接口。</p>
      </div>
    </div>

    <div class="danger-zone">
      <h3>注销提醒</h3>
      <p>注销后会清空本地登录态并跳回公共入口，真实后端接入后需要二次确认与风险提示。</p>

      <div class="form-actions">
        <el-button plain @click="handleLogout">退出登录</el-button>
        <el-button type="danger" round @click="handleCancelAccount">注销账号</el-button>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const authStore = useAuthStore()
const userStore = useUserStore()

async function handleLogout() {
  authStore.logout()
  userStore.clearProfile()
  ElMessage.success('已退出当前账号')
  await router.push('/login')
}

async function handleCancelAccount() {
  try {
    await ElMessageBox.confirm('当前仅为前端骨架演示，确认继续模拟注销吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确认注销',
      cancelButtonText: '再想想',
    })
    authStore.logout()
    userStore.clearProfile()
    ElMessage.success('账号注销流程骨架已执行')
    await router.push('/register')
  } catch {
    ElMessage.info('已取消注销操作')
  }
}
</script>