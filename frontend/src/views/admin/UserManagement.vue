<template>
  <section class="admin-page">
    <article class="toolbar-card">
      <div>
        <h2>用户管理</h2>
        <p>搜索并选择目标账号后，可执行封禁或实名认证复核。</p>
      </div>
    </article>

    <article class="operation-card user-picker">
      <header>
        <h3>目标用户</h3>
        <p>支持按学号、昵称、姓名或 ID 搜索，并按角色筛选。</p>
      </header>
      <div class="picker-controls">
        <el-select v-model="roleFilter" class="role-select" @change="loadUsers(searchKeyword)">
          <el-option label="全部角色" value="ALL" />
          <el-option label="普通用户" value="USER" />
          <el-option label="管理员" value="ADMIN" />
        </el-select>
        <el-select
          v-model="selectedUserId"
          filterable
          remote
          clearable
          :remote-method="loadUsers"
          :loading="userLoading"
          placeholder="搜索用户 ID / 学号 / 昵称 / 姓名"
          class="user-select"
        >
          <el-option
            v-for="user in users"
            :key="user.userId"
            :label="formatUserLabel(user)"
            :value="user.userId"
          >
            <div class="user-option">
              <strong>{{ user.nickname }}</strong>
              <span>{{ user.studentId }} · #{{ user.userId }}</span>
              <em :class="['status-tag', user.status.toLowerCase()]">{{ statusLabels[user.status] }}</em>
            </div>
          </el-option>
        </el-select>
      </div>
      <div v-if="selectedUser" class="selected-user">
        <strong>{{ selectedUser.nickname }}</strong>
        <span>{{ selectedUser.studentId }} · {{ roleLabels[selectedUser.role] }}</span>
        <span>信用分 {{ selectedUser.creditScore }}</span>
        <em :class="['status-tag', selectedUser.status.toLowerCase()]">{{ statusLabels[selectedUser.status] }}</em>
      </div>
    </article>

    <section class="operation-grid">
      <article class="operation-card">
        <header>
          <h3>封禁用户</h3>
          <p>记录封禁原因并将用户状态置为不可用。</p>
        </header>
        <el-form label-position="top">
          <el-form-item label="当前用户">
            <el-input :model-value="selectedUserLabel" disabled />
          </el-form-item>
          <el-form-item label="封禁天数">
            <el-input-number v-model="banForm.days" :min="1" :max="365" controls-position="right" />
          </el-form-item>
          <el-form-item label="封禁原因">
            <el-input v-model="banForm.reason" type="textarea" :rows="4" maxlength="255" show-word-limit />
          </el-form-item>
          <el-button type="danger" :loading="banSubmitting" @click="submitBan">执行封禁</el-button>
        </el-form>
      </article>

      <article class="operation-card">
        <header>
          <h3>实名认证审核</h3>
          <p>通过或驳回用户提交的学生认证材料。</p>
        </header>
        <el-form label-position="top">
          <el-form-item label="当前用户">
            <el-input :model-value="selectedUserLabel" disabled />
          </el-form-item>
          <div class="verification-material">
            <header>
              <h4>认证材料</h4>
              <span :class="['verify-tag', selectedVerificationStatusClass]">
                {{ selectedVerificationStatusLabel }}
              </span>
            </header>
            <div v-if="selectedUser" class="material-grid">
              <div class="material-info">
                <p><span>实名</span><strong>{{ selectedUser.verificationRealName || '未提交' }}</strong></p>
                <p><span>提交时间</span><strong>{{ formatDate(selectedUser.verificationSubmittedAt) }}</strong></p>
                <p v-if="selectedUser.verificationRemark"><span>备注</span><strong>{{ selectedUser.verificationRemark }}</strong></p>
              </div>
              <el-image
                v-if="selectedUser.verificationStudentCardImage"
                class="material-image"
                :src="resolveAssetUrl(selectedUser.verificationStudentCardImage)"
                :preview-src-list="[resolveAssetUrl(selectedUser.verificationStudentCardImage)]"
                fit="cover"
                preview-teleported
              />
              <div v-else class="material-empty">暂无学生证图片</div>
            </div>
            <el-empty v-else description="请先选择用户" :image-size="72" />
          </div>
          <el-form-item label="审核结果">
            <el-radio-group v-model="verifyForm.approved">
              <el-radio-button :label="true">通过</el-radio-button>
              <el-radio-button :label="false">驳回</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="审核备注">
            <el-input v-model="verifyForm.remark" type="textarea" :rows="4" maxlength="255" show-word-limit />
          </el-form-item>
          <el-button type="primary" :loading="verifySubmitting" @click="submitVerify">保存审核结果</el-button>
        </el-form>
      </article>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { banUser, searchAdminUsers, verifyUser } from '@/api/admin'
import type { AdminUserOptionDTO, AdminUserRole } from '@/api/admin'
import { resolveAssetUrl } from '@/utils/asset'

const banSubmitting = ref(false)
const verifySubmitting = ref(false)
const userLoading = ref(false)
const users = ref<AdminUserOptionDTO[]>([])
const selectedUserId = ref<number | string | null>(null)
const roleFilter = ref<AdminUserRole>('ALL')
const searchKeyword = ref('')

const banForm = reactive({
  days: 7,
  reason: '',
})

const verifyForm = reactive({
  approved: true,
  remark: '',
})

const roleLabels: Record<AdminUserOptionDTO['role'], string> = {
  USER: '普通用户',
  ADMIN: '管理员',
}

const statusLabels: Record<AdminUserOptionDTO['status'], string> = {
  NORMAL: '正常',
  PENDING_VERIFICATION: '待认证',
  BANNED: '已封禁',
}

const verificationStatusLabels: Record<NonNullable<AdminUserOptionDTO['verificationStatus']>, string> = {
  NONE: '未提交',
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已驳回',
}

const selectedUser = computed(() => users.value.find((user) => user.userId === selectedUserId.value) || null)
const selectedUserLabel = computed(() =>
  selectedUser.value ? `${selectedUser.value.nickname}（${selectedUser.value.studentId} / #${selectedUser.value.userId}）` : '请先选择用户',
)
const selectedVerificationStatus = computed(() => selectedUser.value?.verificationStatus || 'NONE')
const selectedVerificationStatusLabel = computed(() => verificationStatusLabels[selectedVerificationStatus.value])
const selectedVerificationStatusClass = computed(() => selectedVerificationStatus.value.toLowerCase())

const formatUserLabel = (user: AdminUserOptionDTO) =>
  `${user.nickname} / ${user.studentId} / #${user.userId} / ${roleLabels[user.role]}`

const formatDate = (value?: string | null) => {
  if (!value) return '未提交'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value.replace('T', ' ').slice(0, 16)
  return date.toLocaleString('zh-CN', { hour12: false })
}

const loadUsers = async (keyword = '') => {
  searchKeyword.value = keyword
  userLoading.value = true
  try {
    users.value = await searchAdminUsers({
      keyword: keyword.trim() || undefined,
      role: roleFilter.value,
      size: 30,
    })
  } finally {
    userLoading.value = false
  }
}

const submitBan = async () => {
  if (!selectedUser.value) {
    ElMessage.warning('请先选择要封禁的用户')
    return
  }
  if (!banForm.reason.trim()) {
    ElMessage.warning('请填写封禁原因')
    return
  }

  banSubmitting.value = true
  try {
    await banUser(selectedUser.value.userId, {
      days: banForm.days,
      reason: banForm.reason.trim(),
    })
    ElMessage.success('用户已封禁')
    await loadUsers(searchKeyword.value)
  } finally {
    banSubmitting.value = false
  }
}

const submitVerify = async () => {
  if (!selectedUser.value) {
    ElMessage.warning('请先选择要审核的用户')
    return
  }
  verifySubmitting.value = true
  try {
    await verifyUser(selectedUser.value.userId, {
      approved: verifyForm.approved,
      remark: verifyForm.remark.trim(),
    })
    ElMessage.success('审核结果已保存')
    await loadUsers(searchKeyword.value)
  } finally {
    verifySubmitting.value = false
  }
}

onMounted(() => loadUsers())
</script>

<style scoped>
.admin-page {
  display: grid;
  gap: 18px;
}

.toolbar-card,
.operation-card {
  padding: 20px;
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 26px rgba(15, 23, 42, 0.06);
}

.toolbar-card h2,
.toolbar-card p,
.operation-card h3,
.operation-card p {
  margin: 0;
}

.toolbar-card h2,
.operation-card h3 {
  color: #111827;
  font-size: 20px;
}

.toolbar-card p,
.operation-card p {
  margin-top: 6px;
  color: #64748b;
}

.operation-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.operation-card header {
  margin-bottom: 18px;
}

.user-picker {
  display: grid;
  gap: 16px;
}

.picker-controls {
  display: grid;
  grid-template-columns: 160px minmax(0, 1fr);
  gap: 12px;
}

.user-select {
  width: 100%;
}

.user-option,
.selected-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-option span,
.selected-user span {
  color: #64748b;
}

.selected-user {
  padding: 12px 14px;
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #f8fbff;
}

.status-tag {
  padding: 3px 8px;
  border-radius: 999px;
  background: #eef5ff;
  color: #1268ed;
  font-style: normal;
  font-size: 12px;
  font-weight: 700;
}

.status-tag.banned {
  background: #fff1f1;
  color: #ef4444;
}

.status-tag.pending_verification {
  background: #fff7ed;
  color: #f97316;
}

.verification-material {
  display: grid;
  gap: 12px;
  margin: 4px 0 18px;
  padding: 14px;
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #f8fbff;
}

.verification-material header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 0;
}

.verification-material h4 {
  margin: 0;
  color: #111827;
  font-size: 16px;
}

.verify-tag {
  padding: 3px 9px;
  border-radius: 999px;
  background: #eef5ff;
  color: #1268ed;
  font-size: 12px;
  font-weight: 800;
}

.verify-tag.none {
  background: #f1f5f9;
  color: #64748b;
}

.verify-tag.pending {
  background: #fff7ed;
  color: #f97316;
}

.verify-tag.approved {
  background: #ecfdf3;
  color: #16a34a;
}

.verify-tag.rejected {
  background: #fff1f2;
  color: #ef4444;
}

.material-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 180px;
  gap: 14px;
  align-items: stretch;
}

.material-info {
  display: grid;
  gap: 8px;
}

.material-info p {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr);
  gap: 10px;
  margin: 0;
}

.material-info span {
  color: #64748b;
}

.material-info strong {
  min-width: 0;
  overflow: hidden;
  color: #111827;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.material-image,
.material-empty {
  width: 180px;
  height: 126px;
  border-radius: 8px;
}

.material-image {
  border: 1px solid #dbe6f4;
  background: #fff;
}

.material-empty {
  display: grid;
  place-items: center;
  border: 1px dashed #cbd5e1;
  color: #94a3b8;
  font-size: 13px;
}

@media (max-width: 900px) {
  .operation-grid {
    grid-template-columns: 1fr;
  }

  .picker-controls {
    grid-template-columns: 1fr;
  }

  .material-grid {
    grid-template-columns: 1fr;
  }

  .material-image,
  .material-empty {
    width: 100%;
  }
}
</style>
